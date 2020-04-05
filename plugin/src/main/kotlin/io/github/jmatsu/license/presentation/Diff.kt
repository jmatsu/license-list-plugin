package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.LicenseKey
import org.gradle.util.ChangeListener
import org.gradle.util.DiffUtil

object Diff {
    data class DiffResult(
        val missingKeys: Set<String>,
        val keepKeys: Set<String>,
        val willBeRemovedKeys: Set<String>
    ) {
        fun hasDiff(): Boolean = missingKeys.isNotEmpty() || willBeRemovedKeys.isNotEmpty()
    }

    fun calculateForArtifact(base: Collection<ArtifactDefinition>, newer: Collection<ArtifactDefinition>): DiffResult {
        val baseKeys = base.map { it.key }.toSet()
        val newerKeys = newer.map { it.key }.toSet()

        val keepKeys = base.mapNotNull { a -> a.key.takeIf { a.keep } }.toSet()

        val added = HashSet<String>()
        val removed = HashSet<String>()

        DiffUtil.diff(newerKeys, baseKeys, object : ChangeListener<String> {
            override fun added(element: String) {
                added += element
            }

            override fun changed(element: String) {
                error("DiffUtil does not support changed because it's based on Set")
            }

            override fun removed(element: String) {
                if (element in keepKeys) {
                    return
                }

                removed += element
            }
        })

        return DiffResult(
            missingKeys = added,
            keepKeys = keepKeys,
            willBeRemovedKeys = removed
        )
    }

    fun calculateForLicense(base: Collection<LicenseKey>, newer: Collection<LicenseKey>): DiffResult {
        val baseKeys = base.map { it.value }.toSet()
        val newerKeys = newer.map { it.value }.toSet()

        val added = HashSet<String>()
        val removed = HashSet<String>()

        DiffUtil.diff(newerKeys, baseKeys, object : ChangeListener<String> {
            override fun added(element: String) {
                added += element
            }

            override fun changed(element: String) {
                error("DiffUtil does not support changed because it's based on Set")
            }

            override fun removed(element: String) {
                removed += element
            }
        })

        return DiffResult(
            missingKeys = added,
            keepKeys = emptySet(),
            willBeRemovedKeys = removed
        )
    }
}
