package io.github.jmatsu.license.presentation

import com.charleskorn.kaml.YamlConfiguration
import io.github.jmatsu.license.presentation.encoder.Html
import io.github.jmatsu.license.presentation.encoder.HtmlConfiguration
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.EmptyModule

object Convention {
    object Yaml {
        val Assembly = com.charleskorn.kaml.Yaml(
            context = EmptyModule,
            configuration = YamlConfiguration(
                strictMode = false
            )
        )
    }

    object Json {
        val Assembly = kotlinx.serialization.json.Json(
            context = EmptyModule,
            configuration = JsonConfiguration.Stable.copy(
                ignoreUnknownKeys = true
            )
        )
        val Visualization = kotlinx.serialization.json.Json(
            context = EmptyModule,
            configuration = JsonConfiguration.Stable.copy(
                ignoreUnknownKeys = true,
                unquotedPrint = false,
                prettyPrint = false
            )
        )
    }

    object Html {
        @Suppress("FunctionName")
        fun Visualization(htmlConfiguration: HtmlConfiguration) = Html(
            context = EmptyModule,
            htmlConfiguration = htmlConfiguration
        )
    }
}
