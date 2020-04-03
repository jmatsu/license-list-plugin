pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        jcenter()
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }
}

rootProject.name = "license-list-gradle"

include("license-list-gradle")
project(":license-list-gradle").projectDir = file("plugin")

include("license-list-schema")
project(":license-list-schema").projectDir = file("schema")
