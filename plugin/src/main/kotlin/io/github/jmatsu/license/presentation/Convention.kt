package io.github.jmatsu.license.presentation

import com.charleskorn.kaml.YamlConfiguration
import io.github.jmatsu.license.presentation.encoder.Html
import io.github.jmatsu.license.presentation.encoder.HtmlConfiguration
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule

object Convention {
    object Yaml {
        val Assembly =
            io.github.jmatsu.license.presentation.encoder.Yaml(
                serializersModule = EmptySerializersModule(),
                kaml =
                    com.charleskorn.kaml.Yaml(
                        configuration =
                            YamlConfiguration(
                                strictMode = false,
                            ),
                    ),
            )
    }

    object Json {
        val Assembly =
            Json {
                serializersModule = EmptySerializersModule()
                ignoreUnknownKeys = true
            }
        val Visualization =
            Json {
                serializersModule = EmptySerializersModule()
                ignoreUnknownKeys = true
                prettyPrint = false
            }
    }

    object Html {
        @Suppress("FunctionName")
        fun Visualization(htmlConfiguration: HtmlConfiguration) =
            Html(
                serializersModule = EmptySerializersModule(),
                htmlConfiguration = htmlConfiguration,
            )
    }
}
