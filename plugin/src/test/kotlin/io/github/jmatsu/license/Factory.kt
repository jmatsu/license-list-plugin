package io.github.jmatsu.license

import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.LicenseKey

object Factory {
    fun provideArtifact(key: String): ArtifactDefinition {
        return ArtifactDefinition(
            key = key,
            displayName = "displayName",
            url = "url",
            licenses = listOf(
                LicenseKey("license")
            ),
            copyrightHolders = listOf(
                "copyrightHolder"
            )
        )
    }

    fun provideLicenseKey(value: String): LicenseKey = LicenseKey(value)
}
