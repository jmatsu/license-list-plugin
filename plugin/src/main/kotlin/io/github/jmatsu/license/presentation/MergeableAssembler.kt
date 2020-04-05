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

class MergeableAssembler(
    scopedResolvedArtifacts: SortedMap<ResolveScope, List<ResolvedArtifact>>,
    private val baseLicenses: Set<PlainLicense>,
    private val scopedBaseArtifacts: Map<Scope, List<ArtifactDefinition>>
) : MergeStrategy {
    private val licenseCapture: MutableSet<PlainLicense> = HashSet()
    private val baseArtifacts: Set<ArtifactDefinition> by lazy {
        scopedBaseArtifacts.flatMap { (_, xs) -> xs }.toSet()
    }
    private val scopedArtifacts by lazy {
        scopedResolvedArtifacts.mapValues { (_, artifacts) ->
            artifacts.map { Assembler.assembleArtifact(it, licenseCapture = licenseCapture) }
        }
    }
    private val artifactDiff: Diff.DiffResult by lazy {
        Diff.calculateForArtifact(baseArtifacts, newer = scopedArtifacts.values.flatten())
    }

    // TODO support changed artifacts? : what's the usecase?
    private val willBeSavedArtifacts: List<ArtifactDefinition> by lazy {
        baseArtifacts.filter { it.key !in artifactDiff.willBeRemovedKeys }.reverseMerge(scopedArtifacts.values.flatten().toSet()) { it.key }
    }

    fun assembleArtifacts(style: Assembler.Style, format: StringFormat): String {
        return when (style) {
            Assembler.Style.Flatten -> {
                Assembler.assembleFlatten(
                    format = format,
                    definitions = willBeSavedArtifacts.sorted()
                )
            }
            Assembler.Style.StructuredWithoutScope -> {
                assert(scopedBaseArtifacts.keys.size == 1)
                assert(scopedBaseArtifacts.keys.first() === Scope.StubScope)

                val assemblee = willBeSavedArtifacts.collectToMapByArtifactGroup()

                Assembler.assembleStructuredWithoutScope(
                    format = format,
                    definitionMap = assemblee
                )
            }
            Assembler.Style.StructuredWithScope -> {
                val scopedArtifactKeys: Map<ResolveScope, List<String>> = scopedArtifacts.mapValues { (_, artifacts) -> artifacts.map { it.key } }

                val assemblee = scopedArtifactKeys
                    .map { (scope, keys) ->
                        val newScope = Scope(scope.name)

                        newScope to (
                            willBeSavedArtifacts.filter { it.key in keys } +
                                scopedBaseArtifacts[newScope].orEmpty().filter { it.key in artifactDiff.keepKeys }
                            ).collectToMapByArtifactGroup()
                    }.toMap()

                Assembler.assembleStructuredWithScope(
                    format = format,
                    scopedDefinitionMap = assemblee
                )
            }
        }
    }

    fun assemblePlainLicenses(format: StringFormat): String {
        val willBeSavedLicenseKeys = willBeSavedArtifacts.flatMap { it.licenses.map { it.value } }
        val willBeSavedLicenses = baseLicenses.filter { it.key.value in willBeSavedLicenseKeys }.reverseMerge(licenseCapture) { it.key }

        return format.stringify(PlainLicense.serializer().list, willBeSavedLicenses.distinctBy { it.key }.sortedBy { it.key.value })
    }
}

interface MergeStrategy {

    fun <T, R> List<T>.reverseMerge(definitions: Set<T>, keyExtractor: (T) -> R): List<T> {
        val keysToPreserve = map(keyExtractor)

        return toList() + definitions.filterNot { keyExtractor(it) in keysToPreserve }
    }

    fun List<ArtifactDefinition>.collectToMapByArtifactGroup(): Map<String, List<ArtifactDefinition>> {
        return map {
            // for safe split
            val (group, name) = "${it.key}:${it.key}".split(":")

            group to it.copy(key = name)
        }.sortedBy { it.second }.sortedBy { it.first }.collectToMap()
    }
}
