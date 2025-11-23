package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.internal.LicenseClassifier
import io.github.jmatsu.license.model.ResolveScope
import io.github.jmatsu.license.model.ResolvedArtifact
import io.github.jmatsu.license.model.ResolvedMetadata
import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.LicenseKey
import io.github.jmatsu.license.poko.PlainLicense
import io.github.jmatsu.license.poko.Scope
import java.util.SortedMap

class Builder(
    private val resolvedArtifactMap: SortedMap<ResolveScope, List<ResolvedArtifact>>,
) {
    private val licenseCapture: MutableSet<PlainLicense> = HashSet()

    companion object {
        fun transformToArtifact(
            artifact: ResolvedArtifact,
            licenseCapture: MutableSet<PlainLicense>,
        ): ArtifactDefinition =
            ArtifactDefinition(
                key = "${artifact.id.group}:${artifact.id.name}",
                licenses = artifact.metadata.licenses(licenseCapture),
                copyrightHolders = artifact.metadata.copyrightHolders,
                url = artifact.metadata.associatedUrl,
                displayName = artifact.metadata.displayName,
            )

        private fun ResolvedMetadata.licenses(licenseCapture: MutableSet<PlainLicense>): List<LicenseKey> =
            licenses.map {
                val licenseName = it.name?.trim { c -> c.isWhitespace() || c == '\n' || c == '\r' }

                when (val guessedLicense = LicenseClassifier(licenseName).guess()) {
                    is LicenseClassifier.GuessedLicense.Undetermined -> {
                        val name = licenseName ?: guessedLicense.name
                        val url = it.url.orEmpty()

                        LicenseKey(value = "$name@${url.length}").also { key ->
                            licenseCapture +=
                                PlainLicense(
                                    name = name,
                                    url = url,
                                    key = key,
                                )
                        }
                    }
                    else -> {
                        LicenseKey(value = guessedLicense.key).also { key ->
                            licenseCapture +=
                                PlainLicense(
                                    name = guessedLicense.name,
                                    url = guessedLicense.url,
                                    key = key,
                                )
                        }
                    }
                }
            }
    }

    fun build(): AssembleeData {
        val scopedArtifacts =
            resolvedArtifactMap
                .map { (scope, artifacts) ->
                    Scope(name = scope.name) to (
                        artifacts
                            .map { artifact ->
                                transformToArtifact(artifact, licenseCapture)
                            }.sortedBy { it.key }
                    )
                }.toMap()

        val licenses = licenseCapture.sortedBy { it.key.value }

        return AssembleeData(
            scopedArtifacts = scopedArtifacts,
            licenses = licenses,
        )
    }
}
