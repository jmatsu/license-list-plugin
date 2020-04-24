package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.Factory.provideArtifact
import io.github.jmatsu.license.model.LicenseSeed
import io.github.jmatsu.license.model.ResolveScope
import io.github.jmatsu.license.model.ResolvedArtifact
import io.github.jmatsu.license.model.ResolvedModuleIdentifier
import io.github.jmatsu.license.model.ResolvedPomFile
import io.github.jmatsu.license.model.VersionString
import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.LicenseKey
import io.github.jmatsu.license.poko.PlainLicense
import io.github.jmatsu.license.poko.Scope
import kotlin.test.Test
import kotlin.test.assertEquals

class MergerTest : MergeStrategy {

    @Test
    fun `merge should take care keep attributes and user-defined licenses as well`() {
        val scopedResolvedArtifacts = mapOf<ResolveScope, List<ResolvedArtifact>>(
            ResolveScope.Variant("scope1") to listOf(
                ResolvedArtifact(
                    id = ResolvedModuleIdentifier(
                        group = "group1",
                        name = "name1",
                        version = VersionString("+")
                    ),
                    metadata = ResolvedPomFile(
                        associatedUrl = "http://example.com",
                        displayNameCandidates = listOf("displayName1"),
                        licenses = listOf(LicenseSeed(name = "licenseKeyNew", url = "http://example.com/length/28")),
                        copyrightHolders = listOf("copyrightHolder1")
                    )
                )
            ),
            ResolveScope.Variant("scope3") to listOf(
                ResolvedArtifact(
                    id = ResolvedModuleIdentifier(
                        group = "group1",
                        name = "name4",
                        version = VersionString("+")
                    ),
                    metadata = ResolvedPomFile(
                        associatedUrl = "http://example.com",
                        displayNameCandidates = listOf("displayName1"),
                        licenses = listOf(LicenseSeed(name = "Apache License 2.0", url = "http://example.com")),
                        copyrightHolders = listOf("copyrightHolder1")
                    )
                )
            )
        ).toSortedMap(Comparator { s, s2 -> s.name.compareTo(s2.name) })
        val scopedBaseArtifacts = mapOf(
            Scope("scope1") to listOf(
                ArtifactDefinition(
                    key = "group1:name1",
                    url = "http://example.com",
                    copyrightHolders = listOf(),
                    licenses = listOf(LicenseKey("apache-2.0")),
                    displayName = "displayName1"
                )
            ),
            Scope("scope2") to listOf(
                ArtifactDefinition(
                    key = "group1:name2", // missing in the new artifacts
                    url = "http://example.com",
                    copyrightHolders = listOf(),
                    licenses = listOf(LicenseKey("apache-2.0")),
                    displayName = "displayName1",
                    keep = false
                ),
                ArtifactDefinition(
                    key = "group1:name3", // missing in the new artifacts
                    url = "http://example.com",
                    copyrightHolders = listOf(),
                    licenses = listOf(LicenseKey("apache-2.0")),
                    displayName = "displayName1",
                    keep = true
                )
            ),
            Scope("scope3") to listOf(
                ArtifactDefinition(
                    key = "group1:name4",
                    url = "http://example.com",
                    copyrightHolders = listOf(),
                    licenses = listOf(LicenseKey("licenseKeyNew@28")),
                    displayName = "displayName1"
                )
            )
        )
        val baseLicenses = setOf(
            PlainLicense(
                key = LicenseKey("apache-2.0"),
                url = "http://example.com",
                name = "license1"
            ),
            PlainLicense(
                key = LicenseKey("willDisappear"),
                url = "http://example.com",
                name = "license2"
            ),
            PlainLicense(
                key = LicenseKey("licenseKeyNew@28"),
                url = "http://example.com",
                name = "licenseNew@28"
            )
        )
        val merger = Merger(
            scopedResolvedArtifacts = scopedResolvedArtifacts,
            baseLicenses = baseLicenses,
            scopedBaseArtifacts = scopedBaseArtifacts
        )

        val result = merger.merge()

        assertEquals(listOf(
            PlainLicense(
                key = LicenseKey("apache-2.0"),
                url = "http://example.com",
                name = "license1"
            ),
            PlainLicense(
                key = LicenseKey("licenseKeyNew@28"),
                url = "http://example.com",
                name = "licenseNew@28"
            )
        ), result.licenses)

        assertEquals(mapOf(
            Scope("scope1") to listOf(
                ArtifactDefinition(
                    key = "group1:name1",
                    url = "http://example.com",
                    copyrightHolders = listOf(),
                    licenses = listOf(LicenseKey("apache-2.0")),
                    displayName = "displayName1"
                )
            ),
            Scope("scope2") to listOf(
                ArtifactDefinition(
                    key = "group1:name3", // missing in the new artifacts
                    url = "http://example.com",
                    copyrightHolders = listOf(),
                    licenses = listOf(LicenseKey("apache-2.0")),
                    displayName = "displayName1",
                    keep = true
                )
            ),
            Scope("scope3") to listOf(
                ArtifactDefinition(
                    key = "group1:name4", // missing in the new artifacts
                    url = "http://example.com",
                    copyrightHolders = listOf(),
                    licenses = listOf(LicenseKey("licenseKeyNew@28")),
                    displayName = "displayName1"
                )
            )
        ), result.scopedArtifacts)
    }

    @Test
    fun `reverseMerge should not update the existing values and append unless exists`() {
        val definitions = listOf(
            provideArtifact(key = "key1"),
            provideArtifact(key = "key2").copy(
                displayName = "kept"
            )
        )

        val others = setOf(
            provideArtifact(key = "key2"),
            provideArtifact(key = "key3")
        )

        val mergedResult = definitions.reverseMerge(others) { it.key }

        // preserve key1 in definitions
        assertEquals(definitions.first { it.key == "key1" }, mergedResult.first { it.key == "key1" })

        // use key2 in definitions
        assertEquals(definitions.first { it.key == "key2" }, mergedResult.first { it.key == "key2" })
        assertEquals("kept", mergedResult.first { it.key == "key2" }.displayName)

        // use key3 in others
        assertEquals(others.first { it.key == "key3" }, mergedResult.first { it.key == "key3" })
    }
}
