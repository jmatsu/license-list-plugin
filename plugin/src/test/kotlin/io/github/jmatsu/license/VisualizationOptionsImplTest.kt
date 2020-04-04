package io.github.jmatsu.license

import io.github.jmatsu.license.dsl.HtmlFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Before
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class VisualizationOptionsImplTest {

    lateinit var options: VisualizationOptions

    @Before
    fun setup() {
        options = VisualizationOptionsImpl(
            name = "default"
        )
    }

    @Test
    fun `default options`() {
        with(options) {
            assertEquals(HtmlFormat, format)
            assertNull(htmlTemplateDir)
            assertNull(freeMakerVersion)
            assertNull(outputDir)
        }
    }

    @Test
    fun `format validation`() {
        assertDoesNotThrow {
            options.format = "json"
        }
        assertEquals("json", options.format)
        assertDoesNotThrow {
            options.format = "html"
        }
        assertEquals("html", options.format)
        assertThrows<IllegalStateException> {
            options.format = "yaml"
        }
    }

    @Test
    fun `freeMakerVersion validation`() {
        assertDoesNotThrow {
            options.freeMakerVersion = "2.8.30"
        }
        assertEquals("2.8.30", options.freeMakerVersion)
        assertThrows<IllegalArgumentException> {
            options.freeMakerVersion = "not a version"
        }
    }
}
