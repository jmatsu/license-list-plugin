plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("license-list")
}

android {
    compileSdkVersion(28)

    defaultConfig {
        minSdkVersion(23)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0"
    }

    flavorDimensions("one", "two")

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
    targetVariant = "yellowBlueRelease"
    assembleStyle = "structured"
}
