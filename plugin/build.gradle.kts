@file:Suppress("RemoveRedundantQualifierName")

import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import shared.Definition
import shared.Version
import java.time.Instant

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("plugin.serialization") version shared.Version.kotlin
    id("org.jmailen.kotlinter") version shared.Version.kotlinter

    // release stuff
    maven
    id("com.jfrog.bintray") version shared.Version.bintray
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
            when (requested.group) {
                "org.jetbrains.kotlin" -> useVersion(Version.kotlin)
                "org.junit.jupiter" -> useVersion(Version.junit5)
                "org.junit.vintage" -> useVersion(Version.junit5)
            }
        }
    }
}

dependencies {
    implementation(project(":schema"))

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Version.kotlinSerialization}")
    implementation("com.charleskorn.kaml:kaml:${Version.kaml}")

    compileOnly("com.android.tools.build:gradle:${Version.agp}")
    testImplementation("com.android.tools.build:gradle:${Version.agp}")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("io.mockk:mockk:${Version.mockk}")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")

    testImplementation(gradleTestKit())

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:${Version.junitPlatformLauncher}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

gradlePlugin {
    val `special-thanks` by plugins.creating {
        id = Definition.pluginId
        implementationClass = "io.github.jmatsu.spthanks.SpecialThanksPlugin"
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
    resources.srcDirs(File(project.buildDir, "functionalTest"))
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations.getByName("functionalTestImplementation").extendsFrom(configurations.getByName("testImplementation"))
configurations.getByName("functionalTestRuntimeOnly").extendsFrom(configurations.getByName("testRuntimeOnly"))

val functionalTest by tasks.creating(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = sourceSets.main.get().runtimeClasspath + functionalTestSourceSet.runtimeClasspath
}

val check by tasks.getting(Task::class) {
    dependsOn(functionalTest)
}

kotlinter {
    ignoreFailures = false
    reporters = arrayOf("checkstyle", "html")
    experimentalRules = false
    fileBatchSize = 30
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")
    pkg(closureOf<PackageConfig> {
        repo = "maven"
        name = Definition.name
        userOrg = "jmatsu"
        setLicenses("MIT")
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

