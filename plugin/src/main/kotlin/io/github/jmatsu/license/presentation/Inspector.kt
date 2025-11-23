package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.internal.LicenseClassifier
import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.LicenseKey
import io.github.jmatsu.license.poko.PlainLicense

class Inspector(
    val artifactDefinitions: List<ArtifactDefinition>,
    val plainLicenses: List<PlainLicense>,
) : ArtifactInspector,
    LicenseInspector {
    data class AssociationResult(
        val missingKeys: List<LicenseKey>,
        val restKeys: List<LicenseKey>,
    )

    fun inspectArtifacts(): List<Pair<ArtifactDefinition, List<ArtifactInspector.Result>>> =
        artifactDefinitions.map {
            it to it.inspect()
        }

    fun inspectLicenses(): List<Pair<PlainLicense, List<LicenseInspector.Result>>> =
        plainLicenses.map {
            it to it.inspect()
        }

    fun inspectAssociations(): AssociationResult {
        val definedKeys = plainLicenses.map { it.key }
        val requiredKeys = artifactDefinitions.flatMap { it.licenses }

        return AssociationResult(
            missingKeys = requiredKeys - definedKeys,
            restKeys = definedKeys - requiredKeys,
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

    // null or has elements or unlicensed
    fun ArtifactDefinition.hasCopyrightHolders(): Boolean = copyrightHolders?.isNotEmpty() != false || isUnlicensed()

    fun ArtifactDefinition.hasActiveLicenses(): Boolean = licenses.isNotEmpty() && licenses.all { it.value != LicenseClassifier.PredefinedKey.UNDETERMINED }

    // null or has the element or unlicensed
    fun ArtifactDefinition.hasUrl(): Boolean = url?.isNotBlank() != false || isUnlicensed()

    private fun ArtifactDefinition.isUnlicensed(): Boolean = hasActiveLicenses() && licenses.all { it.value == LicenseClassifier.PredefinedKey.UNLICENSE }
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

    fun PlainLicense.hasUrl(): Boolean = url?.isNotBlank() != false

    fun PlainLicense.hasName(): Boolean = name.isNotBlank()
}
