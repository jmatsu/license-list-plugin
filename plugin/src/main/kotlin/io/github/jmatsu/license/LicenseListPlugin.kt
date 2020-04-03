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
            val targetVariantName = extension.targetVariants.joinToString("") { it.capitalize() }.decapitalize()

            val androidExtension = requireNotNull(project.extensions.findByType(AppExtension::class.java))
            androidExtension.applicationVariants.whenObjectAdded {
                val variantName = name

                project.tasks.register("initLicenseList${variantName.capitalize()}", InitLicenseListTask::class.java, extension, this).configure {
                    description = """
                        |Initialize a license list based on the configuration for $variantName
                    """.trimMargin()
                }

                project.tasks.register("validateLicenseList${variantName.capitalize()}", ValidateLicenseListTask::class.java, extension, this).configure {
                    description = """
                        |Validate the existing license list based on the configuration for $variantName
                    """.trimMargin()
                }

                project.tasks.register("mergeLicenseList${variantName.capitalize()}", MergeLicenseListTask::class.java, extension, this).configure {
                    description = """
                        |Merge the existing license list and the current license list that are retrieved from pom files based on the configuration for $variantName
                    """.trimMargin()
                }

                project.tasks.register("visualizeLicenseList${variantName.capitalize()}", VisualizeLicenseListTask::class.java, extension, this).configure {
                    description = """
                        |Visualize the existing license list of $variantName as the given style
                    """.trimMargin()
                }

                if (targetVariantName == variantName) {
                    project.tasks.register("initLicenseList") {
                        dependsOn(project.tasks.findByName("initLicenseList${variantName.capitalize()}"))
                    }

                    project.tasks.register("validateLicenseList") {
                        dependsOn(project.tasks.findByName("validateLicenseList${variantName.capitalize()}"))
                    }

                    project.tasks.register("mergeLicenseList") {
                        dependsOn(project.tasks.findByName("mergeLicenseList${variantName.capitalize()}"))
                    }

                    project.tasks.register("visualizeLicenseList") {
                        dependsOn(project.tasks.findByName("visualizeLicenseList${variantName.capitalize()}"))
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
