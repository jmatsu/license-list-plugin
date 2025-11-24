package io.github.jmatsu.license

import io.github.jmatsu.license.dsl.IGNORE_FORMAT_REGEX
import io.github.jmatsu.license.dsl.IgnoreFormat
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

open class LicenseListExtension(
    private val variantAwareOptionsContainer: NamedDomainObjectContainer<VariantAwareOptions>,
) {
    @get:Nested
    val variants: NamedDomainObjectContainer<VariantAwareOptions>
        get() = variantAwareOptionsContainer

    @Suppress("unused")
    fun variants(action: Action<NamedDomainObjectContainer<VariantAwareOptions>>) {
        action.execute(variants)
    }

    /**
     * true means this plugin is enabled, otherwise this plugin is disabled.
     * true by default.
     */
    @get:Input
    var isEnabled: Boolean = true

    /**
     * A variant that default tasks will use for the dependency analysis and to get licenses.
     * the default value is release.
     */
    @get:Input
    var defaultVariant: String = "release"

    /**
     * A text format of each statement of .artifactignore
     * regex (regular expression) is by default.
     */
    @get:Input
    var ignoreFormat: IgnoreFormat = IGNORE_FORMAT_REGEX
}
