package io.github.jmatsu.spthanks.presentation

import io.github.jmatsu.spthanks.poko.ArtifactDefinition
import io.github.jmatsu.spthanks.poko.Scope
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.builtins.serializer

class Disassembler(
        private val style: Assembler.Style,
        private val format: StringFormat
) {
    fun disassemble(text: String): List<ArtifactDefinition> {
        return when (style) {
            Assembler.Style.Flatten -> {
                format.parse(ArtifactDefinition.serializer().list, text)
            }
            Assembler.Style.StructuredWithoutScope -> {
                val serializer = MapSerializer(String.serializer(), ArtifactDefinition.serializer().list)
                format.parse(serializer, text).flatMap { (group, artifacts) ->
                    artifacts.map { it.copy(key = "$group:${it.key}") }
                }
            }
            Assembler.Style.StructuredWithScope -> {
                val serializer = MapSerializer(Scope.serializer(), MapSerializer(String.serializer(), ArtifactDefinition.serializer().list))
                format.parse(serializer, text)
                        .map { (_, m) ->
                            m.flatMap { (group, artifacts) ->
                                artifacts.map { it.copy(key = "$group:${it.key}") }
                            }
                        }
                        .flatten()
            }
        }
    }
}