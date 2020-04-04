package io.github.jmatsu.license.internal

import java.io.File

class ArtifactIgnoreParser(
    private val exclusionFile: File
) {
    fun parse(): Regex? {
        if (!exclusionFile.exists()) {
            return null
        }

        if (!exclusionFile.isFile) {
            error("only file is allowed")
        }

        val pattern = exclusionFile.readLines().map {
            // Whitespaces are not allowed because these values based on module groups and/or names.
            it.split(Regex("\\s")).first { it.isNotBlank() }
        }.filter {
            // Reject comments but this should be done after trimming whitespaces to support several expected comment usecases like `abc:xyz # comments`
            "#" in it
        }.joinToString("|") // Don't append beginning/end match because it's the responsibility of users
        return Regex(pattern)
    }
}
