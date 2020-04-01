pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        jcenter()
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }
}

rootProject.name = "license-list-gradle"

include("plugin")
include("schema")
