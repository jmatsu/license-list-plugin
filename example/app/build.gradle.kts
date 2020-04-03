plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("license-list")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(23)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
    }

    flavorDimensions("one", "two")

    productFlavors {
        create("yellow") {
            setDimension("one")
        }
        create("red") {
            setDimension("one")
        }
        create("white") {
            setDimension("two")
        }
        create("blue") {
            setDimension("two")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    viewBinding {
        isEnabled = false
    }
    dataBinding {
        isEnabled = true
    }
}

repositories {
    google()
    jcenter()
    mavenLocal()
}

dependencies {
    implementation("io.github.jmatsu:license-list-schema:+")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.71")
    implementation("androidx.core:core-ktx:1.3.0-alpha02")
    implementation("androidx.appcompat:appcompat:1.2.0-alpha03")

    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("com.google.android.material:material:1.2.0-alpha05")
    implementation("com.xwray:groupie:2.7.0")
    implementation("com.xwray:groupie-databinding:2.7.0")

    implementation("com.squareup.moshi:moshi:1.9.2")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.9.2")

    testImplementation("junit:junit:4.12")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2")
}

kapt {
    correctErrorTypes = true

    javacOptions {
        option("-Xmaxerrs", 500)
    }
}

licenseList {
    // <action>LicenseList will execute <action><targetVariant>LicenseList if specified
    targetVariant = "yellowBlueRelease"

    // flatten is enough to show licenses.
    // structured is probably useful for the management.
    assembleStyle = "structured"

    // it's useful for those who want to customize the appearance of the license viewer
    visualizeFormat = "json"

    // if you would like to add wearApp
    targetConfigurations += "wearApp"
}
