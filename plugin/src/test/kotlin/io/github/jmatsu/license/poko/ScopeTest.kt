package io.github.jmatsu.license.poko

import kotlin.test.expect
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class ScopeTest {
    lateinit var json: Json

    @BeforeTest
    fun setup() {
        json = Json { }
    }

    @Test
    fun `serialize Scope`() {
        val scope = Scope(
            name = "scope"
        )

        expect("\"scope\"") {
            json.encodeToString(Scope.serializer(), scope)
        }
    }

    @Test
    fun `deserialize LicenseKey`() {
        val expected = Scope(
            name = "scope"
        )

        expect(expected) {
            json.decodeFromString(Scope.serializer(), "\"scope\"")
        }
    }
}
