package io.github.jmatsu.spthanks.tasks.internal

import io.github.jmatsu.spthanks.SpecialThanksExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Nested

abstract class BaseTask(
    @get:Nested val extension: SpecialThanksExtension
) : DefaultTask()
