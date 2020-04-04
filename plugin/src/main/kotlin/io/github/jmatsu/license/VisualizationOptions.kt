package io.github.jmatsu.license

import freemarker.template.Version
import io.github.jmatsu.license.dsl.HtmlFormat
import io.github.jmatsu.license.dsl.VisualizeFormat
import io.github.jmatsu.license.dsl.isVisualizeFormat
import io.github.jmatsu.license.dsl.validation.fileBasenameProperty
import io.github.jmatsu.license.dsl.validation.optionalDirectoryProperty
import java.io.File
import org.gradle.api.Named
import org.gradle.api.reflect.HasPublicType
import org.gradle.api.reflect.TypeOf
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.typeOf

interface VisualizationOptions : Named {

    /**
     * A style for how this plugin will visualize artifacts and licenses.
     *
     * @see VisualizeFormat
     */
    @get:Input
    var format: VisualizeFormat

    /**
     * A directory that contains custom `license.html.ftl`
     *
     * @see io.github.jmatsu.license.presentation.encoder.Html
     * @sample /resources/templates/license.html.ftl
     */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:Optional
    var htmlTemplateDir: File?

    /**
     * A version to be used visualizing html format
     */
    @get:Input
    @get:Optional
    var freeMakerVersion: String?

    /**
     * An output directory of the generated visualized file.
     */
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:OutputDirectory
    @get:Optional
    var outputDir: File?

    /**
     * A basename of a visualized licenses' file
     */
    @get:Input
    var fileBasename: String
}

class VisualizationOptionsImpl(
    private val name: String
) : VisualizationOptions, HasPublicType {
    override fun getPublicType(): TypeOf<VisualizationOptions> {
        return typeOf()
    }

    override fun getName(): String = name

    override var format: VisualizeFormat = HtmlFormat
        set(value) {
            if (!isVisualizeFormat(value)) {
                error("$value is not one of visualize formats")
            }

            field = value
        }

    override var htmlTemplateDir: File? by optionalDirectoryProperty()

    override var freeMakerVersion: String? = null
        set(value) {
            if (value != null) {
                Version(value)
            }
            field = value
        }

    override var outputDir: File? by optionalDirectoryProperty()

    override var fileBasename: String by fileBasenameProperty("license-list")
}
