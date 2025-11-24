package io.github.jmatsu.license.dsl

typealias VisualizeFormat = Format

fun isVisualizeFormat(format: Format): Boolean = format in arrayOf(FORMAT_JSON, FORMAT_HTML)
