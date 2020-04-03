package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.LicenseListExtension
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class VisualizeLicenseListTaskTest {
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

        val task = project.tasks.create("sample", VisualizeLicenseListTask::class.java, extension, variant)

        assertTrue(task.isEnabled)
    }
}
