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
import kotlin.test.assertSame
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
    lateinit var args: ReadWriteLicenseTaskArgs

    @BeforeTest
    fun before() {
        project = ProjectBuilder.builder().build() as ProjectInternal
        project.plugins.apply("license-list")
        extension = requireNotNull(project.extensions.findByType(LicenseListExtension::class))
        variant = mockk {
            every { name } returns "featureRelease"
        }
        args = TestArgs(project, extension, variant)
    }

    @Test
    fun `build default args`() {
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
    fun `variantAwareOptions check`() {
        val variantSpecificInstance = extension.variants.create("featureRelease")

        args = TestArgs(project, extension, variant)

        assertSame(variantSpecificInstance, args.variantAwareOptions)
    }

    @Test
    fun `assembly#groupByScopes is false`() {
        args.variantAwareOptions.assembly.groupByScopes = false

        with(args) {
            assertEquals(Assembler.Style.StructuredWithoutScope, assemblyStyle)
        }
    }

    @Test
    fun `assembly#style is flatten`() {
        args.variantAwareOptions.assembly.style = "flatten"

        with(args) {
            assertEquals(Assembler.Style.Flatten, assemblyStyle)
        }
    }

    @Test
    fun `assembly#format is json`() {
        args.variantAwareOptions.assembly.format = "json"

        with(args) {
            assertEquals(Convention.Json.Assembly, assemblyFormat)
            assertEquals("json", assembledFileExt)
            assertEquals(File(project.projectDir, "artifact-definition.json"), assembledArtifactsFile)
            assertEquals(File(project.projectDir, "license-catalog.yml"), assembledLicenseCatalogFile)
        }
    }

    @Test
    fun `defaultVariant is modifiable but no impact`() {
        extension.defaultVariant = "debug"

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
}
