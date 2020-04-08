package io.github.jmatsu.license.tasks.internal

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.AssemblyOptionsImpl
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.VariantAwareOptions
import io.github.jmatsu.license.VariantAwareOptionsImpl
import io.github.jmatsu.license.VisualizationOptionsImpl
import io.github.jmatsu.license.dsl.FlattenStyle
import io.github.jmatsu.license.dsl.JsonFormat
import io.github.jmatsu.license.dsl.StructuredStyle
import io.github.jmatsu.license.dsl.YamlFormat
import io.github.jmatsu.license.model.ResolveScope
import io.github.jmatsu.license.presentation.Assembler
import io.github.jmatsu.license.presentation.Convention
import java.io.File
import kotlinx.serialization.StringFormat
import org.gradle.api.Project

abstract class ReadWriteLicenseTaskArgs(
    private val project: Project,
    extension: LicenseListExtension,
    variant: ApplicationVariant
) {
    // only this variable should be resolved on initialization
    internal val variantAwareOptions: VariantAwareOptions =
        extension.variants.findByName(variant.name)
            ?: VariantAwareOptionsImpl(
                name = variant.name,
                assembly = AssemblyOptionsImpl(
                    name = variant.name
                ),
                visualization = VisualizationOptionsImpl(
                    name = variant.name
                )
            )

    val assemblyFormat: StringFormat by lazy {
        when (variantAwareOptions.assembly.format) {
            JsonFormat -> Convention.Json.Assembly
            YamlFormat -> Convention.Yaml.Assembly
            else -> throw IllegalArgumentException("Only one of $FlattenStyle or $StructuredStyle are allowed.")
        }
    }

    val assemblyStyle: Assembler.Style by lazy {
        when (variantAwareOptions.assembly.style) {
            FlattenStyle -> Assembler.Style.Flatten
            StructuredStyle -> {
                if (variantAwareOptions.assembly.groupByScopes) {
                    Assembler.Style.StructuredWithScope
                } else {
                    Assembler.Style.StructuredWithoutScope
                }
            }
            else -> throw IllegalArgumentException("Only one of $FlattenStyle or $StructuredStyle are allowed.")
        }
    }

    val assembledFileExt: String by lazy {
        when (variantAwareOptions.assembly.format) {
            JsonFormat -> "json"
            YamlFormat -> "yml"
            else -> error("nothing has come")
        }
    }

    val configurationNames: Set<String> by lazy {
        HashSet(variantAwareOptions.assembly.targetConfigurations)
    }

    val variantScope: ResolveScope.Variant = ResolveScope.Variant(variant.name)

    val additionalScopes: Set<ResolveScope.Addition> by lazy {
        variantAwareOptions.assembly.additionalScopes.map { ResolveScope.Addition(it) }.toSet()
    }

    val assembleOutputDir: File
        get() = variantAwareOptions.baseDir ?: project.projectDir
    val assembledArtifactsFile: File
        get() = File(assembleOutputDir, "artifact-definition.$assembledFileExt")
    val assembledLicenseCatalogFile: File
        get() = File(assembleOutputDir, "license-catalog.yml")

    val ignoreFile: File
        get() = File(assembleOutputDir, ".artifactignore")
}
