package io.github.jmatsu.license

import io.github.jmatsu.license.dsl.StructuredStyle
import io.github.jmatsu.license.dsl.YamlFormat
import io.github.jmatsu.license.internal.ArtifactManagement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class AssemblyOptionsImplTest {

    lateinit var options: AssemblyOptions

    @Before
    fun setup() {
        options = AssemblyOptionsImpl(
            name = "default"
        )
    }

    @Test
    fun `default options`() {
        with(options) {
            assertEquals(StructuredStyle, style)
            assertEquals(YamlFormat, format)
            assertTrue(groupByScopes)
            assertEquals(ArtifactManagement.CommonConfigurationNames, targetConfigurations)
            assertEquals(
                setOf(
                    "test",
                    "androidTest"
                ), additionalScopes
            )
            assertNull(exclusionFile)
        }
    }

    @Test
    fun `format validation`() {
        assertDoesNotThrow {
            options.format = "json"
        }
        assertEquals("json", options.format)
        assertDoesNotThrow {
            options.format = "yaml"
        }
        assertEquals("yaml", options.format)
        assertThrows<IllegalStateException> {
            options.format = "html"
        }
    }

    @Test
    fun `style validation`() {
        assertDoesNotThrow {
            options.style = "flatten"
        }
        assertEquals("flatten", options.style)
        assertDoesNotThrow {
            options.style = "structured"
        }
        assertEquals("structured", options.style)
        assertThrows<IllegalStateException> {
            options.style = "well structured"
        }
    }

    @Test
    fun `additionalScopes is appendable`() {
        options.additionalScopes += setOf("abc", "xyz")

        with(options) {
            assertEquals(
                setOf(
                    "test",
                    "androidTest",
                    "abc",
                    "xyz"
                ), additionalScopes
            )
        }
    }

    @Test
    fun `additionalScopes is removable`() {
        options.additionalScopes -= setOf("test")

        with(options) {
            assertEquals(
                setOf(
                    "androidTest"
                ), additionalScopes
            )
        }
    }

    @Test
    fun `targetConfigurations validation`() {
        assertThrows<IllegalArgumentException> {
            options.targetConfigurations = emptySet()
        }
    }

    @Test
    fun `targetConfigurations is appendable`() {
        options.targetConfigurations += setOf("abc", "xyz")

        with(options) {
            assertEquals(
                setOf("api",
                    "compile",
                    "compileOnly",
                    "implementation",
                    "annotationProcessor",
                    "kapt",
                    "abc",
                    "xyz"
                ), targetConfigurations
            )
        }
    }

    @Test
    fun `targetConfigurations is removable`() {
        options.targetConfigurations += setOf("api", "compile")

        with(options) {
            assertEquals(
                ArtifactManagement.CommonConfigurationNames +
                    setOf(
                        "compileOnly",
                        "implementation",
                        "annotationProcessor",
                        "kapt"
                    ), targetConfigurations
            )
        }
    }
}
