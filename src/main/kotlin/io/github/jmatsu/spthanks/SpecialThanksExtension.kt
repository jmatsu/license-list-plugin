package io.github.jmatsu.spthanks

import io.github.jmatsu.spthanks.dsl.AssembleStyle
import io.github.jmatsu.spthanks.dsl.Format
import io.github.jmatsu.spthanks.dsl.StructuredStyle
import io.github.jmatsu.spthanks.dsl.YamlFormat
import io.github.jmatsu.spthanks.internal.ArtifactManagement
import io.github.jmatsu.spthanks.model.ResolveScope
import org.gradle.api.tasks.*
import java.io.File

open class SpecialThanksExtension
@JvmOverloads constructor(
        @get:Internal val name: String = "default"
) {
    /**
     * true means this plugin is enabled, otherwise this plugin is disabled.
     * true by default.
     */
    @get:Input
    var isEnabled: Boolean = true

    /**
     * A file object of a license file.
     */
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    @Optional
    var licenseFile: File? = null

    /**
     * a format of the output.
     * Must be one of `yaml` or `json`.
     * the default format is *yaml*.
     *
     * @see Format
     */
    @get:Input
    var assembleFormat: Format = YamlFormat

    /**
     * a style to assemble the output.
     * Must be one of `structured` or `flatten`.
     * the default style is *structured*.
     *
     * @see AssembleStyle
     */
    @get:Input
    var assembleStyle: AssembleStyle = StructuredStyle

    /**
     * true means this plugin append scopes to the assembled output, otherwise no scope will be visible.
     * this option will be ignored if assembleStyle is flatten.
     * true by default.
     */
    @get:Input
    var withScope: Boolean = true

    /**
     * A set of variants that default tasks will use for the dependency analysis and to get licenses.
     * the default value is empty.
     */
    @get:Input
    var targetVariants: Set<String> = setOf()

    /**
     * A set of additional scopes to analyze dependencies and get licenses.
     * the default value contains test and androidTest.
     *
     * @see ResolveScope.Test
     * @see ResolveScope.AndroidTest
     * @sample additionalScopes += "functionalTest" is proper if you have defined functionalTest configuration
     */
    @get:Input
    var additionalScopes: MutableSet<String> = setOf(
            ResolveScope.Test,
            ResolveScope.AndroidTest
    ).map {
        it.name
    }.toMutableSet()

    /**
     * A set of configurations to be resolved.
     * the default value is what will be basically included to your application files or be used during development.
     *
     * @see ArtifactManagement.CommonConfigurationNames
     */
    @get:Input
    var targetConfigurations: MutableSet<String> = ArtifactManagement.CommonConfigurationNames.toMutableSet()

    /**
     * Group names to exclude.
     */
    @get:Input
    var excludeGroups: MutableSet<String> = hashSetOf()

    /**
     * Artifact names to exclude.
     * The format is "<group>:<name>". e.g. "io.github.jmatsu:example" is it
     */
    @get:Input
    var excludeArtifacts: MutableSet<String> = hashSetOf()
}