@file:Suppress("RemoveRedundantQualifierName")

import shared.Version

plugins {
    id("org.gradle.kotlin.kotlin-dsl")
    id("org.jmailen.kotlinter")

    `maven-publish`
    id("signing")
}

repositories {
    google()
    mavenCentral()
}

ext {
    var isRelease = System.getenv("RELEASE_MODE") == "true"

    val versionFile = File(rootDir, "VERSION")
    if (versionFile.exists()) {
        val versionText = versionFile.readText().trim()

        set("versionText", versionText)
        set("releaseVersion", if (isRelease) versionText else "${versionText.split("-")[0]}-SNAPSHOT")
    } else {
        set("releaseVersion", "0.99.0")
        isRelease = false
    }

    set("isRelease", isRelease)
    set("repoUrl", if (isRelease) "https://oss.sonatype.org/service/local/staging/deploy/maven2/" else "https://oss.sonatype.org/content/repositories/snapshots/")
    set("repoUsername", findProperty("nexusUsername"))
    set("repoPassword", findProperty("nexusPassword"))
}

val isRelease: Boolean by project.ext
val releaseVersion: String by project.ext
val repoUrl: String by project.ext
val repoUsername: String? by project.ext
val repoPassword: String? by project.ext

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

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            setUrl(repoUrl)

            credentials(PasswordCredentials::class) {
                username = repoUsername
                password = repoPassword
            }
        }
    }

    publications {
        create("schema", MavenPublication::class) {
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

afterEvaluate {
    val isRelease: Boolean by project.ext

    signing {
        setRequired { isRelease && gradle.taskGraph.hasTask("publishSchemaPublicationToMavenRepository") || findProperty("signingRequired") == "true" }

        val signingKey: String? = findProperty("signingKey") as? String
        val signingPassword: String? = findProperty("signingPassword") as? String

        useInMemoryPgpKeys(signingKey, signingPassword)
        publishing.publications.configureEach {
            sign(this)
        }
    }
}

