package io.github.jmatsu.spthanks.tasks.internal

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.spthanks.SpecialThanksExtension
import io.github.jmatsu.spthanks.dsl.FlattenStyle
import io.github.jmatsu.spthanks.dsl.JsonFormat
import io.github.jmatsu.spthanks.dsl.StructuredStyle
import io.github.jmatsu.spthanks.dsl.YamlFormat
import io.github.jmatsu.spthanks.model.ResolveScope
import io.github.jmatsu.spthanks.presentation.Assembler
import io.github.jmatsu.spthanks.presentation.Convention
import kotlinx.serialization.StringFormat
import org.gradle.api.Project
import java.io.File

abstract class ReadWriteLicenseTaskArgs(
        project: Project,
        extension: SpecialThanksExtension,
        variant: ApplicationVariant?
) {
    val format: StringFormat = when (extension.assembleFormat) {
        JsonFormat -> Convention.Json
        YamlFormat -> Convention.Yaml
        else -> throw IllegalArgumentException("Only one of $FlattenStyle or $StructuredStyle are allowed.")
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

    val ext: String = when (extension.assembleFormat) {
        JsonFormat -> "json"
        YamlFormat -> "yml"
        else -> error("nothing has come")
    }

    val configurationNames: Set<String> = HashSet(extension.targetConfigurations).apply {
        require(isNotEmpty()) {
            "targetConfigurations must has at least one element"
        }
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

    val artifactsFile: File = extension.outputFile ?: File(project.projectDir, "license.$ext")
    val outputDir: File = artifactsFile.parentFile
    val catalogFile: File = File(artifactsFile.parentFile, "license-catalog.yml")

    val excludeGroups: Set<String> = HashSet(extension.excludeGroups)
    val excludeArtifacts: Set<String> = HashSet(extension.excludeArtifacts)
}