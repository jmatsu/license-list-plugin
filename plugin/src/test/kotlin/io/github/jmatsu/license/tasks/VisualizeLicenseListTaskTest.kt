package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.model.BuildType
import com.android.builder.model.ProductFlavor
import com.android.builder.model.SourceProvider
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.presentation.encoder.Html
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.testfixtures.ProjectBuilder

class VisualizeLicenseListTaskTest {
    lateinit var project: Project
    lateinit var extension: LicenseListExtension
    lateinit var variant: ApplicationVariant
    lateinit var assetDirs: MutableList<File>

    @BeforeTest
    fun before() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply("license-list")
        extension = requireNotNull(project.extensions.findByType(LicenseListExtension::class))
        assetDirs = mutableListOf()
        variant = mockk {
            every { name } returns "featureRelease"
            every { productFlavors } returns listOf(
                mockk<ProductFlavor> {
                    every { name } returns "feature"
                }
            )
            every { buildType } returns mockk<BuildType> {
                every { name } returns "release"
            }
            every { sourceSets } returns listOf(
                mockk<SourceProvider> {
                    every { assetsDirectories } returns assetDirs
                }
            )
        }
    }

    @Test
    fun `the task is generate-able`() {
        val task = project.tasks.create("sample", VisualizeLicenseListTask::class.java, extension, variant)

        assertTrue(task.isEnabled)
    }

    @Test
    fun `args should expose visualization stuff`() {
        val args = VisualizeLicenseListTask.Args(project, extension, variant)

        assertEquals("html", args.visualizedFileExt)
        assertTrue(args.visualizeFormat is Html)
        assertEquals(args.visualizeOutputDir, project.projectDir)
    }

    @Test
    fun `args should use extension as outputDir`() {
        val outputDir: File = mockk(relaxed = true)
        extension.outputDir = outputDir

        val args = VisualizeLicenseListTask.Args(project, extension, variant)

        assetDirs.add(mockk())

        assertEquals(outputDir, args.visualizeOutputDir)
    }

    @Test
    fun `args should use asset dir as outputDir`() {
        val outputDir: File = mockk(relaxed = true) {
            every { absolutePath } returns "/path/to/featureRelease/assets"
        }

        assetDirs.add(outputDir)

        val args = VisualizeLicenseListTask.Args(project, extension, variant)

        assertEquals(outputDir, args.visualizeOutputDir)
    }

    @Test
    fun `args should not use asset dir as outputDir`() {
        val outputDir: File = mockk(relaxed = true) {
            every { absolutePath } returns "/path/to/anyother/assets"
        }

        assetDirs.add(outputDir)

        val args = VisualizeLicenseListTask.Args(project, extension, variant)

        assertEquals(project.projectDir, args.visualizeOutputDir)
    }
}
