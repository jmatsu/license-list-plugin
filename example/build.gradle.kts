buildscript {
    repositories {
        google()
        jcenter()
        mavenLocal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.6.1")
        classpath(kotlin("gradle-plugin", version = "1.3.50")) // same to the version that kotlin-dsl uses
        classpath("io.github.jmatsu:license-list-gradle:+")
    }
}
