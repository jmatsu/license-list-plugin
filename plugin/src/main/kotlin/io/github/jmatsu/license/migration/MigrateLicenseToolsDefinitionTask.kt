package io.github.jmatsu.license.migration

import com.charleskorn.kaml.Yaml
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.ext.collectToMap
import io.github.jmatsu.license.internal.LicenseClassifier
import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.LicenseKey
import io.github.jmatsu.license.poko.PlainLicense
import io.github.jmatsu.license.poko.Scope
import io.github.jmatsu.license.presentation.Convention
import kotlinx.serialization.builtins.ListSerializer
import java.io.File
import javax.inject.Inject
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.VisibleForTesting

abstract class MigrateLicenseToolsDefinitionTask
@Inject constructor(
    @get:Nested val extension: LicenseListExtension,
    @get:Internal val toolsExtension: Any
) : DefaultTask() {
    init {
        isEnabled = extension.isEnabled
    }

    @VisibleForTesting
    object Executor {

        operator fun invoke(
            targetVariant: String,
            artifactFile: File,
            licenseFile: File,
            artifactIgnoreFile: File,
            toolsLicenseFile: File,
            ignoredGroups: Set<String>
        ) {
            val toolsLicenses = Yaml.default.decodeFromString(ListSerializer(LibraryInfo.serializer()), toolsLicenseFile.readText())

            val keyToToolsLicenseMap = toolsLicenses.map {
                it.artifact.split(":").take(2).joinToString(":") to it
            }

            val ignoreKeys = keyToToolsLicenseMap.filter { it.second.skip }.map { it.first }.distinct()
            val keyToPlainLicenseMap = keyToToolsLicenseMap.filterNot {
                it.first in ignoreKeys || it.first.split(":").first() in ignoredGroups
            }.map { (key, l) ->
                val guessedLicense = LicenseClassifier(l.license).guess()

                val licenseKey = when (guessedLicense) {
                    is LicenseClassifier.GuessedLicense.Undetermined -> {
                        val name = l.license.takeIf { it.isNotBlank() } ?: guessedLicense.name
                        val url = l.licenseUrl.takeIf { it.isNotBlank() } ?: guessedLicense.url

                        LicenseKey(value = "$name@${url.length}")
                    }
                    else -> {
                        LicenseKey(value = guessedLicense.key)
                    }
                }

                key to PlainLicense(
                    key = licenseKey,
                    url = l.licenseUrl.takeIf { it.isNotBlank() } ?: guessedLicense.url,
                    name = guessedLicense.name
                )
            }.groupBy { it.first }.mapValues { it.value.map { it.second }.distinctBy { it.key.value } }

            val definitions = keyToToolsLicenseMap.filterNot { it.first in ignoreKeys }.map { (key, l) ->

                ArtifactDefinition(
                    key = key,
                    displayName = l.name.takeIf { it.isNotBlank() } ?: key,
                    url = l.url.takeIf { it.isNotBlank() },
                    copyrightHolders = (listOf(l.copyrightHolder) + l.copyrightHolders + l.author + listOf(l.author)).filter { it.isNotBlank() },
                    licenses = keyToPlainLicenseMap[key]?.map { it.key }.orEmpty(),
                    keep = l.forceGenerate
                )
            }.distinctBy { it.key }.sortedBy { it.key }

            val licenses = keyToPlainLicenseMap.values.flatten().distinctBy { it.key.value }.sortedBy { it.key.value }

            // fixed format
            val formatter = Convention.Yaml.Assembly

            val artifactMap = mapOf(Scope(targetVariant) to definitions.collectToMapByArtifactGroup())

            val serializer = MapSerializer(Scope.serializer(), MapSerializer(String.serializer(), ListSerializer(ArtifactDefinition.serializer())))
            artifactFile.writeText(formatter.encodeToString(serializer, artifactMap))
            licenseFile.writeText(formatter.encodeToString(ListSerializer(PlainLicense.serializer()), licenses))

            artifactIgnoreFile.writeText(
                (
                    ignoredGroups.map { it.replace(".", "\\.") }.map { "$it:.*" } +
                        ignoreKeys.map { it.replace(".", "\\.") }
                    ).joinToString("\n")
            )
        }

        fun List<ArtifactDefinition>.collectToMapByArtifactGroup(): Map<String, List<ArtifactDefinition>> {
            return map {
                // for safe split
                val (group, name) = "${it.key}:${it.key}".split(":")

                group to it.copy(key = name)
            }.sortedBy { it.second }.sortedBy { it.first }.collectToMap()
        }
    }

    @TaskAction
    fun execute() {
        val outputDir = File(project.buildDir, "license-list")

        outputDir.mkdirs()

        val artifactFile = File(outputDir, "artifact-definition.yml")
        val licenseFile = File(outputDir, "license-catalog.yml")
        val artifactIgnoreFile = File(outputDir, ".artifactignore")

        val fields = toolsExtension::javaClass.get().fields

        var toolsLicenseFile = fields.first { it.name == "licensesYaml" }.get(toolsExtension) as File
        @Suppress("UNCHECKED_CAST") val ignoredGroups = fields.first { it.name == "ignoredGroups" }.get(toolsExtension) as Set<String>

        if (!toolsLicenseFile.exists()) {
            error("${toolsLicenseFile.absolutePath} is not found. Please specify the filepath in licenseTools extension properly.")
        }

        logger.warn("The target license tools' file ${toolsLicenseFile.path}")

        Executor(
            targetVariant = extension.defaultVariant, // only this value is delivered from my extension
            artifactFile = artifactFile,
            licenseFile = licenseFile,
            artifactIgnoreFile = artifactIgnoreFile,
            toolsLicenseFile = toolsLicenseFile,
            ignoredGroups = ignoredGroups
        )
    }
}
