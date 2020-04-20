package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.model.ResolveScope
import io.github.jmatsu.license.model.ResolvedArtifact
import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.PlainLicense
import io.github.jmatsu.license.poko.Scope
import java.util.SortedMap

class Merger(
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
            artifacts.map { Builder.transformToArtifact(it, licenseCapture = licenseCapture) }
        }
    }
    private val artifactDiff: Diff.DiffResult by lazy {
        Diff.calculateForArtifact(baseArtifacts, newer = scopedArtifacts.values.flatten())
    }

    // TODO support changed artifacts? : what's the usecase?
    private val willBeUsedArtifacts: List<ArtifactDefinition> by lazy {
        baseArtifacts.filter { it.key !in artifactDiff.willBeRemovedKeys }.reverseMerge(scopedArtifacts.values.flatten().toSet()) { it.key }
    }

    fun merge(): AssembleeData {
        val scopeNamedArtifactKeys: Map<String, List<String>> = scopedArtifacts.mapValues { (_, artifacts) -> artifacts.map { it.key } }.mapKeys { (scope, _) -> scope.name }
        val scopeNames = (scopeNamedArtifactKeys.keys + scopedBaseArtifacts.keys.map { it.name }).distinct()

        val mergedScopedArtifacts = scopeNames
            .map { name ->
                val newScope = Scope(name)
                val existingKeys = scopeNamedArtifactKeys[name].orEmpty()

                newScope to willBeUsedArtifacts.filter { it.key in existingKeys } +
                    scopedBaseArtifacts[newScope].orEmpty().filter { it.key in artifactDiff.keepKeys }
            }.filter { (_, artifacts) ->
                artifacts.isNotEmpty()
            }.toMap()

        val licensesKeysToBeUsed = willBeUsedArtifacts.flatMap { it.licenses.map { it.value } }
        val mergedLicenses = baseLicenses.filter { it.key.value in licensesKeysToBeUsed }.reverseMerge(licenseCapture) { it.key }

        return AssembleeData(
            scopedArtifacts = mergedScopedArtifacts,
            licenses = mergedLicenses
        )
    }
}

interface MergeStrategy {

    fun <T, R> List<T>.reverseMerge(definitions: Set<T>, keyExtractor: (T) -> R): List<T> {
        val keysToPreserve = map(keyExtractor)

        return toList() + definitions.filterNot { keyExtractor(it) in keysToPreserve }
    }
}
