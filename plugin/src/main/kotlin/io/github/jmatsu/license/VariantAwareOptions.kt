package io.github.jmatsu.license

import groovy.lang.Closure
import io.github.jmatsu.license.dsl.validation.optionalDirectoryProperty
import java.io.File
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.reflect.HasPublicType
import org.gradle.api.reflect.TypeOf
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.typeOf

interface VariantAwareOptions : Named {
    /**
     * A parent directory of an artifact definition file.
     */
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    @get:OutputDirectory
    @get:Optional
    var artifactDefinitionDirectory: File?

    @get:Nested
    val assembly: AssemblyOptions

    @get:Nested
    val visualization: VisualizationOptions

    fun assembly(action: Action<AssemblyOptions>)

    fun visualization(action: Action<VisualizationOptions>)
}

class VariantAwareOptionsImpl(
    private val name: String,
    override val assembly: AssemblyOptions,
    override val visualization: VisualizationOptions
) : VariantAwareOptions, HasPublicType {

    override var artifactDefinitionDirectory: File? by optionalDirectoryProperty()

    override fun assembly(action: Action<AssemblyOptions>) {
        action.execute(assembly)
    }

    override fun visualization(action: Action<VisualizationOptions>) {
        action.execute(visualization)
    }

    override fun getPublicType(): TypeOf<VariantAwareOptions> {
        return typeOf()
    }

    /**
     * HACK: For Groovy
     */
    fun assembly(action: Closure<AssemblyOptions>) {
        action.delegate = assembly
        action.resolveStrategy = Closure.DELEGATE_FIRST
        action.call()
    }

    /**
     * HACK: For Groovy
     */
    fun visualization(action: Closure<VisualizationOptions>) {
        action.delegate = visualization
        action.resolveStrategy = Closure.DELEGATE_FIRST
        action.call()
    }

    override fun getName(): String = name
}
