package io.github.jmatsu.license

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.migration.MigrateLicenseToolsDefinitionTask
import io.github.jmatsu.license.tasks.InitLicenseListTask
import io.github.jmatsu.license.tasks.InspectLicenseListTask
import io.github.jmatsu.license.tasks.MergeLicenseListTask
import io.github.jmatsu.license.tasks.ValidateLicenseListTask
import io.github.jmatsu.license.tasks.VisualizeLicenseListTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.hasPlugin

class LicenseListPlugin : Plugin<Project> {
    companion object {
        var logger: Logger? = null
    }

    override fun apply(project: Project) {
        logger = project.logger

        if (project.plugins.hasPlugin(this::class)) {
            project.logger.warn("LicenseList plugin is applied multiple times so ignored the 2nd application")
            return
        }

        val assemblyOptionsContainer = project.container(AssemblyOptions::class) { name ->
            AssemblyOptionsImpl(name)
        }

        val visualizationOptionsContainer = project.container(VisualizationOptions::class) { name ->
            VisualizationOptionsImpl(name)
        }

        val variantAwareOptionsContainer = project.container(VariantAwareOptions::class) { name ->
            val assemblyOptions = assemblyOptionsContainer.create(name)
            val visualizationOptions = visualizationOptionsContainer.create(name)

            VariantAwareOptionsImpl(name, assemblyOptions, visualizationOptions)
        }

        project.extensions.create("licenseList", LicenseListExtension::class.java, variantAwareOptionsContainer)

        project.plugins.withId("com.cookpad.android.licensetools") {
            val toolsExtension = requireNotNull(project.extensions.findByName("licenseTools"))

            project.tasks.register(
                "migrateLicenseToolsDefinition",
                MigrateLicenseToolsDefinitionTask::class.java,
                requireNotNull(project.extensions.getByType(LicenseListExtension::class)),
                toolsExtension
            ).configure {
                description = """
                |Migrate license-tools-plugin configuration to this plugin's configuration
            """.trimMargin()
            }
        }

        project.plugins.withType(AppPlugin::class.java) {
            val extension = requireNotNull(project.extensions.getByType(LicenseListExtension::class))
            val androidExtension = requireNotNull(project.extensions.findByType(AppExtension::class))

            // Do not read values of the extension because it may not be reflected yet

            androidExtension.applicationVariants.whenObjectAdded {
                val variant = this
                val variantName = variant.name

                if (extension.variants.findByName(variantName) == null) {
                    project.logger.info("VariantAwareOptions($variantName) is missing")
                }

                project.registerTask<InitLicenseListTask>("init", variant = variant, extension = extension).configure {
                    description = """
                        |Initialize a license list based on the configuration for $variantName
                    """.trimMargin()
                }

                project.registerTask<ValidateLicenseListTask>("validate", variant = variant, extension = extension).configure {
                    description = """
                        |Validate the existing license list based on the configuration for $variantName
                    """.trimMargin()
                }

                project.registerTask<MergeLicenseListTask>("merge", variant = variant, extension = extension).configure {
                    description = """
                        |Merge the existing license list and the current license list that are retrieved from pom files based on the configuration for $variantName
                    """.trimMargin()
                }
                project.registerTask<VisualizeLicenseListTask>("visualize", variant = variant, extension = extension).configure {
                    description = """
                        |Visualize the existing license list of $variantName as the given style
                    """.trimMargin()
                }

                project.registerTask<InspectLicenseListTask>("inspect", variant = variant, extension = extension).configure {
                    description = """
                        |Inspect the existing license list of $variantName and report missing and/or unsatisfied attributes.
                    """.trimMargin()
                }

                if (extension.defaultVariant == variantName) {
                    project.logger.info("$variantName has been matched to targetVariant.")

                    fun alias(project: Project, action: String, variant: ApplicationVariant) {
                        project.tasks.register("${action.decapitalize()}LicenseList") {
                            dependsOn(project.tasks.findByName("${action.decapitalize()}${variant.name.capitalize()}LicenseList"))
                        }
                    }

                    arrayOf(
                        "init",
                        "validate",
                        "merge",
                        "visualize",
                        "inspect"
                    ).forEach { action ->
                        alias(project, action, variant)
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

    private inline fun <reified T : Task> Project.registerTask(action: String, variant: ApplicationVariant, extension: LicenseListExtension): TaskProvider<T> {
        return project.tasks.register("${action.decapitalize()}${variant.name.capitalize()}LicenseList", T::class.java, extension, variant)
    }
}
