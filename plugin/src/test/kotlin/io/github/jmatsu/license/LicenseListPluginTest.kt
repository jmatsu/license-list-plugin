/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package io.github.jmatsu.license

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.kotlin.dsl.findByType
import org.gradle.testfixtures.ProjectBuilder

class LicenseListPluginTest {

    lateinit var project: ProjectInternal

    @BeforeTest
    fun before() {
        project = ProjectBuilder.builder().build() as ProjectInternal
    }

    @Test
    fun `plugin create an extension`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.android.application")
        project.plugins.apply("io.github.jmatsu.license-list")

        val extension = project.extensions.findByType(LicenseListExtension::class)

        assertNotNull(extension)
        assertTrue {
            extension == project.extensions.findByName("licenseList")
        }
    }
}
