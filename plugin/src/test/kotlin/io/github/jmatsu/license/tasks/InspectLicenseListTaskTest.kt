package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.model.SourceProvider
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.presentation.Assembler
import io.github.jmatsu.license.presentation.Disassembler
import io.github.jmatsu.license.presentation.Inspector
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.serialization.StringFormat
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectLicenseListTaskTest {
    lateinit var project: Project
    lateinit var extension: LicenseListExtension
    lateinit var variant: ApplicationVariant
    lateinit var assetDirs: MutableList<File>
    lateinit var args: InspectLicenseListTask.Args

    @BeforeTest
    fun before() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.jmatsu.license-list")
        extension = requireNotNull(project.extensions.findByType(LicenseListExtension::class))
        assetDirs = mutableListOf()
        variant =
            mockk {
                every { name } returns "featureRelease"
                every { sourceSets } returns
                    listOf(
                        mockk<SourceProvider> {
                            every { assetsDirectories } returns assetDirs
                        },
                    )
            }
        args = InspectLicenseListTask.Args(project, extension, variant)
    }

    @Test
    fun `the task is generate-able`() {
        val task = project.tasks.create("sample", InspectLicenseListTask::class.java, extension, variant)

        assertTrue(task.isEnabled)
    }

    @Test
    fun `verify method calls`() {
        mockkConstructor(Inspector::class, Disassembler::class)
        mockkStatic("kotlin.io.FilesKt__FileReadWriteKt")

        val assemblyFormat: StringFormat = mockk()
        val assemblyStyle: Assembler.Style = mockk()
        val artifactsText = "artifactsText"
        val catalogText = "catalogText"

        val args: InspectLicenseListTask.Args =
            mockk {
                every { assembledArtifactsFile.exists() } returns true
                every { assembledLicenseCatalogFile.exists() } returns true
                every { this@mockk.assemblyFormat } returns assemblyFormat
                every { this@mockk.assemblyStyle } returns assemblyStyle
            }

        every {
            anyConstructed<Disassembler>().disassembleArtifacts(any())
        } returns mapOf()
        every {
            anyConstructed<Disassembler>().disassemblePlainLicenses(any())
        } returns listOf()
        every {
            anyConstructed<Inspector>().inspectArtifacts()
        } returns listOf()
        every {
            anyConstructed<Inspector>().inspectLicenses()
        } returns listOf()
        every {
            anyConstructed<Inspector>().inspectAssociations()
        } returns Inspector.AssociationResult(emptyList(), emptyList())

        every { args.assembledArtifactsFile.readText() } returns artifactsText
        every { args.assembledLicenseCatalogFile.readText() } returns catalogText

        InspectLicenseListTask.Executor(
            args = args,
            logger =
                mockk {
                    every { error(any()) } just Runs
                    every { warn(any()) } just Runs
                },
        )

        verify {
            anyConstructed<Disassembler>().disassembleArtifacts(artifactsText)
            anyConstructed<Disassembler>().disassemblePlainLicenses(catalogText)
            anyConstructed<Inspector>().inspectArtifacts()
            anyConstructed<Inspector>().inspectLicenses()
            anyConstructed<Inspector>().inspectAssociations()

            args.assembledArtifactsFile.readText()
            args.assembledLicenseCatalogFile.readText()
        }

        unmockkAll()
    }
}
