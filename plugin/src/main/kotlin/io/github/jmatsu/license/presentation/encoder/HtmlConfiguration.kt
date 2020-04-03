package io.github.jmatsu.license.presentation.encoder

import freemarker.template.Version
import java.io.File

data class HtmlConfiguration(
    val version: Version,
    val templateDir: File? = null
)
