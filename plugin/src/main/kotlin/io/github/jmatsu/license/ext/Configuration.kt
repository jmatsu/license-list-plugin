package io.github.jmatsu.license.ext

import io.github.jmatsu.license.LicenseListPlugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.LenientConfiguration
import org.gradle.api.artifacts.UnknownConfigurationException

fun Configuration.lenientConfiguration(): LenientConfiguration? {
    // Gradle 9: Declarable-only configurations cannot use copyRecursive()
    // Use resolvable configurations (like RuntimeClasspath) directly
    if (!isCanBeResolved) {
        LicenseListPlugin.logger?.debug("Configuration $name is not resolvable, skipping")
        return null
    }

    return try {
        resolvedConfiguration.lenientConfiguration
    } catch (e: UnknownConfigurationException) {
        LicenseListPlugin.logger?.debug("failed to make a lenient configuration due to unknown configuration", e)
        null
    } catch (e: Exception) {
        // Gradle 9: Android projects may have variants like 'android-lint-local-aar' that cause
        // variant selection failures (NoMatchingConfigurationSelectionException).
        // This can happen when resolving project dependencies.
        if (e.javaClass.name.contains("NoMatchingConfigurationSelectionException") ||
            e.javaClass.name.contains("AmbiguousConfigurationSelectionException") ||
            e.message?.contains("variant") == true
        ) {
            LicenseListPlugin.logger?.debug("failed to make a lenient configuration due to variant selection failure", e)
            null
        } else {
            throw e
        }
    }
}
