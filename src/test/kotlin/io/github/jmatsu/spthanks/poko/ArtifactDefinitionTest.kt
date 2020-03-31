package io.github.jmatsu.spthanks.poko

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.RepeatedTest
import kotlin.test.expect

class ArtifactDefinitionTest {
    lateinit var json: Json

    @Before
    fun setup() {
        json = Json(configuration = JsonConfiguration.Stable)
    }

    @Test
    fun `serialize and deserialize ArtifactDefinition`() {
        val artifactDefinition = ArtifactDefinition(
                key = "key",
                displayName = "displayName",
                url = "url",
                licenses = listOf(
                        LicenseKey("license")
                ),
                copyrightHolders = listOf(
                        "copyrightHolder"
                )
        )

        val serialized = json.stringify(ArtifactDefinition.serializer(), artifactDefinition)

        // string comparision would be unstable because it depends on the order of the properties.
        expect(artifactDefinition) {
            json.parse(ArtifactDefinition.serializer(), serialized)
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
}