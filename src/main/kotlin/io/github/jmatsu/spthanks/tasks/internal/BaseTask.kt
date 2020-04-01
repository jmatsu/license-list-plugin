package io.github.jmatsu.spthanks.tasks.internal

import io.github.jmatsu.spthanks.SpecialThanksExtension
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Nested

abstract class BaseTask
@Inject constructor(
    @get:Nested val extension: SpecialThanksExtension
) : DefaultTask() {
    init {
        isEnabled = extension.isEnabled
    }
}
