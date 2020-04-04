package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.model.SourceProvider
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.presentation.Assembler
import io.github.jmatsu.license.presentation.Disassembler
import io.github.jmatsu.license.presentation.Visualizer
import io.github.jmatsu.license.presentation.encoder.Html
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.StringFormat
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.testfixtures.ProjectBuilder

class VisualizeLicenseListTaskTest {
    lateinit var project: Project
    lateinit var extension: LicenseListExtension
    lateinit var variant: ApplicationVariant
    lateinit var assetDirs: MutableList<File>
    lateinit var args: VisualizeLicenseListTask.Args

    @BeforeTest
    fun before() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply("license-list")
        extension = requireNotNull(project.extensions.findByType(LicenseListExtension::class))
        assetDirs = mutableListOf()
        variant = mockk {
            every { name } returns "featureRelease"
            every { sourceSets } returns listOf(
                mockk<SourceProvider> {
                    every { assetsDirectories } returns assetDirs
                }
            )
        }
        args = VisualizeLicenseListTask.Args(project, extension, variant)
    }

    @Test
    fun `the task is generate-able`() {
        val task = project.tasks.create("sample", VisualizeLicenseListTask::class.java, extension, variant)

        assertTrue(task.isEnabled)
    }

    @Test
    fun `args should expose visualization stuff`() {
        assertEquals("html", args.visualizedFileExt)
        assertTrue(args.visualizationFormat is Html)
        assertEquals(args.visualizeOutputDir, project.projectDir)
    }

    @Test
    fun `args should use extension as outputDir`() {
        val outputDir: File = mockk(relaxed = true)
        args.variantAwareOptions.visualization.outputDir = outputDir

        assetDirs.add(mockk())

        assertEquals(outputDir, args.visualizeOutputDir)
    }

    @Test
    fun `args should use asset dir as outputDir`() {
        val outputDir: File = mockk(relaxed = true) {
            every { absolutePath } returns "/path/to/featureRelease/assets"
        }

        assetDirs.add(outputDir)

        assertEquals(outputDir, args.visualizeOutputDir)
    }

    @Test
    fun `args should not use asset dir as outputDir`() {
        val outputDir: File = mockk(relaxed = true) {
            every { absolutePath } returns "/path/to/anyother/assets"
        }

        assetDirs.add(outputDir)

        assertEquals(project.projectDir, args.visualizeOutputDir)
    }

    @Test
    fun `verify method calls`() {
        mockkConstructor(Visualizer::class, Disassembler::class)
        mockkStatic("kotlin.io.FilesKt__FileReadWriteKt")

        val visualizationFormat: StringFormat = mockk()
        val assemblyFormat: StringFormat = mockk()
        val assemblyStyle: Assembler.Style = mockk()
        val artifactsText = "artifactsText"
        val catalogText = "catalogText"
        val visualizedText = "visualizedText"

        val args: VisualizeLicenseListTask.Args = mockk {
            every { assembledArtifactsFile.exists() } returns true
            every { assembledLicenseCatalogFile.exists() } returns true
            every { this@mockk.assemblyFormat } returns assemblyFormat
            every { this@mockk.assemblyStyle } returns assemblyStyle
            every { this@mockk.visualizationFormat } returns visualizationFormat
            every { visualizeOutputDir } returns mockk {
                every { mkdirs() } returns true
            }
        }

        every {
            anyConstructed<Disassembler>().disassembleArtifacts(any())
        } returns listOf()
        every {
            anyConstructed<Disassembler>().disassemblePlainLicenses(any())
        } returns listOf()
        every {
            anyConstructed<Visualizer>().visualizeArtifacts(any())
        } returns visualizedText

        every { args.assembledArtifactsFile.readText() } returns artifactsText
        every { args.assembledLicenseCatalogFile.readText() } returns catalogText
        every { args.visualizedFile.writeText(any()) } just Runs

        VisualizeLicenseListTask.Executor(
            args = args
        )

        verify {
            anyConstructed<Disassembler>().disassembleArtifacts(artifactsText)
            anyConstructed<Disassembler>().disassemblePlainLicenses(catalogText)
            anyConstructed<Visualizer>().visualizeArtifacts(visualizationFormat)

            args.assembledArtifactsFile.readText()
            args.assembledLicenseCatalogFile.readText()
            args.visualizedFile.writeText(visualizedText)
        }

        unmockkAll()
    }
}
