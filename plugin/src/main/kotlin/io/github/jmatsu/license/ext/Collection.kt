package io.github.jmatsu.license.ext

import org.gradle.util.ChangeListener
import org.gradle.util.DiffUtil

fun <A, B> Collection<Pair<A, B>>.collectToMap(): Map<A, List<B>> {
    return groupBy(
        keySelector = { it.first },
        valueTransform = { it.second }
    )
}

data class DiffResult2<A>(val added: Set<A>, val removed: Set<A>)
data class DiffResult3<A>(val added: Set<A>, val changed: Set<A>, val removed: Set<A>)

fun <A> Collection<A>.xor2(other: Collection<A>): DiffResult2<A> {
    val added = HashSet<A>()
    val removed = HashSet<A>()

    DiffUtil.diff(this.toSet(), other.toSet(), object : ChangeListener<A> {
        override fun added(element: A) {
            added += element
        }

        override fun changed(element: A) {
            error("DiffUtil does not support changed because it's based on Set")
        }

        override fun removed(element: A) {
            removed += element
        }
    })

    return DiffResult2(added = added, removed = removed)
}

// FIXME better to use Myers algorithm because of time-complexity and interfaces
fun <A, B> Collection<A>.xor2(other: Collection<A>, keyExtractor: (A) -> B): DiffResult3<A> {
    val added = HashSet<A>()
    val changed = HashSet<A>()
    val removed = HashSet<A>()

    DiffUtil.diff(
        this.groupBy(keyExtractor).mapValues { (_, vs) -> vs.first() },
        other.groupBy(keyExtractor).mapValues { (_, vs) -> vs.first() },
        object : ChangeListener<Map.Entry<B, A>> {
            override fun added(element: Map.Entry<B, A>) {
                added += element.value
            }

            override fun changed(element: Map.Entry<B, A>) {
                changed += element.value
            }

            override fun removed(element: Map.Entry<B, A>) {
                removed += element.value
            }
        })

    return DiffResult3(added = added, changed = changed, removed = removed)
}
