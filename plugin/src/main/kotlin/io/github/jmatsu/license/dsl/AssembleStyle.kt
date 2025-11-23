package io.github.jmatsu.license.dsl

typealias AssembleStyle = Style

fun isAssembleStyle(style: Style): Boolean = style in arrayOf(StructuredStyle, FlattenStyle)
