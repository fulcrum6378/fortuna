plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ir.mahdiparastesh.fortuna"
    compileSdk = 33
    buildToolsVersion = "34.0.0-rc2"

    defaultConfig {
        applicationId = "ir.mahdiparastesh.fortuna"
        minSdk = 26
        targetSdk = 33
        versionCode = 2
        versionName = "8.1.7"
    }

    setFlavorDimensions(listOf("calendar"))
    productFlavors {
        all { dimension = "calendar" }
        create("iranian")
        create("gregorian") { applicationIdSuffix = ".gregorian" }
    }

    sourceSets.getByName("main") {
        java.srcDirs("src/main/java")
        kotlin.srcDirs("src/main/kotlin")
    }
    sourceSets.getByName("iranian") {
        res.setSrcDirs(listOf("src/main/res", "src/main/res_iranian"))
    }
    sourceSets.getByName("gregorian") {
        res.setSrcDirs(listOf("src/main/res", "src/main/res_gregorian"))
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
    buildFeatures { viewBinding = true }
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.6.1")
    implementation("androidx.emoji2:emoji2:1.2.0")
    implementation("com.google.android.material:material:1.8.0")
}
