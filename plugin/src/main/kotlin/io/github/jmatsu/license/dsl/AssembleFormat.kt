package io.github.jmatsu.license.dsl

typealias AssembleFormat = Format

fun isAssembleFormat(format: Format): Boolean = format in arrayOf(FORMAT_JSON, FORMAT_YAML)
