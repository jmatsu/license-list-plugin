package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.Factory.provideArtifact
import io.github.jmatsu.license.Factory.provideLicenseKey
import kotlin.test.Test
import kotlin.test.assertEquals

class DiffTest {

    @Test
    fun calculateForArtifact() {
        val base = listOf(
            provideArtifact(key = "key1").copy(
                keep = true
            ),
            provideArtifact(key = "key2").copy(
                keep = false
            ),
            provideArtifact(key = "key3").copy(
                keep = false
            )
        )

        val newer = listOf(
            provideArtifact(key = "key2"),
            provideArtifact(key = "key4")
        )

        val result = Diff.calculateForArtifact(
            base = base,
            newer = newer
        )

        assertEquals(setOf("key1"), result.keepKeys)
        assertEquals(setOf("key3"), result.willBeRemovedKeys)
        assertEquals(setOf("key4"), result.missingKeys)
    }

    @Test
    fun calculateForLicense() {
        val base = listOf(
            provideLicenseKey(value = "key1"),
            provideLicenseKey(value = "key2"),
            provideLicenseKey(value = "key3")
        )

        val newer = listOf(
            provideLicenseKey(value = "key2"),
            provideLicenseKey(value = "key4")
        )

        val result = Diff.calculateForLicense(
            base = base,
            newer = newer
        )

        assertEquals(emptySet(), result.keepKeys)
        assertEquals(setOf("key1", "key3"), result.willBeRemovedKeys)
        assertEquals(setOf("key4"), result.missingKeys)
    }
}
