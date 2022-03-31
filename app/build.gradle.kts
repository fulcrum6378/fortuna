plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "ir.mahdiparastesh.fortuna"
        minSdk = 28
        targetSdk = 31
        versionCode = 1
        versionName = "0.9"
    }

    android.sourceSets.all { kotlin.srcDir("src/main/kotlin") }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
    buildFeatures { viewBinding = true }
}

dependencies {
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("androidx.core:core-ktx:1.7.0")
}
