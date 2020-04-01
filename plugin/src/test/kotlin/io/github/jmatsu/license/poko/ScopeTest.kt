package io.github.jmatsu.license.poko

import kotlin.test.expect
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.junit.Before
import org.junit.Test

class ScopeTest {
    lateinit var json: Json

    @Before
    fun setup() {
        json = Json(configuration = JsonConfiguration.Stable)
    }

    @Test
    fun `serialize Scope`() {
        val scope = Scope(
            name = "scope"
        )

        expect("\"scope\"") {
            json.stringify(Scope.serializer(), scope)
        }
    }

    @Test
    fun `deserialize LicenseKey`() {
        val expected = Scope(
            name = "scope"
        )

        expect(expected) {
            json.parse(Scope.serializer(), "\"scope\"")
        }
    }
}
