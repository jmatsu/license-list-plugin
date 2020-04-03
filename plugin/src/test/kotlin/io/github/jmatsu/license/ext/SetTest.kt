package io.github.jmatsu.license.ext

import kotlin.test.expect
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class SetTest {

    @Test
    fun `combination invalid size`() {
        val target = setOf("x")

        assertThrows<IllegalStateException> {
            target.combination(-1)
        }
        assertThrows<IllegalStateException> {
            target.combination(2)
        }
    }

    @Test
    fun `combination n == 0`() {
        val target = setOf<String>()

        expect(setOf(linkedHashSetOf<String>())) {
            target.combination(0)
        }
    }

    @Test
    fun `combination n == 1`() {
        val target = setOf("free")

        expect(setOf(linkedHashSetOf("free"))) {
            target.combination(1)
        }
    }

    @Test
    fun `combination n == 2`() {
        val target = setOf("free", "fall")

        expect(
            setOf(
                linkedHashSetOf("free"),
                linkedHashSetOf("fall")
            )
        ) {
            target.combination(1)
        }

        expect(
            target.combination(1) + setOf(
                linkedHashSetOf("free", "fall")
            )
        ) {
            target.combination(2)
        }
    }

    @Test
    fun `combination n == 4`() {
        val target = setOf("free", "fall", "ext", "debug")

        expect(
            setOf(
                linkedHashSetOf("free"),
                linkedHashSetOf("fall"),
                linkedHashSetOf("ext"),
                linkedHashSetOf("debug")
            )
        ) {
            target.combination(1)
        }

        expect(
            target.combination(1) + setOf(
                linkedHashSetOf("free", "fall"),
                linkedHashSetOf("free", "ext"),
                linkedHashSetOf("free", "debug"),
                linkedHashSetOf("fall", "ext"),
                linkedHashSetOf("fall", "debug"),
                linkedHashSetOf("ext", "debug")
            )
        ) {
            target.combination(2)
        }
    }

    private fun <T> linkedHashSetOf(vararg elements: T): LinkedHashSet<T> {
        val set = LinkedHashSet<T>()
        set.addAll(elements)
        return set
    }
}
