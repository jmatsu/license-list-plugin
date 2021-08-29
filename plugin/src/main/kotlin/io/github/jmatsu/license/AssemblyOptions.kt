package io.github.jmatsu.license

import io.github.jmatsu.license.dsl.AssembleFormat
import io.github.jmatsu.license.dsl.AssembleStyle
import io.github.jmatsu.license.dsl.StructuredStyle
import io.github.jmatsu.license.dsl.YamlFormat
import io.github.jmatsu.license.dsl.isAssembleFormat
import io.github.jmatsu.license.dsl.isAssembleStyle
import io.github.jmatsu.license.internal.ArtifactManagement
import io.github.jmatsu.license.model.ResolveScope
import org.gradle.api.Named
import org.gradle.api.reflect.HasPublicType
import org.gradle.api.reflect.TypeOf
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.kotlin.dsl.typeOf

interface AssemblyOptions : Named {

    /**
     * a format of the output.
     * Must be one of `yaml` or `json`.
     * the default format is *yaml*.
     *
     * @see AssembleFormat
     */
    @get:Input
    var format: AssembleFormat

    /**
     * a style to assemble the output.
     * Must be one of `structured` or `flatten`.
     * the default style is *structured*.
     *
     * @see AssembleStyle
     */
    @get:Input
    var style: AssembleStyle

    /**
     * true means this plugin will group the assembled output by scopes, otherwise no scope will be available in the output.
     * this option will be ignored if assembleStyle is flatten.
     * true by default.
     */
    @get:Input
    var groupByScopes: Boolean

    /**
     * A set of additional scopes to analyze dependencies and get licenses.
     * the default value contains test and androidTest.
     *
     * @see ResolveScope.Test
     * @see ResolveScope.AndroidTest
     * @sample additionalScopes += "functionalTest" is proper if you have defined functionalTest configuration
     */
    @get:Input
    var additionalScopes: Set<String>

    /**
     * A set of configurations to be resolved.
     * the default value is what will be basically included to your application files or be used during development.
     *
     * You can add your custom configurations to this property.
     * Let's say `targetConfigurations += "doggy"` has been passed to this extension,
     * then this plugin will resolve <flavors...>Doggy and doggy configurations and collect their dependencies.
     *
     * @see ArtifactManagement.CommonConfigurationNames
     */
    @get:Input
    var targetConfigurations: Set<String>

    @Input
    override fun getName(): String
}

class AssemblyOptionsImpl(private val name: String) : AssemblyOptions, HasPublicType {
    override fun getName(): String = name

    @Internal
    override fun getPublicType(): TypeOf<AssemblyOptions> {
        return typeOf()
    }

    override var format: AssembleFormat = YamlFormat
        set(value) {
            if (!isAssembleFormat(value)) {
                error("$value is not one of assemble formats")
            }

            field = value
        }

    override var style: AssembleStyle = StructuredStyle
        set(value) {
            if (!isAssembleStyle(value)) {
                error("$value is not one of assemble styles")
            }

            field = value
        }

    override var groupByScopes: Boolean = true

    override var additionalScopes: Set<String> = setOf(
        ResolveScope.Test,
        ResolveScope.AndroidTest
    ).map {
        it.name
    }.toSet()

    override var targetConfigurations: Set<String> = ArtifactManagement.CommonConfigurationNames
        set(value) {
            require(value.isNotEmpty()) {
                "targetConfigurations must has at least one element"
            }

            field = value
        }
}
