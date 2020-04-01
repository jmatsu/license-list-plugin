package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.ext.collectToMap
import io.github.jmatsu.license.model.ResolveScope
import io.github.jmatsu.license.model.ResolvedArtifact
import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.PlainLicense
import io.github.jmatsu.license.poko.Scope
import java.util.SortedMap
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.list

class MergerableAssembler(
    scopedResolvedArtifacts: SortedMap<ResolveScope, List<ResolvedArtifact>>,
    private val licenseCapture: MutableSet<PlainLicense> = HashSet(),
    private val baseArtifacts: Set<ArtifactDefinition>,
    private val newArtifacts: Set<ArtifactDefinition>,
    private val removedArtifacts: Set<ArtifactDefinition>,
    private val baseLicenses: Set<PlainLicense>
) {
    val scopedArtifactDefinitions: Map<ResolveScope, List<ArtifactDefinition>> = scopedResolvedArtifacts.mapValues { (_, artifacts) ->
        artifacts.map { Assembler.assembleArtifact(it, licenseCapture) }
    }

    fun assembleArtifacts(style: Assembler.Style, format: StringFormat): String {
        return when (style) {
            Assembler.Style.Flatten -> {
                val removedKeys = removedArtifacts.map { it.key }

                Assembler.assembleFlatten(
                    format = format,
                    definitions = (baseArtifacts + newArtifacts).filterNot { it.key in removedKeys }.sorted()
                )
            }
            Assembler.Style.StructuredWithoutScope -> {
                val newKeys = newArtifacts.map { it.key }

                val assemblee = scopedArtifactDefinitions
                    .map { (_, definitions) ->
                        definitions.mergeAndSort(newKeys = newKeys, strongerDefinitions = baseArtifacts)
                    }.reduce { acc, map -> acc + map }.toSortedMap()

                Assembler.assembleStructuredWithoutScope(
                    format = format,
                    definitionMap = assemblee
                )
            }
            Assembler.Style.StructuredWithScope -> {
                val newKeys = newArtifacts.map { it.key }

                val assemblee = scopedArtifactDefinitions
                    .map { (scope, definitions) ->
                        Scope(scope.name) to definitions.mergeAndSort(newKeys = newKeys, strongerDefinitions = baseArtifacts)
                    }.toMap()

                Assembler.assembleStructuredWithScope(
                    format = format,
                    scopedDefinitionMap = assemblee
                )
            }
        }
    }

    fun assemblePlainLicenses(format: StringFormat): String {
        // assemble must be called in advance
        val baseLicenseMap = baseLicenses.groupBy { it.key }
        val newLicenses = licenseCapture.map { newLicense ->
            baseLicenseMap[newLicense.key]?.first() ?: newLicense
        }

        return format.stringify(PlainLicense.serializer().list, newLicenses.sortedBy { it.name })
    }

    fun List<ArtifactDefinition>.mergeAndSort(newKeys: List<String>, strongerDefinitions: Set<ArtifactDefinition>): Map<String, List<ArtifactDefinition>> {
        return map { definition ->
            val merged = if (definition.key in newKeys) {
                definition
            } else {
                strongerDefinitions.first { it.key == definition.key }
            }

            // destructure group:name
            merged.key.split(":")[0] to merged.copy(key = merged.key.split(":")[1])
        }.sortedBy { it.second }.sortedBy { it.first }.collectToMap()
    }
}
