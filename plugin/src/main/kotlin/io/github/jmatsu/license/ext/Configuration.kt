package io.github.jmatsu.license.ext

import io.github.jmatsu.license.LicenseListPlugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.LenientConfiguration
import org.gradle.api.artifacts.UnknownConfigurationException

fun Configuration.lenientConfiguration(): LenientConfiguration? {
    // Skip non-resolvable configurations (e.g., annotationProcessor, kapt which are declarable-only in Gradle 9+)
    if (!isCanBeResolved) {
        LicenseListPlugin.logger?.debug("Skipping non-resolvable configuration: $name")
        return null
    }

    return try {
        resolvedConfiguration.lenientConfiguration
    } catch (e: UnknownConfigurationException) {
        LicenseListPlugin.logger?.debug("failed to make a lenient configuration due to unknown configuration", e)
        null
    }
}
