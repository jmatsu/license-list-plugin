package io.github.jmatsu.license.poko

import io.github.jmatsu.license.Factory.provideArtifact
import java.util.stream.Stream
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.expect
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

class ArtifactDefinitionTest {
    lateinit var json: Json

    @BeforeEach
    fun setup() {
        json = Json {  }
    }

    @Test
    fun `serialize ArtifactDefinition with keep`() {
        val artifactDefinition = provideArtifact(key = "key").copy(keep = true)

        val serialized = json.encodeToString(ArtifactDefinition.serializer(), artifactDefinition)

        assertTrue(serialized.contains(""""keep":true"""))
    }

    @Test
    fun `serialize ArtifactDefinition without keep`() {
        val artifactDefinition = provideArtifact(key = "key")

        val serialized = json.encodeToString(ArtifactDefinition.serializer(), artifactDefinition)

        assertFalse(serialized.contains(""""keep":"""))
    }

//    @ImplicitReflectionSerializer
    @ValueSource(
        booleans = [true, false]
    )
    @ParameterizedTest
    fun `deserialize ArtifactDefinition`(keep: Boolean) {
        val artifactDefinition = provideArtifact(key = "key").copy(
            displayName = "displayName",
            url = "url",
            licenses = listOf(LicenseKey("license")),
            copyrightHolders = listOf("copyrightHolder"),
            keep = keep
        )

        val jsonString = """
            {
                "key": "key",
                "displayName": "displayName",
                "url": "url",
                "licenses": ["license"],
                "copyrightHolders": ["copyrightHolder"],
                "keep": $keep
            }
        """.trimIndent()

        expect(artifactDefinition) {
            json.decodeFromString(ArtifactDefinition.serializer(), jsonString)
        }
    }

    // to ensure total order
    @RepeatedTest(value = 10)
    fun `comparator`() {
        val definition = provideArtifact(key = "key")

        expect(listOf("a:a", "b:b", "c:c", "com.example:xyz", "com.example.abc:xyz", "com.example0:xyz")) {
            listOf(
                definition.copy(key = "com.example.abc:xyz"),
                definition.copy(key = "b:b"),
                definition.copy(key = "c:c"),
                definition.copy(key = "com.example0:xyz"),
                definition.copy(key = "a:a"),
                definition.copy(key = "com.example:xyz")
            ).shuffled().sorted().map { it.key }
        }
    }

    @MethodSource("provideArtifactDefinitions")
    @ParameterizedTest
    fun `serialize and deserialize ArtifactDefinition`(artifactDefinition: ArtifactDefinition) {
        val json = Json { }

        val serialized = json.encodeToString(ArtifactDefinition.serializer(), artifactDefinition)

        // string comparision would be unstable because it depends on the order of the properties.
        expect(artifactDefinition) {
            json.decodeFromString(ArtifactDefinition.serializer(), serialized)
        }
    }

    companion object {
        @JvmStatic
        fun provideArtifactDefinitions(): Stream<ArtifactDefinition> {
            return Stream.of(
                provideArtifact(key = "key").copy(keep = true),
                provideArtifact(key = "key").copy(
                    keep = false,
                    displayName = "any"
                ),
                provideArtifact(key = "key").copy(
                    keep = true,
                    url = null
                ),
                provideArtifact(key = "key").copy(
                    keep = true,
                    licenses = listOf(),
                    copyrightHolders = listOf()
                )
            )
        }
    }
}
