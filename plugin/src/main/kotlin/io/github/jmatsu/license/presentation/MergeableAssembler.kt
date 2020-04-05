package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.ext.collectToMap
import io.github.jmatsu.license.ext.xor2
import io.github.jmatsu.license.model.ResolveScope
import io.github.jmatsu.license.model.ResolvedArtifact
import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.PlainLicense
import io.github.jmatsu.license.poko.Scope
import java.util.SortedMap
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.list

class MergeableAssembler(
    private val scopedResolvedArtifacts: SortedMap<ResolveScope, List<ResolvedArtifact>>,
    private val baseArtifacts: Set<ArtifactDefinition>,
    private val baseLicenses: Set<PlainLicense>
) : MergeStrategy {
    private val licenseCapture: MutableSet<PlainLicense> = HashSet()

    fun assembleArtifacts(style: Assembler.Style, format: StringFormat): String {
        val scopedArtifacts = scopedResolvedArtifacts.mapValues { (_, artifacts) ->
            artifacts.map { Assembler.assembleArtifact(it, licenseCapture = licenseCapture) }
        }

        val scopedArtifactKeys: Map<ResolveScope, List<String>> = scopedArtifacts.mapValues { (_, artifacts) -> artifacts.map { it.key } }

        // TODO support changed artifacts : what's the usecase?
        val xorResult = scopedArtifacts.values.flatten().xor2(old = baseArtifacts) { it.key }

        val missingArtifacts: Set<ArtifactDefinition> = xorResult.added
        val restArtifacts: Set<ArtifactDefinition> = xorResult.removed

        return when (style) {
            Assembler.Style.Flatten -> {
                Assembler.assembleFlatten(
                    format = format,
                    definitions = ((baseArtifacts.toList() mergeAll missingArtifacts) excludeAll restArtifacts).sorted()
                )
            }
            Assembler.Style.StructuredWithoutScope -> {
                val assemblee = scopedArtifactKeys
                    .map { (_, keys) ->
                        ((baseArtifacts.filter { it.key in keys } mergeAll missingArtifacts) excludeAll restArtifacts).collectToMapByArtifactGroup()
                    }.reduce { acc, map -> acc + map }.toSortedMap()

                Assembler.assembleStructuredWithoutScope(
                    format = format,
                    definitionMap = assemblee
                )
            }
            Assembler.Style.StructuredWithScope -> {
                val assemblee = scopedArtifactKeys
                    .map { (scope, keys) ->
                        Scope(scope.name) to ((baseArtifacts.filter { it.key in keys } mergeAll missingArtifacts) excludeAll restArtifacts).collectToMapByArtifactGroup()
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
        val preservedLicenses = baseLicenseMap.filterKeys { licenseKey -> baseArtifacts.any { licenseKey in it.licenses } }.flatMap { it.value }

        return format.stringify(PlainLicense.serializer().list, (newLicenses + preservedLicenses).distinctBy { it.key }.sortedBy { it.key.value })
    }
}

interface MergeStrategy {

    infix fun List<ArtifactDefinition>.mergeAll(definitions: Set<ArtifactDefinition>): List<ArtifactDefinition> {
        val keysToPreserve = map { it.key }

        return toList() + definitions.filterNot { it.key in keysToPreserve }
    }

    infix fun List<ArtifactDefinition>.excludeAll(definitions: Set<ArtifactDefinition>): List<ArtifactDefinition> {
        val keysToExclude = definitions.map { it.key }

        return filterNot { it.key in keysToExclude && !it.keep }
    }

    fun List<ArtifactDefinition>.collectToMapByArtifactGroup(): Map<String, List<ArtifactDefinition>> {
        return map {
            // for safe split
            val (group, name) = "${it.key}:${it.key}".split(":")

            group to it.copy(key = name)
        }.sortedBy { it.second }.sortedBy { it.first }.collectToMap()
    }
}
