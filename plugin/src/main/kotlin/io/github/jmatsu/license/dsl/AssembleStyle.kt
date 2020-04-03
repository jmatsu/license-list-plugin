package io.github.jmatsu.license.dsl

typealias AssembleStyle = Style

fun isAssembleStyle(style: Style): Boolean {
    return style in arrayOf(StructuredStyle, FlattenStyle)
}
