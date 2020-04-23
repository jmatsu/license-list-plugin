package io.github.jmatsu.license.internal

import java.io.File
import java.nio.file.FileSystems

typealias IgnorePredicate = (group: String, name: String) -> Boolean

class ArtifactIgnoreParser(
    private val ignoreFile: File
) {
    sealed class Format {
        object Regex : Format()
        object Glob : Format()
    }

    fun buildPredicate(format: Format): IgnorePredicate? {
        if (!ignoreFile.exists()) {
            return null
        }

        if (!ignoreFile.isFile) {
            error("only file is allowed")
        }

        return when (format) {
            Format.Regex -> buildRegexPredicate(ignoreFile.readLines())
            Format.Glob -> buildGlobPredicate(ignoreFile.readLines())
        }
    }

    fun buildRegexPredicate(lines: List<String>): IgnorePredicate {
        val pattern = lines.rejectComments().joinToString("|") { "^$it$" } // enclose by beginning/end match

        val regex = Regex(pattern, RegexOption.IGNORE_CASE)

        return { group, name ->
            regex.matches("$group:$name")
        }
    }

    fun buildGlobPredicate(lines: List<String>): IgnorePredicate {
        val matchers = lines.map { glob ->
            FileSystems.getDefault().getPathMatcher("glob:${glob.replace(":", "/")}")
        }

        return { group, name ->
            val path = FileSystems.getDefault().getPath(group, name)

            matchers.any {
                it.matches(path)
            }
        }
    }

    private fun List<String>.rejectComments(): List<String> {
        return mapNotNull {
            // Whitespaces are not allowed because these values based on module groups and/or names.
            it.split(Regex("\\s")).firstOrNull()
        }.filterNot {
            // Reject comments but this should be done after trimming whitespaces to support several expected comment usecases like `abc:xyz # comments`
            "#" in it || it.isBlank()
        }
    }
}
