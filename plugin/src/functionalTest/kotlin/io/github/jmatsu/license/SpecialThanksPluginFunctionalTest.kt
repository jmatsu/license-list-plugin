package io.github.jmatsu.license

import io.github.jmatsu.license.helper.MinimumProject
import io.github.jmatsu.license.helper.setupProject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SpecialThanksPluginFunctionalTest {
    lateinit var project: MinimumProject

    @BeforeTest
    fun setup() {
        project = setupProject()
    }

    @AfterTest
    fun cleanup() {
        project.projectDir.deleteRecursively()
    }

    @Test
    fun `can run task`() {
        // TODO resolve missing AppPlugin error
    }
}
