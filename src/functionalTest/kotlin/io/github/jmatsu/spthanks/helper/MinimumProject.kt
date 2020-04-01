package io.github.jmatsu.spthanks.helper

import java.io.File

interface MinimumProject {
    val projectDir: File
    val buildFile: File
    val settingsFile: File

    fun applyPlugins(builder: StringBuilder.() -> Unit)
}

fun setupProject(): MinimumProject {
    val projectDir = File("build/functionalTest")
    projectDir.mkdirs()

    val buildFile = projectDir.resolve("build.gradle")
    val settingsFile = projectDir.resolve("settings.gradle")

    settingsFile.writeText("")

    return object : MinimumProject {
        override val projectDir: File
            get() = projectDir
        override val buildFile: File
            get() = buildFile
        override val settingsFile: File
            get() = settingsFile

        override fun applyPlugins(builder: StringBuilder.() -> Unit) {
            buildFile.appendText("""
                plugins {
                  ${buildString(builder)}
                }
            """.trimIndent())
        }
    }
}
