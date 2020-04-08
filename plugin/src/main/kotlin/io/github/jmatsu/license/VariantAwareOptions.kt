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
     * @see baseDir
     */
    @Deprecated("the name has been changed. This would be removed in 1.0.0", replaceWith = ReplaceWith("baseDir"))
    var artifactDefinitionDirectory: File?

    /**
     * @see baseDir
     */
    @Deprecated("this name was produced by a typo. This will be removed in 1.0.0.", replaceWith = ReplaceWith("baseDir"))
    var dataDir: File?

    /**
     * A parent directory of an artifact definition file.
     */
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    @get:OutputDirectory
    @get:Optional
    var baseDir: File?

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

    override var artifactDefinitionDirectory: File?
        set(value) { baseDir = value }
        get() = baseDir

    override var baseDir: File? by optionalDirectoryProperty()

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
