package io.github.jmatsu.license.ext

fun <T> Set<T>.combination(k: Int): Set<LinkedHashSet<T>> =
    when {
        k > size || k < 0 -> error("k must be in (0 <= $size) but $k")
        k == 0 -> setOf(LinkedHashSet())
        else ->
            combination(k - 1)
                .flatMap { `c_k-1` ->
                    this@combination.map { e ->
                        `c_k-1` + e
                    }
                }.toSet()
    }

/**
 * ensure the order of children carefully!
 */
private operator fun <T> LinkedHashSet<T>.plus(element: T): LinkedHashSet<T> {
    val newSet = (this as Set<T>).plus(element)

    return try {
        newSet as LinkedHashSet<T>
    } catch (KotlinChangedImplementation: ClassCastException) {
        val result = LinkedHashSet<T>(newSet.size)
        result.addAll(this)
        result.add(element)
        result
    }
}
