package io.github.jmatsu.spthanks.poko

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.junit.Before
import org.junit.Test
import kotlin.test.expect

class LicenseTest {
    lateinit var json: Json

    @Before
    fun setup() {
        json = Json(configuration = JsonConfiguration.Stable)
    }

    @Test
    fun `serialize LicenseKey`() {
        val license = LicenseKey(
                value = "license"
        )

        expect("\"license\"") {
            json.stringify(LicenseKey.serializer(), license)
        }
    }

    @Test
    fun `deserialize LicenseKey`() {
        val expected = LicenseKey(
                value = "license"
        )

        expect(expected) {
            json.parse(LicenseKey.serializer(), "\"license\"")
        }
    }

    @Test
    fun `serialize PlainLicense`() {
        val license = PlainLicense(
                name = "name",
                url = "url",
                key = "key"
        )

        expect("""{"key":"key","name":"name","url":"url"}""") {
            json.stringify(PlainLicense.serializer(), license)
        }
    }

    @Test
    fun `deserialize PlainLicense`() {
        val expected = PlainLicense(
                name = "name",
                url = "url",
                key = "key"
        )

        expect(expected) {
            json.parse(PlainLicense.serializer(), """{ "name": "name", "url": "url", "key": "key" }""")
        }
    }
}
