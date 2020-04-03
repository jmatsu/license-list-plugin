@file:Suppress("RemoveRedundantQualifierName")

import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import shared.Version
import java.time.Instant

plugins {
    `kotlin-dsl`
    id("org.jmailen.kotlinter") version shared.Version.kotlinter

    maven
    id("com.jfrog.bintray") version shared.Version.bintray
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
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk7")
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
        name = shared.Definition.schemaName
        userOrg = "jmatsu"
        setLicenses("MIT")
        websiteUrl = "https://github.com/jmatsu/license-list-plugin"
        issueTrackerUrl = "https://github.com/jmatsu/license-list-plugin/issues"
        vcsUrl = "https://github.com/jmatsu/license-list-plugin/issues.git"
        githubRepo = "jmatsu/license-list-plugin"
        version(closureOf<VersionConfig> {
            name = project.version as String
            released = Instant.now().toString()
        })
    })

    setConfigurations("archives")
}

