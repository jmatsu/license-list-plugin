package io.github.jmatsu.spthanks.presentation

import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.EmptyModule

object Convention {
    val Yaml = com.charleskorn.kaml.Yaml(
        context = EmptyModule,
        configuration = YamlConfiguration(
            strictMode = false
        )
    )
    val Json = kotlinx.serialization.json.Json(
        context = EmptyModule,
        configuration = JsonConfiguration.Stable.copy(
            ignoreUnknownKeys = true
        )
    )
}
