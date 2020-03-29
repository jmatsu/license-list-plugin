package io.github.jmatsu.spthanks.presentation

import com.charleskorn.kaml.YamlConfiguration
import io.github.jmatsu.spthanks.ext.collectToMap
import io.github.jmatsu.spthanks.internal.LicenseClassifier
import io.github.jmatsu.spthanks.model.ResolveScope
import io.github.jmatsu.spthanks.model.ResolvedArtifact
import io.github.jmatsu.spthanks.model.ResolvedPomFile
import io.github.jmatsu.spthanks.poko.*
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonConfiguration
import java.util.*
import com.charleskorn.kaml.Yaml as Kaml
import kotlinx.serialization.json.Json as KJson

class Assembler(
        private val resolvedArtifactMap: SortedMap<ResolveScope, List<ResolvedArtifact>>
) {
    companion object {
        val Yaml = Kaml(
                context = License.Serialization.module,
                configuration = YamlConfiguration(
                        strictMode = false
                )
        )
        val Json = KJson(
                context = License.Serialization.module,
                configuration = JsonConfiguration.Stable.copy(
                        ignoreUnknownKeys = true
                )
        )
    }

    sealed class Style {
        object Flatten : Style()
        object StructuredWithoutScope : Style()
        object StructuredWithScope : Style()
    }

    fun assemble(style: Style, format: StringFormat): String {
        return when (style) {
            Style.Flatten -> {
                format.stringify(ArtifactDefinition.serializer().list, assembleFlatten())
            }
            Style.StructuredWithoutScope -> {
                val serializer = MapSerializer(String.serializer(), ArtifactDefinition.serializer().list)
                format.stringify(serializer, assembleStructuredWithoutScope())
            }
            Style.StructuredWithScope -> {
                val serializer = MapSerializer(Scope.serializer(), MapSerializer(String.serializer(), ArtifactDefinition.serializer().list))
                format.stringify(serializer, assembleStructuredWithScope())
            }
        }
    }

    fun assembleFlatten(): List<ArtifactDefinition> {
        return resolvedArtifactMap.flatMap { (_, artifacts) ->
            artifacts.map { artifact ->
                ArtifactDefinition(
                        key = "${artifact.id.group}:${artifact.id.name}",
                        licenses = artifact.pomFile.licenses(),
                        copyrightHolders = artifact.pomFile.copyrightHolders,
                        url = artifact.pomFile.associatedUrl,
                        displayName = artifact.pomFile.displayName
                )
            }
        }.sorted()
    }

    fun assembleStructuredWithoutScope(): Map<String, List<ArtifactDefinition>> {
        return resolvedArtifactMap.flatMap { (_, artifacts) ->
            artifacts.map { artifact ->
                artifact.id.group to ArtifactDefinition(
                        key = artifact.id.name,
                        licenses = artifact.pomFile.licenses(),
                        copyrightHolders = artifact.pomFile.copyrightHolders,
                        url = artifact.pomFile.associatedUrl,
                        displayName = artifact.pomFile.displayName
                )
            }.sortedBy { it.second }.sortedBy { it.first }
        }.collectToMap().toSortedMap()
    }

    fun assembleStructuredWithScope(): Map<Scope, Map<String, List<ArtifactDefinition>>> {
        return resolvedArtifactMap.map { (scope, artifacts) ->
            Scope(name = scope.name) to (artifacts.map { artifact ->
                artifact.id.group to ArtifactDefinition(
                        key = artifact.id.name,
                        licenses = artifact.pomFile.licenses(),
                        copyrightHolders = artifact.pomFile.copyrightHolders,
                        url = artifact.pomFile.associatedUrl,
                        displayName = artifact.pomFile.displayName
                )
            }.sortedBy { it.second }.sortedBy { it.first }.collectToMap())
        }.toMap()
    }

    private fun ResolvedPomFile.licenses(): List<License> {
        return licenses.map {
            when (val guessedLicense = LicenseClassifier(it.name).guess()) {
                is LicenseClassifier.GuessedLicense.Undetermined -> {
                    PlainLicense(
                            name = it.name ?: guessedLicense.name,
                            url = it.url ?: guessedLicense.url
                    )
                }
                else -> LicenseKey(value = guessedLicense.key)
            }
        }
    }
}