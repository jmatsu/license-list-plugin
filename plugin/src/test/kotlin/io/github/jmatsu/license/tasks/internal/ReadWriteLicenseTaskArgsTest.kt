package io.github.jmatsu.license.tasks.internal

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.internal.ArtifactManagement
import io.github.jmatsu.license.model.ResolveScope
import io.github.jmatsu.license.presentation.Assembler
import io.github.jmatsu.license.presentation.Convention
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.kotlin.dsl.findByType
import org.gradle.testfixtures.ProjectBuilder

class ReadWriteLicenseTaskArgsTest {
    class TestArgs(
        project: Project,
        extension: LicenseListExtension,
        variant: ApplicationVariant
    ) : ReadWriteLicenseTaskArgs(
        project = project,
        extension = extension,
        variant = variant
    )

    lateinit var project: ProjectInternal
    lateinit var extension: LicenseListExtension
    lateinit var variant: ApplicationVariant

    @BeforeTest
    fun before() {
        project = ProjectBuilder.builder().build() as ProjectInternal
        project.plugins.apply("license-list")
        extension = requireNotNull(project.extensions.findByType(LicenseListExtension::class))
        variant = mockk {
            every { name } returns "featureRelease"
        }
    }

    @Test
    fun `build default args`() {
        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(Assembler.Style.StructuredWithScope, assemblyStyle)
            assertEquals(Convention.Yaml.Assembly, assemblyFormat)
            assertEquals("yml", assembledFileExt)
            assertEquals(ArtifactManagement.CommonConfigurationNames, configurationNames)
            assertEquals(ResolveScope.Variant("featureRelease"), variantScope)
            assertEquals(
                setOf(
                    ResolveScope.Addition("test"),
                    ResolveScope.Addition("androidTest")
                ), additionalScopes
            )
            assertEquals(File(project.projectDir, "artifact-definition.yml"), assembledArtifactsFile)
            assertEquals(project.projectDir, assembleOutputDir)
            assertEquals(File(project.projectDir, "license-catalog.yml"), assembledLicenseCatalogFile)
            assertEquals(emptySet(), excludeGroups)
            assertEquals(emptySet(), excludeArtifacts)
        }
    }

    @Test
    fun `withScope is false`() {
        extension.groupByScopes = false

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(Assembler.Style.StructuredWithoutScope, assemblyStyle)
        }
    }

    @Test
    fun `assembleStyle is flatten`() {
        extension.assembleStyle = "flatten"

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(Assembler.Style.Flatten, assemblyStyle)
        }
    }

    @Test
    fun `assembleFormat is json`() {
        extension.assembleFormat = "json"

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(Convention.Json.Assembly, assemblyFormat)
            assertEquals("json", assembledFileExt)
            assertEquals(File(project.projectDir, "artifact-definition.json"), assembledArtifactsFile)
            assertEquals(File(project.projectDir, "license-catalog.yml"), assembledLicenseCatalogFile)
        }
    }

    @Test
    fun `targetVariant is modifiable but no impact`() {
        extension.targetVariant = "debug"

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(Assembler.Style.StructuredWithScope, assemblyStyle)
            assertEquals(Convention.Yaml.Assembly, assemblyFormat)
            assertEquals("yml", assembledFileExt)
            assertEquals(ArtifactManagement.CommonConfigurationNames, configurationNames)
            assertEquals(ResolveScope.Variant("featureRelease"), variantScope)
            assertEquals(
                setOf(
                    ResolveScope.Addition("test"),
                    ResolveScope.Addition("androidTest")
                ), additionalScopes
            )
            assertEquals(File(project.projectDir, "artifact-definition.yml"), assembledArtifactsFile)
            assertEquals(project.projectDir, assembleOutputDir)
            assertEquals(File(project.projectDir, "license-catalog.yml"), assembledLicenseCatalogFile)
            assertEquals(emptySet(), excludeGroups)
            assertEquals(emptySet(), excludeArtifacts)
        }
    }

    @Test
    fun `additionalScopes is appendable`() {
        extension.additionalScopes += setOf("abc", "xyz")

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(
                setOf(
                    ResolveScope.Addition("test"),
                    ResolveScope.Addition("androidTest"),
                    ResolveScope.Addition("abc"),
                    ResolveScope.Addition("xyz")
                ), additionalScopes
            )
        }
    }

    @Test
    fun `additionalScopes is removable`() {
        extension.additionalScopes -= setOf("test")

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(
                setOf(
                    ResolveScope.Addition("androidTest")
                ), additionalScopes
            )
        }
    }

    @Test
    fun `targetConfigurations is appendable`() {
        extension.targetConfigurations += setOf("abc", "xyz")

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(
                ArtifactManagement.CommonConfigurationNames +
                    setOf(
                        "abc",
                        "xyz"
                    ), configurationNames
            )
        }
    }

    @Test
    fun `targetConfigurations is removable`() {
        extension.targetConfigurations -= setOf("api", "compile")

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(
                setOf(
                    "compileOnly",
                    "implementation",
                    "annotationProcessor",
                    "kapt"
                ), configurationNames
            )
        }
    }

    @Test
    fun `excludeGroups is appendable`() {
        extension.excludeGroups += setOf("abc", "xyz")

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(
                setOf(
                    "abc",
                    "xyz"
                ), excludeGroups
            )
        }
    }

    @Test
    fun `excludeGroups is removable`() {
        extension.excludeGroups += setOf("abc", "xyz")
        extension.excludeGroups -= setOf("xyz")

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(
                setOf(
                    "abc"
                ), excludeGroups
            )
        }
    }

    @Test
    fun `excludeArtifacts is appendable`() {
        extension.excludeArtifacts += setOf("abc:xyz")

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(
                setOf(
                    "abc:xyz"
                ), excludeArtifacts
            )
        }
    }

    @Test
    fun `excludeArtifacts is removable`() {
        extension.excludeArtifacts += setOf("abc:xyz")
        extension.excludeArtifacts -= setOf("abc:xyz")

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(
                emptySet(), excludeArtifacts
            )
        }
    }
}
