package io.github.jmatsu.spthanks.tasks.internal

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.spthanks.SpecialThanksExtension
import org.gradle.api.tasks.Input

abstract class VariantAwareTask(
    extension: SpecialThanksExtension,
    @get:Input val variant: ApplicationVariant
) : BaseTask(extension)
