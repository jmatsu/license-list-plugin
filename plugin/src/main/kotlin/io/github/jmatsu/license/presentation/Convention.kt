package io.github.jmatsu.license.presentation

import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.EmptyModule

class Convention {
    companion object {
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
        val DisplayJson = kotlinx.serialization.json.Json(
            context = EmptyModule,
            configuration = JsonConfiguration.Stable.copy(
                ignoreUnknownKeys = true,
                unquotedPrint = false,
                prettyPrint = false
            )
        )
    }
}
