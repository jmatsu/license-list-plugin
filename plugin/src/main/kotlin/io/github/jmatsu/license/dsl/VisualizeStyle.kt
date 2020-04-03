package io.github.jmatsu.license.dsl

typealias VisualizeFormat = Format

fun isVisualizeFormat(format: Format): Boolean {
    return format in arrayOf(JsonFormat, HtmlFormat)
}
