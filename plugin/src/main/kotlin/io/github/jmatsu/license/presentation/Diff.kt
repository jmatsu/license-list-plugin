package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.internal.ArtifactManagement
import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.LicenseKey

object Diff {
    data class DiffResult(
        val missingKeys: Set<String>,
        val keepKeys: Set<String>,
        val willBeRemovedKeys: Set<String>,
    ) {
        fun hasDiff(): Boolean = missingKeys.isNotEmpty() || willBeRemovedKeys.isNotEmpty()
    }

    fun calculateForArtifact(
        base: Collection<ArtifactDefinition>,
        newer: Collection<ArtifactDefinition>,
    ): DiffResult {
        val baseKeys = base.map { it.key }.toSet()
        val newerKeys = newer.map { it.key }.toSet()

        // Also create normalized keys for KMP variant matching
        val newerNormalizedKeys = newerKeys.map { ArtifactManagement.normalizeArtifactName(it) }.toSet()

        val keepKeys = base.mapNotNull { a -> a.key.takeIf { a.keep } }.toSet()

        val added = newerKeys - baseKeys
        // When checking for removal, also consider normalized KMP names
        // A base key like "annotation" should not be removed if "annotation-jvm" exists in newer
        val removed =
            (baseKeys - newerKeys)
                .filterNot { baseKey ->
                    keepKeys.contains(baseKey) ||
                        // Check if there's a newer key that normalizes to the same base key
                        newerNormalizedKeys.contains(baseKey) ||
                        // Check if the base key (when normalized) matches any normalized newer key
                        newerNormalizedKeys.contains(ArtifactManagement.normalizeArtifactName(baseKey))
                }.toSet()

        return DiffResult(
            missingKeys = added,
            keepKeys = keepKeys,
            willBeRemovedKeys = removed,
        )
    }

    fun calculateForLicense(
        base: Collection<LicenseKey>,
        newer: Collection<LicenseKey>,
    ): DiffResult {
        val baseKeys = base.map { it.value }.toSet()
        val newerKeys = newer.map { it.value }.toSet()

        val added = newerKeys - baseKeys
        val removed = baseKeys - newerKeys

        return DiffResult(
            missingKeys = added,
            keepKeys = emptySet(),
            willBeRemovedKeys = removed,
        )
    }
}
