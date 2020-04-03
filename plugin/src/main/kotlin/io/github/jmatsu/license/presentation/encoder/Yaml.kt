package io.github.jmatsu.license.presentation.encoder

import com.charleskorn.kaml.Yaml as Kaml
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml as SnakeYaml

class Yaml(
    override val context: SerialModule = EmptyModule,
    private val kaml: Kaml
) : SerialFormat by kaml, StringFormat {
    override fun <T> parse(deserializer: DeserializationStrategy<T>, string: String): T =
        kaml.parse(deserializer, string)

    override fun <T> stringify(serializer: SerializationStrategy<T>, value: T): String {
        val output = kaml.stringify(serializer, value)

        // TODO expose these options through extension
        val snake = SnakeYaml(DumperOptions().apply {
            defaultScalarStyle = DumperOptions.ScalarStyle.PLAIN
            nonPrintableStyle = DumperOptions.NonPrintableStyle.ESCAPE
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            isAllowUnicode = true
            indent = 4
            indicatorIndent = 2
        })

        // format
        return snake.dump(snake.load<T>(output))
    }
}
