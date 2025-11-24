package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.PlainLicense
import io.github.jmatsu.license.poko.Scope
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString

class Disassembler(
    private val style: Assembler.Style,
    private val format: StringFormat,
) {
    fun disassembleArtifacts(text: String): Map<Scope, List<ArtifactDefinition>> =
        when (style) {
            Assembler.Style.Flatten -> {
                mapOf(
                    Scope.StubScope to format.decodeFromString(ListSerializer(ArtifactDefinition.serializer()), text),
                )
            }
            Assembler.Style.StructuredWithoutScope -> {
                val serializer = MapSerializer(String.serializer(), ListSerializer(ArtifactDefinition.serializer()))
                mapOf(
                    Scope.StubScope to
                        format.decodeFromString(serializer, text).flatMap { (group, artifacts) ->
                            artifacts.map { it.copy(key = "$group:${it.key}") }
                        },
                )
            }
            Assembler.Style.StructuredWithScope -> {
                val serializer = MapSerializer(Scope.serializer(), MapSerializer(String.serializer(), ListSerializer(ArtifactDefinition.serializer())))
                format
                    .decodeFromString(serializer, text)
                    .mapValues { (_, m) ->
                        m.flatMap { (group, artifacts) ->
                            artifacts.map { it.copy(key = "$group:${it.key}") }
                        }
                    }
            }
        }

    fun disassemblePlainLicenses(text: String): List<PlainLicense> {
        val serializer = ListSerializer(PlainLicense.serializer())
        return format.decodeFromString(serializer, text)
    }
}
