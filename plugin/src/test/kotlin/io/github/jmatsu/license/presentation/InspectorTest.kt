package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.Factory.provideArtifact
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
            assertEquals(ArtifactInspector.Result.NoLicenses, first())
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
                    ArtifactInspector.Result.NoLicenses
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
                providePlainLicense(LicenseClassifier.PredefinedKey.UNDETERMINED),
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

        with(results.first { (a, _) -> a.key.value == LicenseClassifier.PredefinedKey.UNDETERMINED }.second) {
            assertEquals(1, size)
            assertEquals(LicenseInspector.Result.Undetermined, first())
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
}
