package io.github.jmatsu.license.tasks.internal

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.AssemblyOptionsImpl
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.VariantAwareOptions
import io.github.jmatsu.license.VariantAwareOptionsImpl
import io.github.jmatsu.license.VisualizationOptionsImpl
import io.github.jmatsu.license.dsl.FORMAT_JSON
import io.github.jmatsu.license.dsl.FORMAT_YAML
import io.github.jmatsu.license.dsl.IGNORE_FORMAT_GLOB
import io.github.jmatsu.license.dsl.IGNORE_FORMAT_REGEX
import io.github.jmatsu.license.dsl.STYLE_FLATTEN
import io.github.jmatsu.license.dsl.STYLE_STRUCTURED
import io.github.jmatsu.license.internal.ArtifactIgnoreParser
import io.github.jmatsu.license.model.ResolveScope
import io.github.jmatsu.license.presentation.Assembler
import io.github.jmatsu.license.presentation.Convention
import kotlinx.serialization.StringFormat
import org.gradle.api.Project
import java.io.File

abstract class ReadWriteLicenseTaskArgs(
    private val project: Project,
    extension: LicenseListExtension,
    variant: ApplicationVariant,
) {
    // only this variable should be resolved on initialization
    internal val variantAwareOptions: VariantAwareOptions =
        extension.variants.findByName(variant.name)
            ?: VariantAwareOptionsImpl(
                name = variant.name,
                assembly =
                    AssemblyOptionsImpl(
                        name = variant.name,
                    ),
                visualization =
                    VisualizationOptionsImpl(
                        name = variant.name,
                    ),
            )

    val assemblyFormat: StringFormat by lazy {
        when (variantAwareOptions.assembly.format) {
            FORMAT_JSON -> Convention.Json.Assembly
            FORMAT_YAML -> Convention.Yaml.Assembly
            else -> throw IllegalArgumentException("Only one of $STYLE_FLATTEN or $STYLE_STRUCTURED are allowed.")
        }
    }

    val assemblyStyle: Assembler.Style by lazy {
        when (variantAwareOptions.assembly.style) {
            STYLE_FLATTEN -> Assembler.Style.Flatten
            STYLE_STRUCTURED -> {
                if (variantAwareOptions.assembly.groupByScopes) {
                    Assembler.Style.StructuredWithScope
                } else {
                    Assembler.Style.StructuredWithoutScope
                }
            }
            else -> throw IllegalArgumentException("Only one of $STYLE_FLATTEN or $STYLE_STRUCTURED are allowed.")
        }
    }

    val assembledFileExt: String by lazy {
        when (variantAwareOptions.assembly.format) {
            FORMAT_JSON -> "json"
            FORMAT_YAML -> "yml"
            else -> error("nothing has come")
        }
    }

    val configurationNames: Set<String> by lazy {
        HashSet(variantAwareOptions.assembly.targetConfigurations)
    }

    val variantScope: ResolveScope.Variant = ResolveScope.Variant(variant.name)

    val additionalScopes: Set<ResolveScope.Addition> by lazy {
        variantAwareOptions.assembly.additionalScopes
            .map { ResolveScope.Addition(it) }
            .toSet()
    }

    val assembleOutputDir: File
        get() = variantAwareOptions.baseDir ?: project.projectDir
    val assembledArtifactsFile: File
        get() = File(assembleOutputDir, "artifact-definition.$assembledFileExt")
    val assembledLicenseCatalogFile: File
        get() = File(assembleOutputDir, "license-catalog.yml")

    val ignoreFile: File
        get() = File(assembleOutputDir, ".artifactignore")

    val ignoreFormat: ArtifactIgnoreParser.Format by lazy {
        when (extension.ignoreFormat) {
            IGNORE_FORMAT_REGEX -> ArtifactIgnoreParser.Format.Regex
            IGNORE_FORMAT_GLOB -> ArtifactIgnoreParser.Format.Glob
            else -> throw IllegalArgumentException("Only one of $IGNORE_FORMAT_REGEX or $IGNORE_FORMAT_GLOB are allowed.")
        }
    }
}
