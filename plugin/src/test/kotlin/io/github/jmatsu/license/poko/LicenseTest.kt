package io.github.jmatsu.license.poko

import kotlin.test.expect
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class LicenseTest {
    lateinit var json: Json

    @BeforeTest
    fun setup() {
        json = Json { }
    }

    @Test
    fun `serialize LicenseKey`() {
        val license = LicenseKey(
            value = "license"
        )

        expect("\"license\"") {
            json.encodeToString(LicenseKey.serializer(), license)
        }
    }

    @Test
    fun `deserialize LicenseKey`() {
        val expected = LicenseKey(
            value = "license"
        )

        expect(expected) {
            json.decodeFromString(LicenseKey.serializer(), "\"license\"")
        }
    }

    @Test
    fun `serialize PlainLicense`() {
        val license = PlainLicense(
            name = "name",
            url = "url",
            key = LicenseKey(value = "key")
        )

        expect("""{"key":"key","name":"name","url":"url"}""") {
            json.encodeToString(PlainLicense.serializer(), license)
        }
    }

    @Test
    fun `deserialize PlainLicense`() {
        val expected = PlainLicense(
            name = "name",
            url = "url",
            key = LicenseKey(value = "key")
        )

        expect(expected) {
            json.decodeFromString(PlainLicense.serializer(), """{ "name": "name", "url": "url", "key": "key" }""")
        }
    }
}
