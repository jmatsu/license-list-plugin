plugins {
    `kotlin-dsl` apply false
    kotlin("plugin.serialization") version shared.Version.kotlin apply false
    id("org.jmailen.kotlinter") version shared.Version.kotlinter apply false
}

subprojects {
    group = shared.Definition.group
    version = rootProject.file("VERSION").readText().trim()

    tasks.withType(Test::class) {
        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    repositories {
        google()
        mavenCentral()
    }
}

rootProject.tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir, rootProject.file("buildSrc/build"))
}
