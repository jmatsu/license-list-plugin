package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.LicenseKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergeableAssemblerTest : MergeStrategy {

    @Test
    fun `mergeAll should append or update based on the given definitions`() {
        val definitions = listOf(
            provideArtifact(key = "key1"),
            provideArtifact(key = "key2").copy(
                displayName = "kept"
            )
        )

        val others = setOf(
            provideArtifact(key = "key2"),
            provideArtifact(key = "key3")
        )

        val mergedResult = definitions mergeAll others

        // preserve key1 in definitions
        assertEquals(definitions.first { it.key == "key1" }, mergedResult.first { it.key == "key1" })

        // use key2 in definitions
        assertEquals(definitions.first { it.key == "key2" }, mergedResult.first { it.key == "key2" })
        assertEquals("kept", mergedResult.first { it.key == "key2" }.displayName)

        // use key3 in others
        assertEquals(others.first { it.key == "key3" }, mergedResult.first { it.key == "key3" })
    }

    @Test
    fun `excludeAll should reject based on the given definitions but remain kept artifacts`() {
        val definitions = listOf(
            provideArtifact(key = "key1"),
            provideArtifact(key = "key2").copy(
                displayName = "kept",
                keep = true
            )
        )

        val others = setOf(
            provideArtifact(key = "key1"),
            provideArtifact(key = "key2")
        )

        val excludeResult = definitions excludeAll others

        // delete key1
        assertFalse(excludeResult.any { it.key == "key1" })

        // Keep key2 in definitions and don't use key2 in others
        assertEquals(definitions.first { it.key == "key2" }, excludeResult.first { it.key == "key2" })
    }

    @Test
    fun `collectToMapByArtifactGroup does not fail`() {
        val definitions = listOf(
            provideArtifact(key = "group:name"),
            provideArtifact(key = "safe-group")
        )

        val collectResult = definitions.collectToMapByArtifactGroup()

        assertEquals(collectResult, mapOf(
            "group" to listOf(
                provideArtifact(key = "name")
            ),
            "safe-group" to listOf(
                provideArtifact(key = "safe-group")
            )
        ))
    }

    private fun provideArtifact(key: String): ArtifactDefinition {
        return ArtifactDefinition(
            key = key,
            displayName = "displayName",
            url = "url",
            licenses = listOf(
                LicenseKey("license")
            ),
            copyrightHolders = listOf(
                "copyrightHolder"
            )
        )
    }
}
