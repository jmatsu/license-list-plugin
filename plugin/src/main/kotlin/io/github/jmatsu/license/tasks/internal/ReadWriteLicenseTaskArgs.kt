package io.github.jmatsu.license.tasks.internal

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.LicenseListExtension
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
    project: Project,
    extension: LicenseListExtension,
    variant: ApplicationVariant?
) {
    val assemblyFormat: StringFormat = when (extension.assembleFormat) {
        JsonFormat -> Convention.Json.Assembly
        YamlFormat -> Convention.Yaml.Assembly
        else -> throw IllegalArgumentException("Only one of $FlattenStyle or $StructuredStyle are allowed.")
    }

    val assemblyStyle: Assembler.Style = when (extension.assembleStyle) {
        FlattenStyle -> Assembler.Style.Flatten
        StructuredStyle -> {
            if (extension.groupByScopes) {
                Assembler.Style.StructuredWithScope
            } else {
                Assembler.Style.StructuredWithoutScope
            }
        }
        else -> throw IllegalArgumentException("Only one of $FlattenStyle or $StructuredStyle are allowed.")
    }

    val assembledFileExt: String = when (extension.assembleFormat) {
        JsonFormat -> "json"
        YamlFormat -> "yml"
        else -> error("nothing has come")
    }

    val configurationNames: Set<String> = HashSet(extension.targetConfigurations).apply {
        require(isNotEmpty()) {
            "targetConfigurations must has at least one element"
        }
    }

    val variantScopes: Set<ResolveScope.Variant> = variant?.let {
        val flavors = variant.productFlavors.map { it.name }
        val build = variant.buildType.name

        (flavors + listOf(build)).map { ResolveScope.Variant(it) }.toSet()
    }.orEmpty()

    val additionalScopes: Set<ResolveScope.Addition> = extension.additionalScopes.map { ResolveScope.Addition(it) }.toSet()

    val assembleOutputDir: File = extension.outputFile?.parentFile ?: project.projectDir
    val assembledArtifactsFile: File = File(assembleOutputDir, "license.$assembledFileExt")
    val assembledLicenseCatalogFile: File = File(assembleOutputDir, "license-catalog.yml")

    val excludeGroups: Set<String> = HashSet(extension.excludeGroups)
    val excludeArtifacts: Set<String> = HashSet(extension.excludeArtifacts)
}
