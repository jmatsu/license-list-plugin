package io.github.jmatsu.license

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import io.github.jmatsu.license.tasks.InitLicenseListTask
import io.github.jmatsu.license.tasks.MergeLicenseListTask
import io.github.jmatsu.license.tasks.ValidateLicenseListTask
import io.github.jmatsu.license.tasks.VisualizeLicenseListTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger

class LicenseListPlugin : Plugin<Project> {
    companion object {
        var logger: Logger? = null
    }

    override fun apply(project: Project) {
        logger = project.logger

        project.extensions.add("licenseList", LicenseListExtension::class.java)

        project.plugins.withType(AppPlugin::class.java) {
            val extension = requireNotNull(project.extensions.getByType(LicenseListExtension::class.java))
            val androidExtension = requireNotNull(project.extensions.findByType(AppExtension::class.java))

            // Do not read values of the extension because it may not be reflected yet

            androidExtension.applicationVariants.whenObjectAdded {
                val variantName = name

                project.tasks.register("init${variantName.capitalize()}LicenseList", InitLicenseListTask::class.java, extension, this).configure {
                    description = """
                        |Initialize a license list based on the configuration for $variantName
                    """.trimMargin()
                }

                project.tasks.register("validate${variantName.capitalize()}LicenseList", ValidateLicenseListTask::class.java, extension, this).configure {
                    description = """
                        |Validate the existing license list based on the configuration for $variantName
                    """.trimMargin()
                }

                project.tasks.register("merge${variantName.capitalize()}LicenseList", MergeLicenseListTask::class.java, extension, this).configure {
                    description = """
                        |Merge the existing license list and the current license list that are retrieved from pom files based on the configuration for $variantName
                    """.trimMargin()
                }

                project.tasks.register("visualize${variantName.capitalize()}LicenseList", VisualizeLicenseListTask::class.java, extension, this).configure {
                    description = """
                        |Visualize the existing license list of $variantName as the given style
                    """.trimMargin()
                }

                if (extension.targetVariant == variantName) {
                    project.logger.info("$variantName has been matched to targetVariant.")

                    project.tasks.register("initLicenseList") {
                        dependsOn(project.tasks.findByName("init${variantName.capitalize()}LicenseList"))
                    }

                    project.tasks.register("validateLicenseList") {
                        dependsOn(project.tasks.findByName("validate${variantName.capitalize()}LicenseList"))
                    }

                    project.tasks.register("mergeLicenseList") {
                        dependsOn(project.tasks.findByName("merge${variantName.capitalize()}LicenseList"))
                    }

                    project.tasks.register("visualizeLicenseList") {
                        dependsOn(project.tasks.findByName("visualize${variantName.capitalize()}LicenseList"))
                    }
                }
            }
        }

        project.afterEvaluate {
            if (!plugins.hasPlugin(AppPlugin::class.java)) {
                error("license-list plugin requires android plugin")
            }
        }
    }
}
