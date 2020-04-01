package io.github.jmatsu.spthanks.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.spthanks.SpecialThanksExtension
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.testfixtures.ProjectBuilder

class InitLicenseListTaskTest {
    lateinit var project: Project
    lateinit var extension: SpecialThanksExtension

    @BeforeTest
    fun before() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply("special-thanks")
        extension = requireNotNull(project.extensions.findByType(SpecialThanksExtension::class))
    }

    @Test
    fun `the task is generate-able`() {
        val variant = mockk<ApplicationVariant>()

        val task = project.tasks.create("sample", InitLicenseListTask::class.java, extension, variant)

        assertTrue(task.isEnabled)
    }
}
