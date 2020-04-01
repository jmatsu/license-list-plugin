package io.github.jmatsu.spthanks.tasks.internal

import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.model.BuildType
import com.android.builder.model.ProductFlavor
import io.github.jmatsu.spthanks.SpecialThanksExtension
import io.github.jmatsu.spthanks.internal.ArtifactManagement
import io.github.jmatsu.spthanks.model.ResolveScope
import io.github.jmatsu.spthanks.presentation.Assembler
import io.github.jmatsu.spthanks.presentation.Convention
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
        extension: SpecialThanksExtension,
        variant: ApplicationVariant
    ) : ReadWriteLicenseTaskArgs(
        project = project,
        extension = extension,
        variant = variant
    )

    lateinit var project: ProjectInternal
    lateinit var extension: SpecialThanksExtension
    lateinit var variant: ApplicationVariant

    @BeforeTest
    fun before() {
        project = ProjectBuilder.builder().build() as ProjectInternal
        project.plugins.apply("special-thanks")
        extension = requireNotNull(project.extensions.findByType(SpecialThanksExtension::class))
        variant = mockk {
            every { productFlavors } returns listOf(
                mockk<ProductFlavor> {
                    every { name } returns "feature"
                }
            )
            every { buildType } returns mockk<BuildType> {
                every { name } returns "release"
            }
        }
    }

    @Test
    fun `build default args`() {
        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(Assembler.Style.StructuredWithScope, style)
            assertEquals(Convention.Yaml, format)
            assertEquals("yml", ext)
            assertEquals(ArtifactManagement.CommonConfigurationNames, configurationNames)
            assertEquals(setOf(ResolveScope.Variant("feature"), ResolveScope.Variant("release")), variantScopes)
            assertEquals(
                setOf(
                    ResolveScope.Addition("test"),
                    ResolveScope.Addition("androidTest")
                ), additionalScopes
            )
            assertEquals(File(project.projectDir, "license.yml"), artifactsFile)
            assertEquals(project.projectDir, outputDir)
            assertEquals(File(project.projectDir, "license-catalog.yml"), catalogFile)
            assertEquals(emptySet(), excludeGroups)
            assertEquals(emptySet(), excludeArtifacts)
        }
    }

    @Test
    fun `withScope is false`() {
        extension.withScope = false

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(Assembler.Style.StructuredWithoutScope, style)
        }
    }

    @Test
    fun `assembleStyle is flatten`() {
        extension.assembleStyle = "flatten"

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(Assembler.Style.Flatten, style)
        }
    }

    @Test
    fun `assembleFormat is json`() {
        extension.assembleFormat = "json"

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(Convention.Json, format)
            assertEquals("json", ext)
            assertEquals(File(project.projectDir, "license.json"), artifactsFile)
            assertEquals(File(project.projectDir, "license-catalog.yml"), catalogFile)
        }
    }

    @Test
    fun `targetVariants is appendable but no impact`() {
        extension.targetVariants += setOf("abc", "xyz")

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(Assembler.Style.StructuredWithScope, style)
            assertEquals(Convention.Yaml, format)
            assertEquals("yml", ext)
            assertEquals(ArtifactManagement.CommonConfigurationNames, configurationNames)
            assertEquals(setOf(ResolveScope.Variant("feature"), ResolveScope.Variant("release")), variantScopes)
            assertEquals(
                setOf(
                    ResolveScope.Addition("test"),
                    ResolveScope.Addition("androidTest")
                ), additionalScopes
            )
            assertEquals(File(project.projectDir, "license.yml"), artifactsFile)
            assertEquals(project.projectDir, outputDir)
            assertEquals(File(project.projectDir, "license-catalog.yml"), catalogFile)
            assertEquals(emptySet(), excludeGroups)
            assertEquals(emptySet(), excludeArtifacts)
        }
    }

    @Test
    fun `targetVariants is removable but no impact`() {
        extension.targetVariants += setOf("abc", "xyz")
        extension.targetVariants -= setOf("abc")

        val args: ReadWriteLicenseTaskArgs = TestArgs(project, extension, variant)

        with(args) {
            assertEquals(Assembler.Style.StructuredWithScope, style)
            assertEquals(Convention.Yaml, format)
            assertEquals("yml", ext)
            assertEquals(ArtifactManagement.CommonConfigurationNames, configurationNames)
            assertEquals(setOf(ResolveScope.Variant("feature"), ResolveScope.Variant("release")), variantScopes)
            assertEquals(
                setOf(
                    ResolveScope.Addition("test"),
                    ResolveScope.Addition("androidTest")
                ), additionalScopes
            )
            assertEquals(File(project.projectDir, "license.yml"), artifactsFile)
            assertEquals(project.projectDir, outputDir)
            assertEquals(File(project.projectDir, "license-catalog.yml"), catalogFile)
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
                    "implementation"
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
