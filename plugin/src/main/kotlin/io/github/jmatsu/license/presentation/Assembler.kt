package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.LicenseListPlugin
import io.github.jmatsu.license.ext.collectToMap
import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.PlainLicense
import io.github.jmatsu.license.poko.Scope
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

class Assembler(
    private val assembleeData: AssembleeData,
) : StructuringStrategy {
    sealed class Style {
        object Flatten : Style()

        object StructuredWithoutScope : Style()

        object StructuredWithScope : Style()
    }

    fun assembleArtifacts(
        style: Style,
        format: StringFormat,
    ): String {
        val scopedArtifacts = assembleeData.scopedArtifacts

        return when (style) {
            Style.Flatten -> {
                val serializer = ListSerializer(ArtifactDefinition.serializer())
                format.encodeToString(serializer, scopedArtifacts.values.flatten().sorted())
            }
            Style.StructuredWithoutScope -> {
                val serializer = MapSerializer(String.serializer(), ListSerializer(ArtifactDefinition.serializer()))
                format.encodeToString(serializer, scopedArtifacts.values.flatten().collectToMapByArtifactGroup())
            }
            Style.StructuredWithScope -> {
                val serializer = MapSerializer(Scope.serializer(), MapSerializer(String.serializer(), ListSerializer(ArtifactDefinition.serializer())))
                format.encodeToString(serializer, scopedArtifacts.mapValues { (_, artifacts) -> artifacts.collectToMapByArtifactGroup() })
            }
        }
    }

    fun assemblePlainLicenses(format: StringFormat): String {
        val licenses = assembleeData.licenses.sortedBy { it.key.value }

        licenses.forEach {
            LicenseListPlugin.logger?.info(it.key.toString())
        }

        return format.encodeToString(ListSerializer(PlainLicense.serializer()), licenses)
    }
}

interface StructuringStrategy {
    fun List<ArtifactDefinition>.collectToMapByArtifactGroup(): Map<String, List<ArtifactDefinition>> =
        map {
            // for safe split
            val (group, name) = "${it.key}:${it.key}".split(":")

            group to it.copy(key = name)
        }.sortedBy { it.second }.sortedBy { it.first }.collectToMap()
}
