package io.github.jmatsu.spthanks

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import io.github.jmatsu.spthanks.tasks.CreateLicenseListTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging

val globalLogger = Logging.getLogger("SpecialThanksPlugin")

class SpecialThanksPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.add("spthanks", SpecialThanksExtension::class.java)

        project.plugins.withType(AppPlugin::class.java) {
            val extension = requireNotNull(project.extensions.getByType(SpecialThanksExtension::class.java))

            project.tasks.register("createLicenseList", CreateLicenseListTask::class.java, extension, null).configure {
                description = """
                    |Create a license list based on the configuration
                """.trimMargin()
            }

            val androidExtension = requireNotNull(project.extensions.findByType(AppExtension::class.java))
            androidExtension.applicationVariants.whenObjectAdded {
                val variantName = name

                project.tasks.register("createLicenseList${variantName}", CreateLicenseListTask::class.java, extension, this).configure {
                    description = """
                        |Create a license list based on the configuration for $variantName
                    """.trimMargin()
                }
            }
        }

        project.afterEvaluate {
            if (!plugins.hasPlugin(AppPlugin::class.java)) {
                error("special-thanks plugin requires android plugin")
            }
        }
    }
}
