package io.github.jmatsu.license.tasks.internal

import io.github.jmatsu.license.LicenseListExtension
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.testfixtures.ProjectBuilder

class BaseTaskTest {
    lateinit var project: Project
    lateinit var extension: LicenseListExtension

    @BeforeTest
    fun before() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.jmatsu.license-list")
        extension = requireNotNull(project.extensions.findByType(LicenseListExtension::class))
    }

    @Test
    fun `extension's isEnabled should disable tasks`() {
        extension.isEnabled = false

        val task = project.tasks.create("sample", BaseTask::class, extension)

        assertFalse(task.isEnabled)
    }
}
