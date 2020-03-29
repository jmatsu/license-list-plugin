package io.github.jmatsu.spthanks.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.spthanks.SpecialThanksExtension
import io.github.jmatsu.spthanks.dsl.FlattenStyle
import io.github.jmatsu.spthanks.dsl.JsonFormat
import io.github.jmatsu.spthanks.dsl.StructuredStyle
import io.github.jmatsu.spthanks.dsl.YamlFormat
import io.github.jmatsu.spthanks.internal.ArtifactManagement
import io.github.jmatsu.spthanks.model.ResolveScope
import io.github.jmatsu.spthanks.presentation.Assembler
import io.github.jmatsu.spthanks.tasks.internal.VariantAwareTask
import kotlinx.serialization.StringFormat
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

abstract class CreateLicenseListTask
@Inject constructor(
        extension: SpecialThanksExtension,
        variant: ApplicationVariant?
) : VariantAwareTask(extension, variant) {

    @TaskAction
    fun execute() {
        val args = Args.Builder.build(project, extension, variant)

        val artifactManagement = ArtifactManagement(
                project = project,
                configurationNames = args.configurationNames
        )
        val scopedResolvedArtifacts = artifactManagement.analyze(
                variantScopes = args.variantScopes,
                additionalScopes = args.additionalScopes
        )
        val assembler = Assembler(
                resolvedArtifactMap = scopedResolvedArtifacts
        )

        val text = assembler.assemble(args.style, args.format)

        args.licenseFile.writeText(text)
    }

    data class Args(
            val format: StringFormat,
            val style: Assembler.Style,
            val configurationNames: Set<String>,
            val variantScopes: Set<ResolveScope.Variant>,
            val additionalScopes: Set<ResolveScope.Addition>,
            val licenseFile: File
    ) {
        object Builder {
            fun build(project: Project, extension: SpecialThanksExtension, variant: ApplicationVariant?): Args {
                val format: StringFormat = when (extension.assembleFormat) {
                    JsonFormat -> Assembler.Json
                    YamlFormat -> Assembler.Yaml
                    else -> throw IllegalArgumentException("Only one of $FlattenStyle or $StructuredStyle are allowed.")
                }

                val ext: String = when (extension.assembleFormat) {
                    JsonFormat -> "json"
                    YamlFormat -> "yml"
                    else -> error("nothing has come")
                }

                val style: Assembler.Style = when (extension.assembleStyle) {
                    FlattenStyle -> Assembler.Style.Flatten
                    StructuredStyle -> {
                        if (extension.withScope) {
                            Assembler.Style.StructuredWithScope
                        } else {
                            Assembler.Style.StructuredWithoutScope
                        }
                    }
                    else -> throw IllegalArgumentException("Only one of $FlattenStyle or $StructuredStyle are allowed.")
                }

                val configurationNames: Set<String> = extension.targetConfigurations

                require(configurationNames.isNotEmpty()) {
                    "targetConfigurations must has at least one element"
                }

                val variantScopes: Set<ResolveScope.Variant> = when {
                    variant != null -> {
                        val build = variant.buildType.name
                        val flavors = variant.productFlavors.map { it.name }

                        (listOf(build) + flavors).map { ResolveScope.Variant(it) }.toSet()
                    }
                    else -> extension.targetVariants.map { ResolveScope.Variant(it) }.toSet()
                }

                val additionalScopes: Set<ResolveScope.Addition> = extension.additionalScopes.map { ResolveScope.Addition(it) }.toSet()

                val licenseFile: File = extension.licenseFile ?: File(project.projectDir, "license.$ext")

                return Args(
                        format = format,
                        style = style,
                        configurationNames = configurationNames,
                        variantScopes = variantScopes,
                        additionalScopes = additionalScopes,
                        licenseFile = licenseFile
                )
            }
        }
    }
}