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
        fun assemble(artifact: ResolvedArtifact): ArtifactDefinition {
            return ArtifactDefinition(
                    key = "${artifact.id.group}:${artifact.id.name}",
                    licenses = artifact.pomFile.licenses(),
                    copyrightHolders = artifact.pomFile.copyrightHolders,
                    url = artifact.pomFile.associatedUrl,
                    displayName = artifact.pomFile.displayName
            )
        }

        fun assembleFlatten(format: StringFormat, definitions: List<ArtifactDefinition>): String {
            return format.stringify(ArtifactDefinition.serializer().list, definitions)
        }

        fun assembleStructuredWithoutScope(format: StringFormat, definitionMap: Map<String, List<ArtifactDefinition>>): String {
            val serializer = MapSerializer(String.serializer(), ArtifactDefinition.serializer().list)
            return format.stringify(serializer, definitionMap)
        }

        fun assembleStructuredWithScope(format: StringFormat, scopedDefinitionMap: Map<Scope, Map<String, List<ArtifactDefinition>>>): String {
            val serializer = MapSerializer(Scope.serializer(), MapSerializer(String.serializer(), ArtifactDefinition.serializer().list))
            return format.stringify(serializer, scopedDefinitionMap)
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

    sealed class Style {
        object Flatten : Style()
        object StructuredWithoutScope : Style()
        object StructuredWithScope : Style()
    }

    fun assemble(style: Style, format: StringFormat): String {
        return when (style) {
            Style.Flatten -> {
                assembleFlatten(format = format, definitions = transformForFlatten())
            }
            Style.StructuredWithoutScope -> {
                assembleStructuredWithoutScope(format = format, definitionMap = tranformForStructuredWithoutScope())
            }
            Style.StructuredWithScope -> {
                assembleStructuredWithScope(format = format, scopedDefinitionMap = transformForStructuredWithScope())
            }
        }
    }

    fun transformForFlatten(): List<ArtifactDefinition> {
        return resolvedArtifactMap.flatMap { (_, artifacts) ->
            artifacts.map { artifact ->
                assemble(artifact)
            }
        }.sorted()
    }

    fun tranformForStructuredWithoutScope(): Map<String, List<ArtifactDefinition>> {
        return resolvedArtifactMap.map { (_, artifacts) ->
            artifacts.map { artifact ->
                artifact.id.group to assemble(artifact).copy(key = artifact.id.name)
            }.sortedBy { it.second }.sortedBy { it.first }.collectToMap()
        }.reduce { acc, map -> acc + map }.toSortedMap()
    }

    fun transformForStructuredWithScope(): Map<Scope, Map<String, List<ArtifactDefinition>>> {
        return resolvedArtifactMap.map { (scope, artifacts) ->
            Scope(name = scope.name) to (artifacts.map { artifact ->
                artifact.id.group to assemble(artifact).copy(key = artifact.id.name)
            }.sortedBy { it.second }.sortedBy { it.first }.collectToMap())
        }.toMap()
    }
}