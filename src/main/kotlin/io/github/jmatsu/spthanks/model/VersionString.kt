package io.github.jmatsu.spthanks.model

import io.github.jmatsu.spthanks.globalLogger

data class VersionString(
        val value: String
) : Comparable<VersionString> {
    private val hunks: List<Int> = value.split(".").map {
        if (it == "+") {
            Int.MAX_VALUE
        } else {
            it.toIntOrNull() ?: run {
                globalLogger.debug("$it might not be handled properly")
                0
            }
        }
    }

    override fun compareTo(other: VersionString): Int {
        val compareSeed = hunks.zip(other.hunks).firstOrNull {
            it.first.compareTo(it.second) != 0
        }

        return compareSeed?.first?.compareTo(compareSeed.second) ?: hunks.size.compareTo(other.hunks.size)
    }
}