package io.github.jmatsu.spthanks.ext

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.LenientConfiguration

fun Configuration.lenientConfiguration(): LenientConfiguration {
    // to avoid unexpected resolve
    return copyRecursive().run {
        // copied configuration is UNRESOLVED
        isCanBeResolved = true

        resolvedConfiguration.lenientConfiguration
    }
}
