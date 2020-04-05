package io.github.jmatsu.license.poko

import java.util.stream.Stream
import kotlin.test.BeforeTest
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.expect
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class ArtifactDefinitionTest {
    lateinit var json: Json

    @BeforeEach
    @BeforeTest
    fun setup() {
        json = Json(configuration = JsonConfiguration.Stable)
    }

    @Test
    fun `serialize ArtifactDefinition with keep`() {
        val artifactDefinition = ArtifactDefinition(
            key = "key",
            displayName = "displayName",
            url = "url",
            licenses = listOf(
                LicenseKey("license")
            ),
            copyrightHolders = listOf(
                "copyrightHolder"
            ),
            keep = true
        )

        val serialized = json.stringify(ArtifactDefinition.serializer(), artifactDefinition)

        assertTrue(serialized.contains(""""keep":true"""))
    }

    @Test
    fun `serialize ArtifactDefinition without keep`() {
        val artifactDefinition = ArtifactDefinition(
            key = "key",
            displayName = "displayName",
            url = "url",
            licenses = listOf(
                LicenseKey("license")
            ),
            copyrightHolders = listOf(
                "copyrightHolder"
            ),
            keep = false
        )

        val serialized = json.stringify(ArtifactDefinition.serializer(), artifactDefinition)

        assertFalse(serialized.contains(""""keep":"""))
    }

    @ImplicitReflectionSerializer
    @ValueSource(
        booleans = [true, false]
    )
    @ParameterizedTest
    fun `deserialize ArtifactDefinition`(keep: Boolean) {
        val artifactDefinition = ArtifactDefinition(
            key = "key",
            displayName = "displayName",
            url = "url",
            licenses = listOf(
                LicenseKey("license")
            ),
            copyrightHolders = listOf(
                "copyrightHolder"
            ),
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
            json.parse(ArtifactDefinition.serializer(), jsonString)
        }
    }

    // to ensure total order
    @RepeatedTest(value = 10)
    fun `comparator`() {
        val definition = ArtifactDefinition(
            key = "key",
            displayName = "displayName",
            url = null,
            licenses = listOf(),
            copyrightHolders = listOf()
        )

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
        val json = Json(configuration = JsonConfiguration.Stable)

        val serialized = json.stringify(ArtifactDefinition.serializer(), artifactDefinition)

        // string comparision would be unstable because it depends on the order of the properties.
        expect(artifactDefinition) {
            json.parse(ArtifactDefinition.serializer(), serialized)
        }
    }

    companion object {
        @JvmStatic
        fun provideArtifactDefinitions(): Stream<ArtifactDefinition> {
            return Stream.of(
                ArtifactDefinition(
                    key = "key",
                    displayName = "displayName",
                    url = "url",
                    licenses = listOf(
                        LicenseKey("license")
                    ),
                    copyrightHolders = listOf(
                        "copyrightHolder"
                    ),
                    keep = true
                ),
                ArtifactDefinition(
                    key = "key",
                    displayName = "displayName",
                    url = "url",
                    licenses = listOf(
                        LicenseKey("license")
                    ),
                    copyrightHolders = listOf(
                        "copyrightHolder"
                    ),
                    keep = false
                ),
                ArtifactDefinition(
                    key = "key",
                    displayName = "displayName",
                    url = null,
                    licenses = listOf(
                        LicenseKey("license")
                    ),
                    copyrightHolders = listOf(
                        "copyrightHolder"
                    ),
                    keep = true
                ),
                ArtifactDefinition(
                    key = "key",
                    displayName = "displayName",
                    url = "url",
                    licenses = listOf(),
                    copyrightHolders = listOf(),
                    keep = true
                )
            )
        }
    }
}
