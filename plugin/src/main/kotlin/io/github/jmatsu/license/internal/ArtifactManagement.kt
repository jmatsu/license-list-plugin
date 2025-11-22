package io.github.jmatsu.license.internal

import io.github.jmatsu.license.LicenseListPlugin
import io.github.jmatsu.license.ext.collectToMap
import io.github.jmatsu.license.ext.lenientConfiguration
import io.github.jmatsu.license.model.LicenseSeed
import io.github.jmatsu.license.model.ResolveScope
import io.github.jmatsu.license.model.ResolvedArtifact
import io.github.jmatsu.license.model.ResolvedLocalFileMetadata
import io.github.jmatsu.license.model.ResolvedModuleIdentifier
import io.github.jmatsu.license.model.VersionString
import io.github.jmatsu.license.model.localGroup
import java.io.File
import java.util.SortedMap
import java.util.zip.ZipFile
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.SelfResolvingDependency
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.kotlin.dsl.withArtifacts
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact

class ArtifactManagement(
    private val project: Project,
    private val configurationNames: Set<String>,
    private val exclusionPredicate: IgnorePredicate?
) {
    companion object {
        /**
         * Common configurations that will be used basically
         */
        val CommonConfigurationNames: Set<String> = setOf(
            // runtimeOnly
            "compileOnly",
            "implementation",
            "api",
            "compile",
            "annotationProcessor",
            "kapt"
        )
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
        additionalScopes: Set<ResolveScope.Addition>
    ): SortedMap<ResolveScope, List<ResolvedArtifact>> {
        val variantConfigurations = project.allConfigurations(variantScope)

        val scopedConfigurations = (listOf(variantScope to variantConfigurations) +
            additionalScopes.map { scope ->
                scope to project.allConfigurations(variantScope, additionalScope = scope)
            }).toMap()

        val components: MutableList<ComponentIdentifier> = ArrayList()

        val localFileMap: MutableMap<ResolveScope, List<File>> = hashMapOf()

        val scopedModules = scopedConfigurations.mapValues { (scope, configurations) ->
            configurations.flatMap { configuration ->
                runCatching {
                    localFileMap.putIfAbsent(scope, emptyList())
                    localFileMap[scope] = localFileMap.getValue(scope) + configuration.allDependencies
                        .filterIsInstance<SelfResolvingDependency>()
                        .flatMap {
                            it.resolve()
                        }
                        .filter { it.name.endsWith(".jar") || it.name.endsWith(".aar") }
                }.onFailure {
                    LicenseListPlugin.logger?.error("could not resolve local files in ${scope.name}", it)
                }

                val newResolvedIdentifiers = configuration.toResolvedModuleIdentifiers().filterNot { id ->
                    id.id?.componentIdentifier in components
                }

                components += newResolvedIdentifiers.mapNotNull { it.id?.componentIdentifier }.toSet()

                newResolvedIdentifiers
            }.groupedSortedDistinct()
                .mapKeys { it.value.id!!.componentIdentifier }
        }

        val resolveResults = project.dependencies.createArtifactResolutionQuery()
            .forComponents(scopedModules.flatMap { (_, modules) -> modules.keys })
            .withArtifacts(MavenModule::class, MavenPomArtifact::class)
            .execute()

        val scopedModuleSeq = scopedModules.asSequence()

        val allScopes = listOf(variantScope) + additionalScopes

        val scopedLocalFiles = localFileMap.values.flatten().distinct().groupBy { file ->
            localFileMap.asIterable().first { (_, files) -> file in files }.key
        }.flatMap { (key, files) ->
            files.mapNotNull { file ->
                val id = ResolvedModuleIdentifier(
                    group = localGroup,
                    name = file.name
                )

                if (exclusionPredicate?.invoke(id.group, id.name) == true) {
                    return@mapNotNull null
                }

                val licenseCandidates = ZipFile(file).use { zip ->
                    zip.entries().asSequence()
                        .filter { !it.isDirectory && it.name.startsWith("META-INF/") && it.name.contains("LICENSE") }
                        .map { it.name }
                        .toList()
                }

                // It's hard to infer used licenses through META-INF/LICENSE*
                val metadata = ResolvedLocalFileMetadata(
                    displayName = file.nameWithoutExtension,
                    licenses = licenseCandidates.mapIndexed { i, path ->
                        LicenseSeed(
                            name = "${file.nameWithoutExtension}-$i",
                            url = path
                        )
                    }
                )

                key to ResolvedArtifact(
                    id = id,
                    metadata = metadata
                )
            }
        }

        return (resolveResults.resolvedComponents.flatMap { result ->
            result.getArtifacts(MavenPomArtifact::class.java).map {
                it as ResolvedArtifactResult

                val pomFile = PomParser(it.file).parse()
                val component = it.id.componentIdentifier

                when (val e = scopedModuleSeq.filter { (_, modules) -> component in modules }.firstOrNull()) {
                    null -> {
                        val fragments = it.id.displayName.split(":")

                        val (group, name, version) = when (fragments.size) {
                            0 -> arrayOf("unknown", "unknown", "unknown")
                            1 -> arrayOf(fragments[0], "unknown", "unknown")
                            2 -> arrayOf(fragments[0], fragments[1], "unknown")
                            else -> arrayOf(fragments[0], fragments[1], fragments[2])
                        }

                        val id = ResolvedModuleIdentifier(
                            group = group,
                            name = name,
                            version = VersionString(version),
                            id = it.id
                        )

                        ResolveScope.Unknown to ResolvedArtifact(
                            id = id,
                            metadata = pomFile
                        )
                    }
                    else -> {
                        val (scope, modules) = e

                        scope to ResolvedArtifact(
                            id = modules.getValue(component),
                            metadata = pomFile
                        )
                    }
                }
            }
        } + scopedLocalFiles).collectToMap().toSortedMap(Comparator { o1, o2 ->
            allScopes.indexOf(o1).compareTo(allScopes.indexOf(o2))
        })
    }

    /**
     * Return all configurations based the given scopes.
     *
     * @return all configurations that we should resolve but some of them might be *ancestor* configuration.
     */
    private fun Project.allConfigurations(
        variantScope: ResolveScope.Variant,
        additionalScope: ResolveScope? = null
    ): List<Configuration> {
        val suffixes = if (additionalScope != null) {
            configurationNames.map { name ->
                additionalScope.name.decapitalize() + name.capitalize()
            }
        } else {
            configurationNames
        }

        val targetConfigurationNames = suffixes + suffixes.map { suffix ->
            variantScope.name.decapitalize() + suffix.capitalize()
        }

        return project.configurations.flatMap { it.getAllHierarchy() }.distinctBy { it.name }.filter { configuration ->
            (configuration.name in targetConfigurationNames).also { isTarget ->
                if (isTarget) {
                    project.logger.info("Configuration(name = ${configuration.name}) will be search")
                }
            }
        }
    }

    private fun Configuration.getAllHierarchy(): Set<Configuration> {
        val targets = mutableListOf(this)
        val results = mutableSetOf<Configuration>()

        while (targets.isNotEmpty()) {
            val c = targets.removeAt(0)

            if (results.add(c)) {
                targets.addAll(c.hierarchy)
            }
        }

        return results
    }

    /**
     * A helper to do grouping, merging, and sorting resolved module ids
     *
     * @return well-sorted and merged map
     */
    private fun List<ResolvedModuleIdentifier>.groupedSortedDistinct(): SortedMap<Pair<String, String>, ResolvedModuleIdentifier> {
        return groupBy { id ->
            id.group to id.name
        }.mapValues {
            it.value.reduce { acc, id ->
                acc.copy(
                    version = arrayOf(acc.version, id.version).max()!!
                )
            }
        }.toSortedMap(Comparator { o1, o2 ->
            requireNotNull(o1)
            requireNotNull(o2)

            o1.first.compareTo(o2.first)
                .takeIf { it != 0 } ?: o1.second.compareTo(o2.second)
        })
    }

    /**
     * Transform artifacts that the given Gradle's dependency configuration provides to the Model class of this plugin
     */
    internal fun Configuration.toResolvedModuleIdentifiers(): List<ResolvedModuleIdentifier> {
        return lenientConfiguration()?.run {
            artifacts.filter { it.type == "aar" || it.type == "jar" }
                .let {
                    if (exclusionPredicate != null) {
                        it.filterNot { artifact ->
                            exclusionPredicate.invoke(artifact.moduleVersion.id.group, artifact.moduleVersion.id.name)
                        }
                    } else {
                        it
                    }
                }
                .map { artifact ->
                    ResolvedModuleIdentifier(
                        name = artifact.moduleVersion.id.name,
                        group = artifact.moduleVersion.id.group,
                        version = VersionString(artifact.moduleVersion.id.version),
                        id = artifact.id
                    )
                }
        }.orEmpty()
    }
}
