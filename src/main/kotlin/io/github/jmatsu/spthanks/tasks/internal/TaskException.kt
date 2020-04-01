package io.github.jmatsu.spthanks.tasks.internal

import io.github.jmatsu.spthanks.BaseException

abstract class TaskException(
    message: String
) : BaseException(message)
