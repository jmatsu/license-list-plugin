import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import shared.Definition
import java.time.Instant
import shared.Version

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    maven
    id("com.jfrog.bintray") version shared.Version.bintray
    kotlin("plugin.serialization") version shared.Version.kotlin
}

repositories {
    google()
    jcenter()
}

group = Definition.group
version = "0.0.1"

configurations.configureEach {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(Version.kotlin)
            }
        }
    }
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Version.kotlinSerialization}")
    implementation("com.charleskorn.kaml:kaml:${Version.kaml}")

    compileOnly("com.android.tools.build:gradle:${Version.agp}")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

gradlePlugin {
    val `special-thanks` by plugins.creating {
        id = Definition.pluginId
        implementationClass = "io.github.jmatsu.spthanks.SpecialThanksPlugin"
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations.getByName("functionalTestImplementation").extendsFrom(configurations.getByName("testImplementation"))

val functionalTest by tasks.creating(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

val check by tasks.getting(Task::class) {
    dependsOn(functionalTest)
}

tasks.withType(Test::class) {
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.getByName("clean") {
    delete(project.buildDir, project.file("buildSrc/build"))
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")
    pkg(closureOf<PackageConfig> {
        repo = "maven"
        name = "special-thanks"
        userOrg = "jmatsu"
        setLicenses("Apache-2.0") // FIXME use proper licenses based on dependencies that this plugin uses
        websiteUrl = "https://github.com/jmatsu/special-thanks-plugin"
        issueTrackerUrl = "https://github.com/jmatsu/special-thanks-plugin/issues"
        vcsUrl = "https://github.com/jmatsu/special-thanks-plugin/issues.git"
        githubRepo = "jmatsu/special-thanks-plugin"
        version(closureOf<VersionConfig> {
            name = project.version as String
            released = Instant.now().toString()
        })
    })

    setConfigurations("archives")
}

