package io.github.jmatsu.license.tasks.internal

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.LicenseListExtension
import org.gradle.api.tasks.Input

abstract class VariantAwareTask(
    extension: LicenseListExtension,
    @get:Input val variant: ApplicationVariant
) : BaseTask(extension)
