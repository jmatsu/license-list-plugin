package io.github.jmatsu.license.ext

fun <A, B> Collection<Pair<A, B>>.collectToMap(): Map<A, List<B>> =
    groupBy(
        keySelector = { it.first },
        valueTransform = { it.second },
    )
