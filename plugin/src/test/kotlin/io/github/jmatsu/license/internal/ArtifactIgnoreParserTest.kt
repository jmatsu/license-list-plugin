package io.github.jmatsu.license.internal

import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArtifactIgnoreParserTest {

    lateinit var parser: ArtifactIgnoreParser

    lateinit var ignoreFile: File

    @BeforeTest
    fun setup() {
        ignoreFile = File.createTempFile("prefix", "ignore")

        parser = ArtifactIgnoreParser(ignoreFile = ignoreFile)
    }

    @AfterTest
    fun cleanup() {
        ignoreFile.exists() && ignoreFile.delete()
    }

    @Test
    fun `Regex should be null unless ignoreFile exists`() {
        ignoreFile.delete()
        assertNull(parser.parse())
    }

    @Test
    fun `ignore specific artifact`() {
        ignoreFile.writeText("""
            specific:artifact
        """.trimIndent())

        val regex = parser.parse()

        assertNotNull(regex)
        assertTrue(regex.matches("specific:artifact"))
        assertFalse(regex.matches("specific:artifact2"))
        assertFalse(regex.matches("specific2:artifact"))
    }

    @Test
    fun `ignore group`() {
        ignoreFile.writeText("""
            ignore_group:.*
        """.trimIndent())

        val regex = parser.parse()

        assertNotNull(regex)
        assertTrue(regex.matches("ignore_group:one"))
        assertTrue(regex.matches("ignore_group:two"))
        assertFalse(regex.matches("group:ignore_group"))
    }

    @Test
    fun `ignore several artifacts`() {
        ignoreFile.writeText("""
            several:artifact-.*
        """.trimIndent())

        val regex = parser.parse()

        assertNotNull(regex)
        assertTrue(regex.matches("several:artifact-any"))
        assertTrue(regex.matches("several:artifact-"))
        assertFalse(regex.matches("several:artifact"))
        assertFalse(regex.matches("any:artifact-any"))
    }

    @Test
    fun `ignore several groups`() {
        ignoreFile.writeText("""
            several-group-[^:]+:.+
        """.trimIndent())

        val regex = parser.parse()

        assertNotNull(regex)
        assertTrue(regex.matches("several-group-any:group"))
        assertTrue(regex.matches("several-group-other:group"))
        assertTrue(regex.matches("several-group-other:any"))
        assertFalse(regex.matches("several-group:any"))
    }

    @Test
    fun `work even if chomped file`() {
        ignoreFile.writeText("""
            specific:artifact
        """.trimIndent().trimEnd { it.isWhitespace() || it == '\n' || it == '\r' })

        val regex = parser.parse()

        assertNotNull(regex)
        assertTrue(regex.matches("specific:artifact"))
        assertFalse(regex.matches("specific:artifact2"))
        assertFalse(regex.matches("specific2:artifact"))
    }

    @Test
    fun `ignore all examples at once`() {
        ignoreFile.writeText("""
            specific:artifact
            ignore_group:.*
            several:artifact-.*
            several-group-[^:]+:.+
        """.trimIndent())

        val regex = parser.parse()

        assertNotNull(regex)

        assertTrue(regex.matches("specific:artifact"))
        assertFalse(regex.matches("specific:artifact2"))
        assertFalse(regex.matches("specific2:artifact"))

        assertTrue(regex.matches("ignore_group:one"))
        assertTrue(regex.matches("ignore_group:two"))
        assertFalse(regex.matches("group:ignore_group"))

        assertTrue(regex.matches("several:artifact-any"))
        assertTrue(regex.matches("several:artifact-"))
        assertFalse(regex.matches("several:artifact"))
        assertFalse(regex.matches("any:artifact-any"))

        assertTrue(regex.matches("several-group-any:group"))
        assertTrue(regex.matches("several-group-other:group"))
        assertTrue(regex.matches("several-group-other:any"))
        assertFalse(regex.matches("several-group:any"))
    }
}
