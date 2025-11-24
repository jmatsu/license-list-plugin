package io.github.jmatsu.license.presentation

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

        val keepKeys = base.mapNotNull { a -> a.key.takeIf { a.keep } }.toSet()

        val added = newerKeys - baseKeys
        val removed = (baseKeys - newerKeys).filterNot { keepKeys.contains(it) }.toSet()

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
