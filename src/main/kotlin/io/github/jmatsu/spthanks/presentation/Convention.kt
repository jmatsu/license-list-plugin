package io.github.jmatsu.spthanks.presentation

import com.charleskorn.kaml.YamlConfiguration
import io.github.jmatsu.spthanks.poko.License
import kotlinx.serialization.json.JsonConfiguration

object Convention {
    val Yaml = com.charleskorn.kaml.Yaml(
            context = License.Serialization.module,
            configuration = YamlConfiguration(
                    strictMode = false
            )
    )
    val Json = kotlinx.serialization.json.Json(
            context = License.Serialization.module,
            configuration = JsonConfiguration.Stable.copy(
                    ignoreUnknownKeys = true
            )
    )
}