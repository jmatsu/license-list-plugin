package io.github.jmatsu.license.internal

import io.github.jmatsu.license.LicenseListPlugin
import io.github.jmatsu.license.ext.collectToMap
import io.github.jmatsu.license.model.LOCAL_FILE_GROUP
import io.github.jmatsu.license.model.LicenseSeed
import io.github.jmatsu.license.model.ResolveScope
import io.github.jmatsu.license.model.ResolvedArtifact
import io.github.jmatsu.license.model.ResolvedLocalFileMetadata
import io.github.jmatsu.license.model.ResolvedModuleIdentifier
import io.github.jmatsu.license.model.VersionString
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.attributes.Attribute
import org.gradle.kotlin.dsl.withArtifacts
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import java.io.File
import java.util.SortedMap
import java.util.zip.ZipFile
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3

class ArtifactManagement(
    private val project: Project,
    private val configurationNames: Set<String>,
    private val exclusionPredicate: IgnorePredicate?,
) {
    companion object {
        /**
         * Common configurations that will be used basically
         * Gradle 9: Use RuntimeClasspath as the resolvable configuration
         */
        val CommonConfigurationNames: Set<String> =
            setOf(
                "RuntimeClasspath",
            )

        /**
         * Common KMP variant suffixes
         */
        private val KMP_VARIANT_SUFFIXES =
            listOf(
                "-android",
                "-jvm",
                "-desktop",
                "-native",
                "-js",
                "-iosarm64",
                "-iosx64",
                "-jvmstubs",
                "-linuxx64",
                "-macosx64",
                "-mingwx64",
            )

        /**
         * Normalize artifact name by removing KMP variant suffix
         */
        internal fun normalizeArtifactName(name: String): String {
            var normalized = name
            for (suffix in KMP_VARIANT_SUFFIXES) {
                if (normalized.endsWith(suffix)) {
                    normalized = normalized.removeSuffix(suffix)
                    break
                }
            }
            return normalized
        }

        /**
         * Check if artifact name has a KMP variant suffix
         */
        internal fun hasKmpVariantSuffix(name: String): Boolean = name != normalizeArtifactName(name)
    }

    /**
     * Analyze dependencies based on the given scopes.
     *
     * @param variantScope a variant like freeRelease that depends on users' projects.
     * @param additionalScopes not plain scopes like test, androidTest and what users have defined. No order-aware. May be empty.
     * @return resolved artifacts grouped by scopes and sorted by scopes
     */
    fun analyze(
        variantScope: ResolveScope.Variant,
        additionalScopes: Set<ResolveScope.Addition>,
    ): SortedMap<ResolveScope, List<ResolvedArtifact>> {
        val variantConfigurations = project.allConfigurations(variantScope)

        val scopedConfigurations =
            (
                listOf(variantScope to variantConfigurations) +
                    additionalScopes.map { scope ->
                        scope to project.allConfigurations(variantScope, additionalScope = scope)
                    }
            ).toMap()

        val components: MutableList<ComponentIdentifier> = ArrayList()

        val localFileMap: MutableMap<ResolveScope, List<File>> = hashMapOf()

        val scopedModules =
            scopedConfigurations.mapValues { (scope, configurations) ->
                configurations
                    .flatMap { configuration ->
                        runCatching {
                            localFileMap.putIfAbsent(scope, emptyList())
                            localFileMap[scope] = localFileMap.getValue(scope) +
                                configuration.allDependencies
                                    .filterIsInstance<FileCollectionDependency>()
                                    .flatMap {
                                        it.files.files
                                    }.filter { it.name.endsWith(".jar") || it.name.endsWith(".aar") }
                        }.onFailure {
                            LicenseListPlugin.logger?.error("could not resolve local files in ${scope.name}", it)
                        }

                        val newResolvedIdentifiers =
                            configuration
                                .toResolvedModuleIdentifiers()
                                .filterNot { id ->
                                    id.id?.componentIdentifier in components
                                }

                        components += newResolvedIdentifiers.mapNotNull { it.id?.componentIdentifier }.toSet()

                        newResolvedIdentifiers
                    }.groupedSortedDistinct()
                    .mapKeys { it.value.id!!.componentIdentifier }
            }

        val resolveResults =
            project.dependencies
                .createArtifactResolutionQuery()
                .forComponents(scopedModules.flatMap { (_, modules) -> modules.keys })
                .withArtifacts(MavenModule::class, MavenPomArtifact::class)
                .execute()

        val scopedModuleSeq = scopedModules.asSequence()

        val allScopes = listOf(variantScope) + additionalScopes

        val scopedLocalFiles =
            localFileMap.values
                .flatten()
                .distinct()
                .groupBy { file ->
                    localFileMap.asIterable().first { (_, files) -> file in files }.key
                }.flatMap { (key, files) ->
                    files.mapNotNull { file ->
                        val id =
                            ResolvedModuleIdentifier(
                                group = LOCAL_FILE_GROUP,
                                name = file.name,
                            )

                        if (exclusionPredicate?.invoke(id.group, id.name) == true) {
                            return@mapNotNull null
                        }

                        val licenseCandidates =
                            ZipFile(file).use { zip ->
                                zip
                                    .entries()
                                    .asSequence()
                                    .filter { !it.isDirectory && it.name.startsWith("META-INF/") && it.name.contains("LICENSE") }
                                    .map { it.name }
                                    .toList()
                            }

                        // It's hard to infer used licenses through META-INF/LICENSE*
                        val metadata =
                            ResolvedLocalFileMetadata(
                                displayName = file.nameWithoutExtension,
                                licenses =
                                    licenseCandidates.mapIndexed { i, path ->
                                        LicenseSeed(
                                            name = "${file.nameWithoutExtension}-$i",
                                            url = path,
                                        )
                                    },
                            )

                        key to
                            ResolvedArtifact(
                                id = id,
                                metadata = metadata,
                            )
                    }
                }

        return (
            resolveResults.resolvedComponents.flatMap { result ->
                result.getArtifacts(MavenPomArtifact::class.java).map {
                    it as ResolvedArtifactResult

                    val pomFile = PomParser(it.file).parse()
                    val component = it.id.componentIdentifier

                    when (val e = scopedModuleSeq.filter { (_, modules) -> component in modules }.firstOrNull()) {
                        null -> {
                            val fragments = it.id.displayName.split(":")

                            val (group, name, version) =
                                when (fragments.size) {
                                    0 -> arrayOf("unknown", "unknown", "unknown")
                                    1 -> arrayOf(fragments[0], "unknown", "unknown")
                                    2 -> arrayOf(fragments[0], fragments[1], "unknown")
                                    else -> arrayOf(fragments[0], fragments[1], fragments[2])
                                }

                            val id =
                                ResolvedModuleIdentifier(
                                    group = group,
                                    name = name,
                                    version = VersionString(version),
                                    id = it.id,
                                )

                            ResolveScope.Unknown to
                                ResolvedArtifact(
                                    id = id,
                                    metadata = pomFile,
                                )
                        }
                        else -> {
                            val (scope, modules) = e

                            scope to
                                ResolvedArtifact(
                                    id = modules.getValue(component),
                                    metadata = pomFile,
                                )
                        }
                    }
                }
            } + scopedLocalFiles
        ).collectToMap().toSortedMap(
            Comparator { o1, o2 ->
                allScopes.indexOf(o1).compareTo(allScopes.indexOf(o2))
            },
        )
    }

    /**
     * Return all configurations based the given scopes.
     *
     * @return all configurations that we should resolve but some of them might be *ancestor* configuration.
     */
    private fun Project.allConfigurations(
        variantScope: ResolveScope.Variant,
        additionalScope: ResolveScope? = null,
    ): List<Configuration> {
        // Gradle 9: Configuration naming convention changed
        // - test scope -> UnitTest suffix (e.g., yellowBlueReleaseUnitTestRuntimeClasspath)
        // - androidTest scope -> AndroidTest suffix (e.g., yellowBlueDebugAndroidTestRuntimeClasspath)
        //   Note: androidTest only exists for debug builds, so we need to replace Release with Debug
        val scopeSuffix =
            when (additionalScope?.name?.lowercase()) {
                "test" -> "UnitTest"
                "androidtest" -> "AndroidTest"
                else -> additionalScope?.name ?: ""
            }

        // For androidTest, we need to use Debug build type instead of Release
        // because androidTest configurations only exist for debug variants
        val variantPrefix =
            if (additionalScope?.name?.lowercase() == "androidtest") {
                variantScope.name
                    .replace("Release", "Debug", ignoreCase = true)
                    .replaceFirstChar { it.lowercaseChar() }
            } else {
                variantScope.name.replaceFirstChar { it.lowercaseChar() }
            }

        val targetConfigurationNames =
            if (scopeSuffix.isNotEmpty()) {
                configurationNames.map { name ->
                    variantPrefix + scopeSuffix + name
                }
            } else {
                configurationNames.map { name ->
                    variantPrefix + name
                } + configurationNames
            }

        return project.configurations
            .filter { configuration ->
                (configuration.name in targetConfigurationNames).also { isTarget ->
                    if (isTarget) {
                        project.logger.info("Configuration(name = ${configuration.name}) will be search")
                    }
                }
            }
    }

    /**
     * A helper to do grouping, merging, and sorting resolved module ids
     *
     * @return well-sorted and merged map
     */
    private fun List<ResolvedModuleIdentifier>.groupedSortedDistinct(): SortedMap<Pair<String, String>, ResolvedModuleIdentifier> =
        groupBy { id ->
            id.group to id.name
        }.mapValues {
            it.value.reduce { acc, id ->
                acc.copy(
                    version = arrayOf(acc.version, id.version).max()!!,
                )
            }
        }.toSortedMap(
            Comparator { o1, o2 ->
                requireNotNull(o1)
                requireNotNull(o2)

                o1.first
                    .compareTo(o2.first)
                    .takeIf { it != 0 } ?: o1.second.compareTo(o2.second)
            },
        )

    /**
     * Transform artifacts that the given Gradle's dependency configuration provides to the Model class of this plugin
     *
     * Uses capabilitiesResolution to prefer JVM variants for KMP libraries, which provides
     * compatibility with Gradle 8.x behavior.
     */
    internal fun Configuration.toResolvedModuleIdentifiers(): List<ResolvedModuleIdentifier> {
        if (!isCanBeResolved) {
            LicenseListPlugin.logger?.debug("Configuration $name is not resolvable, skipping")
            return emptyList()
        }

        LicenseListPlugin.logger?.info("Resolving configuration: $name with capabilities resolution")

        val artifactTypeAttr = Attribute.of("artifactType", String::class.java)

        // Create a unique configuration name to avoid conflicts
        val configName = "licenseListResolve_${name}_${System.currentTimeMillis()}"

        val resolveConfig: Configuration
        try {
            resolveConfig =
                project.configurations.create(configName) {
                    // Extend from the original RuntimeClasspath to inherit all dependencies
                    extendsFrom(this@toResolvedModuleIdentifiers)

                    // This configuration is only for resolution, not consumption
                    isCanBeResolved = true
                    isCanBeConsumed = false

                    // Configure capabilities resolution to prefer non-Android variants
                    resolutionStrategy {
                        capabilitiesResolution.all {
                            val candidates = candidates
                            if (candidates.size > 1) {
                                // Find a non-Android candidate if available
                                val nonAndroidCandidate =
                                    candidates.firstOrNull { candidate ->
                                        val id = candidate.id.toString()
                                        !id.contains("-android") &&
                                            (id.contains("-jvm") || id.contains("-desktop") || !KMP_VARIANT_SUFFIXES.any { suffix -> id.contains(suffix) })
                                    }

                                if (nonAndroidCandidate != null) {
                                    LicenseListPlugin.logger?.info("Selecting ${nonAndroidCandidate.id} over Android variant")
                                    select(nonAndroidCandidate)
                                } else {
                                    // Fall back to selecting the first candidate
                                    select(candidates.first())
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            LicenseListPlugin.logger?.warn("Failed to create configuration: ${e.message}")
            return toResolvedModuleIdentifiersFallback()
        }

        val results = mutableListOf<ResolvedModuleIdentifier>()
        val seenComponents = mutableSetOf<String>()

        try {
            // Resolve JAR artifacts
            resolveConfig.incoming
                .artifactView {
                    attributes {
                        attribute(artifactTypeAttr, "jar")
                    }
                    lenient(true)
                }.artifacts
                .forEach { artifact ->
                    val componentId = artifact.id.componentIdentifier
                    if (componentId is ModuleComponentIdentifier) {
                        val key = "${componentId.group}:${componentId.module}:${componentId.version}"
                        if (key !in seenComponents) {
                            seenComponents.add(key)
                            if (exclusionPredicate?.invoke(componentId.group, componentId.module) != true) {
                                results.add(
                                    ResolvedModuleIdentifier(
                                        name = componentId.module,
                                        group = componentId.group,
                                        version = VersionString(componentId.version),
                                        id = artifact.id,
                                    ),
                                )
                            }
                        }
                    }
                }

            // Resolve AAR artifacts
            resolveConfig.incoming
                .artifactView {
                    attributes {
                        attribute(artifactTypeAttr, "aar")
                    }
                    lenient(true)
                }.artifacts
                .forEach { artifact ->
                    val componentId = artifact.id.componentIdentifier
                    if (componentId is ModuleComponentIdentifier) {
                        val key = "${componentId.group}:${componentId.module}:${componentId.version}"
                        if (key !in seenComponents) {
                            seenComponents.add(key)
                            if (exclusionPredicate?.invoke(componentId.group, componentId.module) != true) {
                                results.add(
                                    ResolvedModuleIdentifier(
                                        name = componentId.module,
                                        group = componentId.group,
                                        version = VersionString(componentId.version),
                                        id = artifact.id,
                                    ),
                                )
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            LicenseListPlugin.logger?.warn("Failed to resolve: ${e.message}")
            // Clean up and fall back
            try {
                project.configurations.remove(resolveConfig)
            } catch (_: Exception) {
            }
            return toResolvedModuleIdentifiersFallback()
        }

        // Clean up the temporary configuration
        try {
            project.configurations.remove(resolveConfig)
        } catch (e: Exception) {
            LicenseListPlugin.logger?.debug("Failed to remove temporary configuration: ${e.message}")
        }

        LicenseListPlugin.logger?.info("Resolved ${results.size} modules")

        return results
    }

    /**
     * Fallback resolution path when capabilities resolution fails
     */
    private fun Configuration.toResolvedModuleIdentifiersFallback(): List<ResolvedModuleIdentifier> {
        val artifactTypeAttr = Attribute.of("artifactType", String::class.java)
        val results = mutableListOf<ResolvedModuleIdentifier>()
        val seenComponents = mutableSetOf<String>()

        // Resolve jar artifacts
        try {
            val jarView =
                incoming.artifactView {
                    attributes {
                        attribute(artifactTypeAttr, "jar")
                    }
                    lenient(true)
                }
            jarView.artifacts.forEach { artifact ->
                val componentId = artifact.id.componentIdentifier
                if (componentId is ModuleComponentIdentifier) {
                    val key = "${componentId.group}:${componentId.module}:${componentId.version}"
                    if (key !in seenComponents) {
                        seenComponents.add(key)
                        if (exclusionPredicate?.invoke(componentId.group, componentId.module) != true) {
                            results.add(
                                ResolvedModuleIdentifier(
                                    name = componentId.module,
                                    group = componentId.group,
                                    version = VersionString(componentId.version),
                                    id = artifact.id,
                                ),
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LicenseListPlugin.logger?.debug("Failed to resolve jar artifacts for $name", e)
        }

        // Resolve aar artifacts
        try {
            val aarView =
                incoming.artifactView {
                    attributes {
                        attribute(artifactTypeAttr, "aar")
                    }
                    lenient(true)
                }
            aarView.artifacts.forEach { artifact ->
                val componentId = artifact.id.componentIdentifier
                if (componentId is ModuleComponentIdentifier) {
                    val key = "${componentId.group}:${componentId.module}:${componentId.version}"
                    if (key !in seenComponents) {
                        seenComponents.add(key)
                        if (exclusionPredicate?.invoke(componentId.group, componentId.module) != true) {
                            results.add(
                                ResolvedModuleIdentifier(
                                    name = componentId.module,
                                    group = componentId.group,
                                    version = VersionString(componentId.version),
                                    id = artifact.id,
                                ),
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LicenseListPlugin.logger?.debug("Failed to resolve aar artifacts for $name", e)
        }

        return results
    }
}
