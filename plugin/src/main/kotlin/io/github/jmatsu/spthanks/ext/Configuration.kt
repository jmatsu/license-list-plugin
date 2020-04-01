package io.github.jmatsu.spthanks.ext

import io.github.jmatsu.spthanks.SpecialThanksPlugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.LenientConfiguration
import org.gradle.api.artifacts.UnknownConfigurationException

fun Configuration.lenientConfiguration(): LenientConfiguration? {
    // to avoid unexpected resolve
    return copyRecursive().run {
        // copied configuration is UNRESOLVED
        isCanBeResolved = true

        try {
            resolvedConfiguration.lenientConfiguration
        } catch (e: UnknownConfigurationException) {
            SpecialThanksPlugin.logger?.debug("failed to make a lenient configuration due to unknown configuration", e)
            null
        }
    }
}
