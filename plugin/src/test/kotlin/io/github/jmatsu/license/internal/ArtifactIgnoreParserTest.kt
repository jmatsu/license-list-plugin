package io.github.jmatsu.license.internal

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArtifactIgnoreParserTest {
    lateinit var parser: ArtifactIgnoreParser

    lateinit var ignoreFile: File

    @BeforeEach
    fun setup() {
        ignoreFile = File.createTempFile("prefix", "ignore")

        parser = ArtifactIgnoreParser(ignoreFile = ignoreFile)
    }

    @AfterEach
    fun cleanup() {
        ignoreFile.exists() && ignoreFile.delete()
    }

    @Nested
    @DisplayName("RegexIgnore")
    inner class RegexIgnore {
        @Test
        fun `predicate should be null unless ignoreFile exists`() {
            ignoreFile.delete()

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Regex)

            assertNull(predicate)
        }

        @Test
        fun `ignore specific artifact`() {
            ignoreFile.writeText(
                """
                specific:artifact
                """.trimIndent(),
            )

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Regex)

            assertNotNull(predicate)
            assertTrue(predicate("specific", "artifact"))
            assertFalse(predicate("specific", "artifac2"))
            assertFalse(predicate("specific2", "artifact"))
        }

        @Test
        fun `ignore group`() {
            ignoreFile.writeText(
                """
                ignore_group:.*
                """.trimIndent(),
            )

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Regex)

            assertNotNull(predicate)
            assertTrue(predicate("ignore_group", "one"))
            assertTrue(predicate("ignore_group", "two"))
            assertFalse(predicate("group", "ignore_group"))
        }

        @Test
        fun `ignore several artifacts`() {
            ignoreFile.writeText(
                """
                several:artifact-.*
                """.trimIndent(),
            )

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Regex)

            assertNotNull(predicate)
            assertTrue(predicate("several", "artifact-any"))
            assertTrue(predicate("several", "artifact-"))
            assertFalse(predicate("several", "artifact"))
            assertFalse(predicate("any", "artifact-any"))
        }

        @Test
        fun `ignore several groups`() {
            ignoreFile.writeText(
                """
                several-group-[^:]+:.+
                """.trimIndent(),
            )

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Regex)

            assertNotNull(predicate)
            assertTrue(predicate("several-group-any", "group"))
            assertTrue(predicate("several-group-other", "group"))
            assertTrue(predicate("several-group-other", "any"))
            assertFalse(predicate("several-group", "any"))
        }

        @Test
        fun `ignore artifacts that do not have the prefix`() {
            ignoreFile.writeText(
                """
                prefix-.*
                """.trimIndent(),
            )

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Regex)

            assertNotNull(predicate)
            assertTrue(predicate("prefix-", "artifact-any"))
            assertTrue(predicate("prefix-", "artifact-"))
            assertFalse(predicate("prefix", "artifact"))
            assertFalse(predicate("any", "artifact-any"))
        }

        @Test
        fun `work even if chomped file`() {
            ignoreFile.writeText(
                """
                specific:artifact
                """.trimIndent().trimEnd { it.isWhitespace() || it == '\n' || it == '\r' },
            )

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Regex)

            assertNotNull(predicate)
            assertTrue(predicate("specific", "artifact"))
            assertFalse(predicate("specific", "artifact2"))
            assertFalse(predicate("specific2", "artifact"))
        }

        @Test
        fun `ignore all examples at once`() {
            ignoreFile.writeText(
                """
                specific:artifact
                ignore_group:.*
                several:artifact-.*
                several-group-[^:]+:.+
                prefix-.*
                """.trimIndent(),
            )

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Regex)

            assertNotNull(predicate)

            assertTrue(predicate("specific", "artifact"))
            assertFalse(predicate("specific", "artifact2"))
            assertFalse(predicate("specific2", "artifact"))

            assertTrue(predicate("ignore_group", "one"))
            assertTrue(predicate("ignore_group", "two"))
            assertFalse(predicate("group", "ignore_group"))

            assertTrue(predicate("several", "artifact-any"))
            assertTrue(predicate("several", "artifact-"))
            assertFalse(predicate("several", "artifact"))
            assertFalse(predicate("any", "artifact-any"))

            assertTrue(predicate("several-group-any", "group"))
            assertTrue(predicate("several-group-other", "group"))
            assertTrue(predicate("several-group-other", "any"))
            assertFalse(predicate("several-group", "any"))

            assertTrue(predicate("prefix-", "artifact-any"))
            assertTrue(predicate("prefix-", "artifact-"))
            assertFalse(predicate("prefix", "artifact"))
        }
    }

    @Nested
    @DisplayName("GlobIgnore")
    inner class GlobIgnore {
        @Test
        fun `predicate should be null unless ignoreFile exists`() {
            ignoreFile.delete()

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Glob)

            assertNull(predicate)
        }

        @Test
        fun `ignore specific artifact`() {
            ignoreFile.writeText(
                """
                specific:artifact
                """.trimIndent(),
            )

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Glob)

            assertNotNull(predicate)
            assertTrue(predicate("specific", "artifact"))
            assertFalse(predicate("specific", "artifac2"))
            assertFalse(predicate("specific2", "artifact"))
        }

        @Test
        fun `ignore group`() {
            ignoreFile.writeText(
                """
                ignore_group:*
                """.trimIndent(),
            )

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Glob)

            assertNotNull(predicate)
            assertTrue(predicate("ignore_group", "one"))
            assertTrue(predicate("ignore_group", "two"))
            assertFalse(predicate("group", "ignore_group"))
        }

        @Test
        fun `ignore several artifacts`() {
            ignoreFile.writeText(
                """
                several:artifact-*
                """.trimIndent(),
            )

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Glob)

            assertNotNull(predicate)
            assertTrue(predicate("several", "artifact-any"))
            assertTrue(predicate("several", "artifact-"))
            assertFalse(predicate("several", "artifact"))
            assertFalse(predicate("any", "artifact-any"))
        }

        @Test
        fun `ignore several groups`() {
            ignoreFile.writeText(
                """
                several-group-*:*
                """.trimIndent(),
            )

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Glob)

            assertNotNull(predicate)
            assertTrue(predicate("several-group-any", "group"))
            assertTrue(predicate("several-group-other", "group"))
            assertTrue(predicate("several-group-other", "any"))
            assertFalse(predicate("several-group", "any"))
        }

        @Test
        fun `ignore artifacts that do not have the prefix`() {
            ignoreFile.writeText(
                """
                prefix-**
                """.trimIndent(),
            )

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Glob)

            assertNotNull(predicate)
            assertTrue(predicate("prefix-", "artifact-any"))
            assertTrue(predicate("prefix-", "artifact-"))
            assertFalse(predicate("prefix", "artifact"))
            assertFalse(predicate("any", "artifact-any"))
        }

        @Test
        fun `work even if chomped file`() {
            ignoreFile.writeText(
                """
                specific:artifact
                """.trimIndent().trimEnd { it.isWhitespace() || it == '\n' || it == '\r' },
            )

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Glob)

            assertNotNull(predicate)
            assertTrue(predicate("specific", "artifact"))
            assertFalse(predicate("specific", "artifact2"))
            assertFalse(predicate("specific2", "artifact"))
        }

        @Test
        fun `ignore all examples at once`() {
            ignoreFile.writeText(
                """
                specific:artifact
                ignore_group:*
                several:artifact-*
                several-group-*:*
                prefix-**
                """.trimIndent(),
            )

            val predicate = parser.buildPredicate(ArtifactIgnoreParser.Format.Glob)

            assertNotNull(predicate)

            assertTrue(predicate("specific", "artifact"))
            assertFalse(predicate("specific", "artifact2"))
            assertFalse(predicate("specific2", "artifact"))

            assertTrue(predicate("ignore_group", "one"))
            assertTrue(predicate("ignore_group", "two"))
            assertFalse(predicate("group", "ignore_group"))

            assertTrue(predicate("several", "artifact-any"))
            assertTrue(predicate("several", "artifact-"))
            assertFalse(predicate("several", "artifact"))
            assertFalse(predicate("any", "artifact-any"))

            assertTrue(predicate("several-group-any", "group"))
            assertTrue(predicate("several-group-other", "group"))
            assertTrue(predicate("several-group-other", "any"))
            assertFalse(predicate("several-group", "any"))

            assertTrue(predicate("prefix-", "artifact-any"))
            assertTrue(predicate("prefix-", "artifact-"))
            assertFalse(predicate("prefix", "artifact"))
        }
    }
}
