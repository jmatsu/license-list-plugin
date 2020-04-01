package io.github.jmatsu.spthanks.helper

import java.io.File

fun File.appendAndroidExtension() {
    appendText("""

        android {
          defaultConfig {
            applicationId "io.github.jmatsu.spthanks"
            versionName "1.0"
            versionCode 1
            compileSdkVersion 28
            targetSdkVersion 28
            minSdkVersion 28
          }
        }

    """.trimIndent())
}
