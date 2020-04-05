@file:Suppress("RemoveRedundantQualifierName")

import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import org.jetbrains.kotlin.cli.common.toBooleanLenient
import shared.Version
import java.time.Instant

plugins {
    id("org.gradle.kotlin.kotlin-dsl")
    id("org.jmailen.kotlinter")

    maven
    `maven-publish`
    id("com.jfrog.bintray")
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
    key = System.getenv("BINTRAY_API_KEY")
    pkg(closureOf<PackageConfig> {
        repo = "maven"
        name = shared.Definition.schemaName
        userOrg = "jmatsu"
        setLicenses("MIT")
        websiteUrl = "https://github.com/jmatsu/license-list-plugin"
        issueTrackerUrl = "https://github.com/jmatsu/license-list-plugin/issues"
        vcsUrl = "https://github.com/jmatsu/license-list-plugin.git"
        githubRepo = "jmatsu/license-list-plugin"
        githubReleaseNotesFile = "CHANGELOG.md"
        version(closureOf<VersionConfig> {
            name = project.version as String
            released = Instant.now().toString()
        })
    })

    dryRun = properties["dryRun"]?.toString()?.toBoolean() ?: true

    setPublications("schemaPublish")
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create("schemaPublish", MavenPublication::class) {
            from(components.getByName("java"))
            groupId = shared.Definition.group
            artifactId = shared.Definition.schemaName
            version = project.version as String

            pom {
                name.set(shared.Definition.schemaDisplayName)
                description.set(shared.Definition.schemaDescription)
                url.set(shared.Definition.webUrl)

                licenses {
                    license {
                        name.set("The MIT License (MIT)")
                        url.set("https://github.com/jmatsu/license-list-plugin/tree/master/license-files/mit.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("jmatsu")
                        name.set("Jumpei Matsuda")
                    }
                }

                scm {
                    connection.set("scm:git@github.com:jmatsu/license-list-plugin.git")
                    developerConnection.set("scm:git@github.com:jmatsu/license-list-plugin.git")
                    url.set("https://github.com/jmatsu/license-list-plugin")
                }
            }
        }
    }
}

