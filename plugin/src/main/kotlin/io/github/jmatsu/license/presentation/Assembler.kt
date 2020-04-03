package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.LicenseListPlugin
import io.github.jmatsu.license.ext.collectToMap
import io.github.jmatsu.license.internal.LicenseClassifier
import io.github.jmatsu.license.model.ResolveScope
import io.github.jmatsu.license.model.ResolvedArtifact
import io.github.jmatsu.license.model.ResolvedPomFile
import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.LicenseKey
import io.github.jmatsu.license.poko.PlainLicense
import io.github.jmatsu.license.poko.Scope
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.builtins.serializer
import java.util.SortedMap

class Assembler(
    private val resolvedArtifactMap: SortedMap<ResolveScope, List<ResolvedArtifact>>,
    private val licenseCapture: MutableSet<PlainLicense> = HashSet()
) {
    companion object {
        fun assembleArtifact(artifact: ResolvedArtifact, licenseCapture: MutableSet<PlainLicense>): ArtifactDefinition {
            return ArtifactDefinition(
                key = "${artifact.id.group}:${artifact.id.name}",
                licenses = artifact.pomFile.licenses(licenseCapture),
                copyrightHolders = artifact.pomFile.copyrightHolders,
                url = artifact.pomFile.associatedUrl,
                displayName = artifact.pomFile.displayName
            )
        }

        fun assembleFlatten(format: StringFormat, definitions: List<ArtifactDefinition>): String {
            return format.stringify(ArtifactDefinition.serializer().list, definitions)
        }

        fun assembleStructuredWithoutScope(format: StringFormat, definitionMap: Map<String, List<ArtifactDefinition>>): String {
            val serializer = MapSerializer(String.serializer(), ArtifactDefinition.serializer().list)
            return format.stringify(serializer, definitionMap)
        }

        fun assembleStructuredWithScope(format: StringFormat, scopedDefinitionMap: Map<Scope, Map<String, List<ArtifactDefinition>>>): String {
            val serializer = MapSerializer(Scope.serializer(), MapSerializer(String.serializer(), ArtifactDefinition.serializer().list))
            return format.stringify(serializer, scopedDefinitionMap)
        }

        private fun ResolvedPomFile.licenses(licenseCapture: MutableSet<PlainLicense>): List<LicenseKey> {
            return licenses.map {
                when (val guessedLicense = LicenseClassifier(it.name).guess()) {
                    is LicenseClassifier.GuessedLicense.Undetermined -> {
                        val name = it.name ?: guessedLicense.name
                        val url = it.url ?: guessedLicense.url

                        LicenseKey(value = "$name@${url.length}").also { key ->
                            licenseCapture += PlainLicense(
                                name = name,
                                url = url,
                                key = key
                            )
                        }
                    }
                    else -> {
                        LicenseKey(value = guessedLicense.key).also { key ->
                            licenseCapture += PlainLicense(
                                name = guessedLicense.name,
                                url = guessedLicense.url,
                                key = key
                            )
                        }
                    }
                }
            }
        }
    }

    sealed class Style {
        object Flatten : Style()
        object StructuredWithoutScope : Style()
        object StructuredWithScope : Style()
    }

    fun assembleArtifacts(style: Style, format: StringFormat): String {
        return when (style) {
            Style.Flatten -> {
                assembleFlatten(format = format, definitions = transformForFlatten())
            }
            Style.StructuredWithoutScope -> {
                assembleStructuredWithoutScope(format = format, definitionMap = tranformForStructuredWithoutScope())
            }
            Style.StructuredWithScope -> {
                assembleStructuredWithScope(format = format, scopedDefinitionMap = transformForStructuredWithScope())
            }
        }
    }

    fun assemblePlainLicenses(format: StringFormat): String {
        // assemble must be called in advance
        val licenses = licenseCapture.sortedBy { it.key.value }

        licenses.forEach {
            LicenseListPlugin.logger?.info(it.key.toString())
        }

        return format.stringify(PlainLicense.serializer().list, licenses)
    }

    fun transformForFlatten(): List<ArtifactDefinition> {
        return resolvedArtifactMap.flatMap { (_, artifacts) ->
            artifacts.map { artifact ->
                assembleArtifact(artifact, licenseCapture)
            }
        }.sorted()
    }

    fun tranformForStructuredWithoutScope(): Map<String, List<ArtifactDefinition>> {
        return resolvedArtifactMap.map { (_, artifacts) ->
            artifacts.map { artifact ->
                artifact.id.group to assembleArtifact(artifact, licenseCapture).copy(key = artifact.id.name)
            }.sortedBy { it.second }.sortedBy { it.first }.collectToMap()
        }.reduce { acc, map -> acc + map }.toSortedMap()
    }

    fun transformForStructuredWithScope(): Map<Scope, Map<String, List<ArtifactDefinition>>> {
        return resolvedArtifactMap.map { (scope, artifacts) ->
            Scope(name = scope.name) to (artifacts.map { artifact ->
                artifact.id.group to assembleArtifact(artifact, licenseCapture).copy(key = artifact.id.name)
            }.sortedBy { it.second }.sortedBy { it.first }.collectToMap())
        }.toMap()
    }
}
