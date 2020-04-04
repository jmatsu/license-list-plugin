package io.github.jmatsu.license.tasks.internal

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.LicenseListExtension
import org.gradle.api.tasks.Internal

abstract class VariantAwareTask(
    extension: LicenseListExtension,
    @get:Internal val variant: ApplicationVariant
) : BaseTask(extension)
