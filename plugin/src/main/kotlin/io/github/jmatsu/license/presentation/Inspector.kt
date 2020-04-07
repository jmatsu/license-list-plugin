package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.internal.LicenseClassifier
import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.LicenseKey
import io.github.jmatsu.license.poko.PlainLicense

class Inspector(
    val artifactDefinitions: List<ArtifactDefinition>,
    val plainLicenses: List<PlainLicense>
) : ArtifactInspector, LicenseInspector {
    data class AssociationResult(
        val missingKeys: List<LicenseKey>,
        val restKeys: List<LicenseKey>
    )

    fun inspectArtifacts(): List<Pair<ArtifactDefinition, List<ArtifactInspector.Result>>> {
        return artifactDefinitions.map {
            it to it.inspect()
        }
    }

    fun inspectLicenses(): List<Pair<PlainLicense, List<LicenseInspector.Result>>> {
        return plainLicenses.map {
            it to it.inspect()
        }
    }

    fun inspectAssociations(): AssociationResult {
        val definedKeys = plainLicenses.map { it.key }
        val requiredKeys = artifactDefinitions.flatMap { it.licenses }

        return AssociationResult(
            missingKeys = requiredKeys - definedKeys,
            restKeys = definedKeys - requiredKeys
        )
    }
}

interface ArtifactInspector {
    sealed class Result {
        object NoCopyrightHolders : Result()
        object InactiveLicense : Result()
        object NoUrl : Result()
        object Success : Result()
    }

    fun ArtifactDefinition.inspect(): List<Result> {
        val results: MutableList<Result> = mutableListOf()

        if (!hasCopyrightHolders()) {
            results.add(Result.NoCopyrightHolders)
        }
        if (!hasActiveLicenses()) {
            results.add(Result.InactiveLicense)
        }
        if (!hasUrl()) {
            results.add(Result.NoUrl)
        }
        return results.takeIf { it.isNotEmpty() } ?: listOf(Result.Success)
    }

    fun ArtifactDefinition.hasCopyrightHolders(): Boolean {
        return copyrightHolders.isNotEmpty() || licenses.any { it.value == LicenseClassifier.PredefinedKey.UNLICENSE }
    }

    fun ArtifactDefinition.hasActiveLicenses(): Boolean {
        return licenses.isNotEmpty() && licenses.all { it.value != LicenseClassifier.PredefinedKey.UNDETERMINED }
    }

    fun ArtifactDefinition.hasUrl(): Boolean {
        return url?.isNotBlank() == true
    }
}

interface LicenseInspector {
    sealed class Result {
        object NoUrl : Result()
        object NoName : Result()
        object Success : Result()
    }

    fun PlainLicense.inspect(): List<Result> {
        val results: MutableList<Result> = mutableListOf()

        if (!hasUrl()) {
            results.add(Result.NoUrl)
        }
        if (!hasName()) {
            results.add(Result.NoName)
        }

        return results.takeIf { it.isNotEmpty() } ?: listOf(Result.Success)
    }

    fun PlainLicense.hasUrl(): Boolean {
        return url.isNotBlank()
    }

    fun PlainLicense.hasName(): Boolean {
        return name.isNotBlank()
    }
}
