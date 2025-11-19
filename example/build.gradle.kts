buildscript {
    repositories {
        google()
        mavenCentral()
        if (System.getenv("CI") == "true") {
            mavenLocal()
        } else {
            maven {
                url = uri("https://plugins.gradle.org/m2/")
            }
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.12.3")
        classpath(kotlin("gradle-plugin", version = "2.2.21"))
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.3.0")
        classpath("io.github.jmatsu:license-list-gradle:${rootProject.file("../VERSION").readText().trim()}")
        // classpath("com.cookpad.android.licensetools:license-tools-plugin:1.7.0")
    }
}
