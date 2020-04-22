package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.internal.ArtifactIgnoreParser
import io.github.jmatsu.license.internal.ArtifactManagement
import io.github.jmatsu.license.internal.IgnorePredicate
import io.github.jmatsu.license.model.ResolveScope
import io.github.jmatsu.license.model.ResolvedArtifact
import io.github.jmatsu.license.presentation.AssembleeData
import io.github.jmatsu.license.presentation.Assembler
import io.github.jmatsu.license.presentation.Convention
import io.github.jmatsu.license.presentation.Disassembler
import io.github.jmatsu.license.presentation.Merger
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
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

class MergeLicenseListTaskTest {
    lateinit var project: Project
    lateinit var extension: LicenseListExtension

    @BeforeTest
    fun before() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.jmatsu.license-list")
        extension = requireNotNull(project.extensions.findByType(LicenseListExtension::class))
    }

    @Test
    fun `the task is generate-able`() {
        val variant = mockk<ApplicationVariant>()

        val task = project.tasks.create("sample", MergeLicenseListTask::class.java, extension, variant)

        assertTrue(task.isEnabled)
    }

    @Test
    fun `verify method calls`() {
        mockkConstructor(ArtifactIgnoreParser::class, ArtifactManagement::class, Merger::class, Assembler::class, Disassembler::class)
        mockkStatic("kotlin.io.FilesKt__FileReadWriteKt")

        val additionalScopes: Set<ResolveScope.Addition> = mockk()
        val variantScope: ResolveScope.Variant = mockk()
        val assemblyFormat: StringFormat = mockk()
        val assemblyStyle: Assembler.Style = mockk()
        val artifactsText = "artifactsText"
        val catalogText = "catalogText"

        val args: MergeLicenseListTask.Args = mockk {
            every { assembleOutputDir } returns mockk {
                every { mkdirs() } returns true
            }
            every { assembledArtifactsFile.exists() } returns false
            every { assembledLicenseCatalogFile.exists() } returns false
            every { this@mockk.additionalScopes } returns additionalScopes
            every { this@mockk.variantScope } returns variantScope
            every { this@mockk.assemblyFormat } returns assemblyFormat
            every { this@mockk.assemblyStyle } returns assemblyStyle
            every { configurationNames } returns setOf()
            every { ignoreFile } returns mockk()
        }

        val ignoreFormat: ArtifactIgnoreParser.Format = ArtifactIgnoreParser.Format.Regex
        val ignorePredicate: IgnorePredicate = { _, _ -> false }
        val analyzedResult: SortedMap<ResolveScope, List<ResolvedArtifact>> = emptyMap<ResolveScope, List<ResolvedArtifact>>()
            .toSortedMap(kotlin.Comparator { t, t2 -> t.hashCode().compareTo(t2.hashCode()) })
        val mergedResult = AssembleeData(scopedArtifacts = emptyMap(), licenses = emptyList())
        val assembledArtifacts = "assembledArtifacts"
        val assembledLicenses = "assembledLicenses"

        every {
            anyConstructed<ArtifactIgnoreParser>().buildPredicate(ignoreFormat)
        } returns ignorePredicate

        every {
            anyConstructed<Disassembler>().disassembleArtifacts(any())
        } returns mapOf()
        every {
            anyConstructed<Disassembler>().disassemblePlainLicenses(any())
        } returns listOf()

        every {
            anyConstructed<ArtifactManagement>().analyze(
                additionalScopes = any(),
                variantScope = any()
            )
        } returns analyzedResult

        every {
            anyConstructed<Merger>().merge()
        } returns mergedResult

        every {
            anyConstructed<Assembler>().assembleArtifacts(
                format = any(),
                style = any()
            )
        } returns assembledArtifacts

        every {
            anyConstructed<Assembler>().assemblePlainLicenses(
                format = any()
            )
        } returns assembledLicenses

        every { args.ignoreFormat } returns ignoreFormat
        every { args.assembledArtifactsFile.readText() } returns artifactsText
        every { args.assembledLicenseCatalogFile.readText() } returns catalogText

        every { args.assembledArtifactsFile.writeText(any()) } just (Runs)
        every { args.assembledLicenseCatalogFile.writeText(any()) } just (Runs)

        MergeLicenseListTask.Executor(
            project = project,
            args = args
        )

        verify {
            anyConstructed<ArtifactIgnoreParser>().buildPredicate(ignoreFormat)
            anyConstructed<ArtifactManagement>().analyze(
                additionalScopes = additionalScopes,
                variantScope = variantScope
            )
            anyConstructed<Disassembler>().disassembleArtifacts(artifactsText)
            anyConstructed<Disassembler>().disassemblePlainLicenses(catalogText)
            anyConstructed<Merger>().merge()
            anyConstructed<Assembler>().assembleArtifacts(
                format = assemblyFormat,
                style = assemblyStyle
            )
            anyConstructed<Assembler>().assemblePlainLicenses(
                format = Convention.Yaml.Assembly
            )

            args.assembledArtifactsFile.writeText(assembledArtifacts)
            args.assembledLicenseCatalogFile.writeText(assembledLicenses)
        }

        unmockkAll()
    }
}
