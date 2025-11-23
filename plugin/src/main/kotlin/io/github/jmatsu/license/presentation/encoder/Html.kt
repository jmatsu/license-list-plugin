package io.github.jmatsu.license.presentation.encoder

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.github.jmatsu.license.poko.DisplayArtifact
import java.io.StringWriter
import java.util.Locale
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * Not a proper string format of kotlin serialization. This is just a wrapper class to unify types.
 */
class Html(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    private val htmlConfiguration: HtmlConfiguration
) : StringFormat {
    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        error("does not support")
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        @Suppress("UNCHECKED_CAST")
        value as List<DisplayArtifact>

        val configuration = Configuration(htmlConfiguration.version)

        if (htmlConfiguration.templateDir == null) {
            configuration.setClassLoaderForTemplateLoading(this.javaClass.classLoader, "templates")
        } else {
            configuration.setDirectoryForTemplateLoading(htmlConfiguration.templateDir)
        }

        configuration.defaultEncoding = "UTF-8"
        configuration.locale = Locale.US
        configuration.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER

        val input = mapOf(
            "title" to "License List",
            "artifacts" to value
        )

        val template = configuration.getTemplate("license.html.ftl")

        return StringWriter().apply {
            use {
                template.process(input, it)
            }
        }.toString()
    }
}
