package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.internal.ArtifactManagement
import io.github.jmatsu.license.model.ResolveScope
import io.github.jmatsu.license.model.ResolvedArtifact
import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.PlainLicense
import io.github.jmatsu.license.poko.Scope
import java.util.SortedMap

class Merger(
    scopedResolvedArtifacts: SortedMap<ResolveScope, List<ResolvedArtifact>>,
    private val baseLicenses: Set<PlainLicense>,
    private val scopedBaseArtifacts: Map<Scope, List<ArtifactDefinition>>,
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

    // Build a map from normalized key to existing base keys for KMP variant matching
    private val normalizedToBaseKeys: Map<String, Set<String>> by lazy {
        baseArtifacts
            .groupBy { ArtifactManagement.normalizeArtifactName(it.key) }
            .mapValues { (_, artifacts) -> artifacts.map { it.key }.toSet() }
    }

    // Build a map from base key to its original scope
    private val baseKeyToScope: Map<String, Scope> by lazy {
        scopedBaseArtifacts.flatMap { (scope, artifacts) ->
            artifacts.map { it.key to scope }
        }.toMap()
    }

    // TODO support changed artifacts? : what's the usecase?
    private val willBeUsedArtifacts: List<ArtifactDefinition> by lazy {
        val filteredBase = baseArtifacts.filter { it.key !in artifactDiff.willBeRemovedKeys }
        filteredBase.reverseMerge(scopedArtifacts.values.flatten().toSet()) { it.key }
    }

    /**
     * Check if a base artifact key should be included based on detected keys.
     * This handles KMP variant matching - if "annotation-jvm" is detected,
     * "annotation" should also be included.
     */
    private fun shouldIncludeBaseKey(
        baseKey: String,
        detectedKeys: Set<String>,
    ): Boolean {
        // Direct match
        if (baseKey in detectedKeys) return true

        // Check if any detected key normalizes to this base key
        val normalizedBase = ArtifactManagement.normalizeArtifactName(baseKey)
        return detectedKeys.any { detected ->
            ArtifactManagement.normalizeArtifactName(detected) == normalizedBase
        }
    }

    fun merge(): AssembleeData {
        val scopeNamedArtifactKeys: Map<String, List<String>> = scopedArtifacts.mapValues { (_, artifacts) -> artifacts.map { it.key } }.mapKeys { (scope, _) -> scope.name }
        val allDetectedKeys = scopeNamedArtifactKeys.values.flatten().toSet()

        // Determine scope for each artifact, preferring existing scope
        val mergedScopedArtifacts = mutableMapOf<Scope, MutableList<ArtifactDefinition>>()

        // First, add all base artifacts that should be kept (in their original scope)
        for ((scope, artifacts) in scopedBaseArtifacts) {
            for (artifact in artifacts) {
                // Keep if it's a keep key, or if it matches (directly or via normalization) a detected key
                val shouldKeep =
                    artifact.key in artifactDiff.keepKeys ||
                        shouldIncludeBaseKey(artifact.key, allDetectedKeys)

                if (shouldKeep) {
                    mergedScopedArtifacts.getOrPut(scope) { mutableListOf() }.add(artifact)
                }
            }
        }

        // Then, add new artifacts that don't have a base equivalent
        val baseKeys = baseArtifacts.map { it.key }.toSet()
        val baseNormalizedKeys = baseKeys.map { ArtifactManagement.normalizeArtifactName(it) }.toSet()

        // Iterate directly over scopedArtifacts to avoid key type mismatch issues
        // (ResolveScope.Addition vs ResolveScope.Test/AndroidTest objects)
        for ((resolveScope, artifacts) in scopedArtifacts) {
            val scope = Scope(resolveScope.name)
            val newArtifacts = artifacts.filter { artifact ->
                // Only add if there's no existing base artifact for this (normalized) key
                val normalizedKey = ArtifactManagement.normalizeArtifactName(artifact.key)
                artifact.key !in baseKeys && normalizedKey !in baseNormalizedKeys
            }

            if (newArtifacts.isNotEmpty()) {
                mergedScopedArtifacts.getOrPut(scope) { mutableListOf() }.addAll(newArtifacts)
            }
        }

        // Convert to immutable map and filter empty scopes
        val finalScopedArtifacts =
            mergedScopedArtifacts
                .filter { (_, artifacts) -> artifacts.isNotEmpty() }
                .mapValues { (_, artifacts) -> artifacts.toList() }

        val licensesKeysToBeUsed = finalScopedArtifacts.flatMap { (_, artifacts) -> artifacts.flatMap { it.licenses.map { it.value } } }
        val mergedLicenses = baseLicenses.reverseMerge(licenseCapture) { it.key }.filter { it.key.value in licensesKeysToBeUsed }.fixLicenseUrl()

        return AssembleeData(
            scopedArtifacts = finalScopedArtifacts,
            licenses = mergedLicenses,
        )
    }

    /**
     * v0.7 introduced the breaking changes on license URLs. This hack fixes it.
     */
    private fun Iterable<PlainLicense>.fixLicenseUrl(): List<PlainLicense> =
        map { license ->
            if (license.url?.isFixTarget == true) {
                license.copy(url = license.url.replace("github.com", "raw.githubusercontent.com").replace("/blob", ""))
            } else {
                license
            }
        }

    private val String.isFixTarget: Boolean
        get() {
            return startsWith("https://github.com/jmatsu/license-list-plugin/blob/master/license-files/") ||
                this == "https://github.com/facebook/facebook-android-sdk/blob/master/LICENSE.txt"
        }
}

interface MergeStrategy {
    fun <T, R> Collection<T>.reverseMerge(
        definitions: Set<T>,
        keyExtractor: (T) -> R,
    ): List<T> {
        val keysToPreserve = map(keyExtractor)

        return toList() + definitions.filterNot { keyExtractor(it) in keysToPreserve }
    }
}
