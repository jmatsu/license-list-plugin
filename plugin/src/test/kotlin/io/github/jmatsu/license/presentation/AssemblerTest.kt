package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.Factory
import kotlin.test.Test
import kotlin.test.assertEquals

class AssemblerTest : StructuringStrategy {
    @Test
    fun `collectToMapByArtifactGroup does not fail`() {
        val definitions =
            listOf(
                Factory.provideArtifact(key = "group:name"),
                Factory.provideArtifact(key = "safe-group"),
            )

        val collectResult = definitions.collectToMapByArtifactGroup()

        assertEquals(
            collectResult,
            mapOf(
                "group" to
                    listOf(
                        Factory.provideArtifact(key = "name"),
                    ),
                "safe-group" to
                    listOf(
                        Factory.provideArtifact(key = "safe-group"),
                    ),
            ),
        )
    }
}
