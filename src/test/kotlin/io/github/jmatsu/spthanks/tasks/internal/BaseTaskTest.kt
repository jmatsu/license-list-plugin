package io.github.jmatsu.spthanks.tasks.internal

import io.github.jmatsu.spthanks.SpecialThanksExtension
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.testfixtures.ProjectBuilder

class BaseTaskTest {
    lateinit var project: Project
    lateinit var extension: SpecialThanksExtension

    @BeforeTest
    fun before() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply("special-thanks")
        extension = requireNotNull(project.extensions.findByType(SpecialThanksExtension::class))
    }

    @Test
    fun `extension's isEnabled should disable tasks`() {
        extension.isEnabled = false

        val task = project.tasks.create("sample", BaseTask::class, extension)

        assertFalse(task.isEnabled)
    }
}
