package io.github.jmatsu.license.internal

import java.io.File

class ArtifactIgnoreParser(
    private val ignoreFile: File
) {
    fun parse(): Regex? {
        if (!ignoreFile.exists()) {
            return null
        }

        if (!ignoreFile.isFile) {
            error("only file is allowed")
        }

        val pattern = ignoreFile.readLines().mapNotNull {
            // Whitespaces are not allowed because these values based on module groups and/or names.
            it.split(Regex("\\s")).firstOrNull()
        }.filterNot {
            // Reject comments but this should be done after trimming whitespaces to support several expected comment usecases like `abc:xyz # comments`
            "#" in it || it.isBlank()
        }.joinToString("|") { "^$it$" } // enclose by beginning/end match
        return Regex(pattern)
    }
}
