package io.github.jmatsu.license.tasks.internal

import io.github.jmatsu.license.LicenseListExtension
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Nested

abstract class BaseTask
@Inject constructor(
    @get:Nested val extension: LicenseListExtension
) : DefaultTask() {
    init {
        isEnabled = extension.isEnabled
    }
}
