pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        jcenter()
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }
}

rootProject.name = "android-example"

include("app")
