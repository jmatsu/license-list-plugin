package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.poko.DisplayArtifact
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertSame
import kotlinx.serialization.StringFormat

class VisualizerTest {

    @Test
    fun `visualizeArtifacts uses format#stringify properly`() {
        val displayArtifacts: List<DisplayArtifact> = ArrayList()
        val expectedText = "expectedText"

        val visualizer = Visualizer(
            displayArtifacts = displayArtifacts
        )

        val format: StringFormat = mockk {
            every { stringify<List<DisplayArtifact>>(any(), any()) } returns expectedText
        }

        val actualText = visualizer.visualizeArtifacts(format)

        verify {
            // serializer will be returned new instances for every calls
            format.stringify(any(), displayArtifacts)
        }

        assertSame(expectedText, actualText)
    }
}
