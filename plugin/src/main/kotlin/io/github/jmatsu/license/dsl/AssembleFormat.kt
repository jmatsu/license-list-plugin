package io.github.jmatsu.license.dsl

typealias AssembleFormat = Format

fun isAssembleFormat(format: Format): Boolean {
    return format in arrayOf(JsonFormat, YamlFormat)
}
