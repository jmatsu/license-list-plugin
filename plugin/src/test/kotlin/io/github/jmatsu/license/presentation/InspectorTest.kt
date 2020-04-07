package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.Factory.provideArtifact
import io.github.jmatsu.license.Factory.provideLicenseKey
import io.github.jmatsu.license.Factory.providePlainLicense
import io.github.jmatsu.license.internal.LicenseClassifier
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class InspectorTest {

    @Test
    fun `inspect artifacts`() {
        val inspector = Inspector(
            artifactDefinitions = listOf(
                provideArtifact(key = "missing_url").copy(
                    url = null
                ),
                provideArtifact(key = "missing_copyrightholder").copy(
                    copyrightHolders = emptyList()
                ),
                provideArtifact(key = "missing_license").copy(
                    licenses = emptyList()
                ),
                provideArtifact(key = "undetemined_license").copy(
                    licenses = listOf(
                        provideLicenseKey(LicenseClassifier.PredefinedKey.UNDETERMINED)
                    )
                ),
                provideArtifact(key = "contain_undetermined_license").copy(
                    licenses = listOf(
                        provideLicenseKey("any"),
                        provideLicenseKey(LicenseClassifier.PredefinedKey.UNDETERMINED)
                    )
                ),
                provideArtifact(key = "success"),
                provideArtifact(key = "all_failure").copy(
                    copyrightHolders = emptyList(),
                    licenses = emptyList(),
                    url = null
                )
            ),
            plainLicenses = mockk()
        )

        val results = inspector.inspectArtifacts()

        with(results.first { (a, _) -> a.key == "missing_url" }.second) {
            assertEquals(1, size)
            assertEquals(ArtifactInspector.Result.NoUrl, first())
        }

        with(results.first { (a, _) -> a.key == "missing_copyrightholder" }.second) {
            assertEquals(1, size)
            assertEquals(ArtifactInspector.Result.NoCopyrightHolders, first())
        }

        with(results.first { (a, _) -> a.key == "missing_license" }.second) {
            assertEquals(1, size)
            assertEquals(ArtifactInspector.Result.InactiveLicense, first())
        }

        with(results.first { (a, _) -> a.key == "undetemined_license" }.second) {
            assertEquals(1, size)
            assertEquals(ArtifactInspector.Result.InactiveLicense, first())
        }

        with(results.first { (a, _) -> a.key == "contain_undetermined_license" }.second) {
            assertEquals(1, size)
            assertEquals(ArtifactInspector.Result.InactiveLicense, first())
        }

        with(results.first { (a, _) -> a.key == "success" }.second) {
            assertEquals(1, size)
            assertEquals(ArtifactInspector.Result.Success, first())
        }

        with(results.first { (a, _) -> a.key == "all_failure" }.second) {
            assertEquals(3, size)
            assertEquals(
                setOf(
                    ArtifactInspector.Result.NoUrl,
                    ArtifactInspector.Result.NoCopyrightHolders,
                    ArtifactInspector.Result.InactiveLicense
                ),
                toSet()
            )
        }
    }

    @Test
    fun `inspect licenses`() {
        val inspector = Inspector(
            artifactDefinitions = mockk(),
            plainLicenses = listOf(
                providePlainLicense("missing_url").copy(
                    url = ""
                ),
                providePlainLicense("missing_name").copy(
                    name = ""
                ),
                providePlainLicense("success"),
                providePlainLicense("all_failure").copy(
                    url = "",
                    name = ""
                )
            )
        )

        val results = inspector.inspectLicenses()

        with(results.first { (a, _) -> a.key.value == "missing_url" }.second) {
            assertEquals(1, size)
            assertEquals(LicenseInspector.Result.NoUrl, first())
        }

        with(results.first { (a, _) -> a.key.value == "missing_name" }.second) {
            assertEquals(1, size)
            assertEquals(LicenseInspector.Result.NoName, first())
        }

        with(results.first { (a, _) -> a.key.value == "success" }.second) {
            assertEquals(1, size)
            assertEquals(LicenseInspector.Result.Success, first())
        }

        with(results.first { (a, _) -> a.key.value == "all_failure" }.second) {
            assertEquals(2, size)
            assertEquals(
                setOf(
                    LicenseInspector.Result.NoUrl,
                    LicenseInspector.Result.NoName
                ),
                toSet()
            )
        }
    }

    @Test
    fun `inspect associations`() {
        val inspector = Inspector(
            artifactDefinitions = listOf(
                provideArtifact(key = "key1").copy(
                    licenses = listOf(
                        provideLicenseKey("defined1")
                    )
                ),
                provideArtifact(key = "undetemined_license").copy(
                    licenses = listOf(
                        provideLicenseKey("defined2"),
                        provideLicenseKey("not_defined1")
                    )
                )
            ),
            plainLicenses = listOf(
                providePlainLicense("defined1"),
                providePlainLicense("defined2"),
                providePlainLicense("rest1")
            )
        )

        val result = inspector.inspectAssociations()

        assertEquals(listOf(provideLicenseKey("not_defined1")), result.missingKeys)
        assertEquals(listOf(provideLicenseKey("rest1")), result.restKeys)
    }
}
