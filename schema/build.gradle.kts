@file:Suppress("RemoveRedundantQualifierName")

import shared.Version

plugins {
    `kotlin-dsl`
    id("org.jmailen.kotlinter") version shared.Version.kotlinter
}

repositories {
    google()
    jcenter()
}

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
    compileOnly(platform("org.jetbrains.kotlin:kotlin-bom"))
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk7")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("io.mockk:mockk:${Version.mockk}")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:${Version.junitPlatformLauncher}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

kotlinter {
    ignoreFailures = false
    reporters = arrayOf("checkstyle", "html")
    experimentalRules = false
    fileBatchSize = 30
}
