package io.github.jmatsu.spthanks

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import io.github.jmatsu.spthanks.tasks.CreateLicenseListTask
import io.github.jmatsu.spthanks.tasks.MergeLicenseTask
import io.github.jmatsu.spthanks.tasks.ValidateLicenseTask
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

            project.tasks.register("validateLicenseList", ValidateLicenseTask::class.java, extension, null).configure {
                description = """
                    |Validate the existing license list based on the configuration
                """.trimMargin()
            }

            project.tasks.register("mergeLicenseList", MergeLicenseTask::class.java, extension, null).configure {
                description = """
                    |Merge the existing license list and the current guessed license list based on the configuration
                """.trimMargin()
            }

            val androidExtension = requireNotNull(project.extensions.findByType(AppExtension::class.java))
            androidExtension.applicationVariants.whenObjectAdded {
                val variantName = name

                project.tasks.register("createLicenseList${variantName.capitalize()}", CreateLicenseListTask::class.java, extension, this).configure {
                    description = """
                        |Create a license list based on the configuration for $variantName
                    """.trimMargin()
                }

                project.tasks.register("validateLicenseList${variantName.capitalize()}", ValidateLicenseTask::class.java, extension, this).configure {
                    description = """
                        |Validate the existing license list based on the configuration for $variantName
                    """.trimMargin()
                }

                project.tasks.register("mergeLicenseList${variantName.capitalize()}", MergeLicenseTask::class.java, extension, this).configure {
                    description = """
                        |Merge the existing license list and the current guessed license list based on the configuration for $variantName
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
