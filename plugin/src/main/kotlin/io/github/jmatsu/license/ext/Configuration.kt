package io.github.jmatsu.license.ext

import io.github.jmatsu.license.LicenseListPlugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.LenientConfiguration
import org.gradle.api.artifacts.UnknownConfigurationException

fun Configuration.lenientConfiguration(): LenientConfiguration? {
    if (!isCanBeResolved) {
        return null
    }
    // to avoid unexpected resolve
    return copyRecursive().run {
        // copied configuration is UNRESOLVED
        isCanBeResolved = true

        try {
            resolvedConfiguration.lenientConfiguration
        } catch (e: UnknownConfigurationException) {
            LicenseListPlugin.logger?.debug("failed to make a lenient configuration due to unknown configuration", e)
            null
        }
    }
}
