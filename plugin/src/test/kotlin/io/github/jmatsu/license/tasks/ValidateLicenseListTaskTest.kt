package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.internal.ArtifactIgnoreParser
import io.github.jmatsu.license.internal.ArtifactManagement
import io.github.jmatsu.license.model.ResolveScope
import io.github.jmatsu.license.model.ResolvedArtifact
import io.github.jmatsu.license.presentation.Assembler
import io.github.jmatsu.license.presentation.Disassembler
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import java.util.SortedMap
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.serialization.StringFormat
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.testfixtures.ProjectBuilder

class ValidateLicenseListTaskTest {
    lateinit var project: Project
    lateinit var extension: LicenseListExtension

    @BeforeTest
    fun before() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply("license-list")
        extension = requireNotNull(project.extensions.findByType(LicenseListExtension::class))
    }

    @Test
    fun `the task is generate-able`() {
        val variant = mockk<ApplicationVariant>()

        val task = project.tasks.create("sample", ValidateLicenseListTask::class.java, extension, variant)

        assertTrue(task.isEnabled)
    }

    @Test
    fun `verify method calls`() {
        mockkConstructor(ArtifactIgnoreParser::class, ArtifactManagement::class, Assembler::class, Disassembler::class)
        mockkStatic("kotlin.io.FilesKt__FileReadWriteKt")

        val additionalScopes: Set<ResolveScope.Addition> = mockk()
        val variantScope: ResolveScope.Variant = mockk()
        val assemblyFormat: StringFormat = mockk()
        val assemblyStyle: Assembler.Style = mockk()
        val artifactsText = "artifactsText"
        val catalogText = "catalogText"

        val args: ValidateLicenseListTask.Args = mockk {
            every { assembledArtifactsFile.exists() } returns true
            every { assembledLicenseCatalogFile.exists() } returns true
            every { this@mockk.additionalScopes } returns additionalScopes
            every { this@mockk.variantScope } returns variantScope
            every { this@mockk.assemblyFormat } returns assemblyFormat
            every { this@mockk.assemblyStyle } returns assemblyStyle
            every { configurationNames } returns setOf()
            every { ignoreFile } returns mockk()
        }

        val regex: Regex = mockk()
        val analyzedResult: SortedMap<ResolveScope, List<ResolvedArtifact>> = mockk()
        val assembledArtifacts = "assembledArtifacts"
        val assembledLicenses = "assembledLicenses"

        every {
            anyConstructed<ArtifactIgnoreParser>().parse()
        } returns regex

        every {
            anyConstructed<Assembler>().transformForFlatten()
        } returns listOf()

        every {
            anyConstructed<Disassembler>().disassembleArtifacts(any())
        } returns listOf()
        every {
            anyConstructed<Disassembler>().disassemblePlainLicenses(any())
        } returns listOf()

        every {
            anyConstructed<ArtifactManagement>().analyze(
                additionalScopes = any(),
                variantScope = any()
            )
        } returns analyzedResult

        every { args.assembledArtifactsFile.readText() } returns artifactsText
        every { args.assembledLicenseCatalogFile.readText() } returns catalogText

        ValidateLicenseListTask.Executor(
            project = project,
            args = args,
            logger = mockk(relaxed = true, relaxUnitFun = true)
        )

        verify {
            anyConstructed<ArtifactIgnoreParser>().parse()
            anyConstructed<ArtifactManagement>().analyze(
                additionalScopes = additionalScopes,
                variantScope = variantScope
            )
            anyConstructed<Disassembler>().disassembleArtifacts(artifactsText)
            anyConstructed<Disassembler>().disassemblePlainLicenses(catalogText)
            anyConstructed<Assembler>().transformForFlatten()

            args.assembledArtifactsFile.readText()
            args.assembledLicenseCatalogFile.readText()
        }

        unmockkAll()
    }
}
