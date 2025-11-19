plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("io.github.jmatsu.license-list")
}

android {
    compileSdk = 36

    defaultConfig {
        namespace = "io.github.jmatsu.license.example"
        minSdk = 23
        targetSdk = 29
        versionCode = 1
        versionName = "1.0"
    }

    flavorDimensions += arrayOf("one", "two")

    productFlavors {
        create("yellow") {
            dimension = "one"
        }
        create("red") {
            dimension = "one"
        }
        create("white") {
            dimension = "two"
        }
        create("blue") {
            dimension = "two"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

repositories {
    if (System.getenv("CI") == "true") {
        mavenLocal()
    }
    google()
    mavenCentral()
}

val sampleConfiguration = configurations.create("sample")
configurations.getByName("implementation").extendsFrom(sampleConfiguration)

val orphanConfiguration = configurations.create("orphan")

dependencies {
    implementation(fileTree(project.file("lib")) {
        include("*.jar")
    })

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.21")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")

    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("com.google.android.material:material:1.13.0")

    implementation("com.squareup.moshi:moshi:1.15.2")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.2")

    // sampleConfiguration is included by `implementation` so this plugin will collect sampleConfiguration as well without any setup
    sampleConfiguration("io.github.jmatsu:license-list-schema:${rootProject.file("../VERSION").readText().trim()}")
    // orphanConfiguration is not included by any other configurations, so you need to add this to targetConfigurations. See licenseList block.
    orphanConfiguration("com.android.support.test:runner:1.0.2")

    // the license text contains line-return else...
    implementation("org.jvnet.staxex:stax-ex:1.7.8")

    testImplementation("junit:junit:4.12")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2")
}

licenseList {
    // <action>LicenseList will execute <action><targetVariant>LicenseList if specified
    defaultVariant = "yellowBlueRelease"

    variants {
        create("yellowBlueRelease") {
            baseDir = file("license-list")

            assembly {
                // flatten is enough to show licenses but structured could be probably useful for the management.
                style = "structured"

                // Group artifacts by scopes like `test`, `androidTest` for the management
                groupByScopes = true

                // if you would like to add wearApp
                targetConfigurations += "wearApp"
                // orphanConfiguration is not visible unless specified
//                targetConfigurations += "orphanConfiguration"
            }

            visualization {
                // it's useful for those who want to customize the appearance of the license viewer
                format = properties["visualizationFormat"] as? String ?: "html"
            }
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
