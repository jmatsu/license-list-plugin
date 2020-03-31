package io.github.jmatsu.spthanks

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import io.github.jmatsu.spthanks.tasks.InitLicenseListTask
import io.github.jmatsu.spthanks.tasks.MergeLicenseTask
import io.github.jmatsu.spthanks.tasks.ValidateLicenseTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class SpecialThanksPlugin : Plugin<Project> {
    companion object {
        var logger: Logger? = null
    }

    override fun apply(project: Project) {
        logger = project.logger

        project.extensions.add("spthanks", SpecialThanksExtension::class.java)

        project.plugins.withType(AppPlugin::class.java) {
            val extension = requireNotNull(project.extensions.getByType(SpecialThanksExtension::class.java))

            project.tasks.register("initLicenseList", InitLicenseListTask::class.java, extension, null).configure {
                description = """
                    |Initialize a license list based on the configuration
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

                project.tasks.register("initLicenseList${variantName.capitalize()}", InitLicenseListTask::class.java, extension, this).configure {
                    description = """
                        |Initialize a license list based on the configuration for $variantName
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
