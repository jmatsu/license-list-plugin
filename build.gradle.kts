subprojects {
    group = shared.Definition.group
    version = "0.0.1"

    tasks.withType(Test::class) {
        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}

rootProject.tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir, rootProject.file("buildSrc/build"))
}
