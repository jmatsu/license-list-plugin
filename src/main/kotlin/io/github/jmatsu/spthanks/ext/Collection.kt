package io.github.jmatsu.spthanks.ext

fun <A, B> Collection<Pair<A, B>>.collectToMap(): Map<A, List<B>> {
    return groupBy(
        keySelector = { it.first },
        valueTransform = { it.second }
    )
}
