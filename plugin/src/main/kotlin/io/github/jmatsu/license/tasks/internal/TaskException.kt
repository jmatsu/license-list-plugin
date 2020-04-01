package io.github.jmatsu.license.tasks.internal

import io.github.jmatsu.license.BaseException

abstract class TaskException(
    message: String
) : BaseException(message)
