plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 32
    buildToolsVersion = "33.0.0"

    defaultConfig {
        applicationId = "ir.mahdiparastesh.fortuna"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "5.9.5"
    }

    setFlavorDimensions(listOf("calendar"))
    productFlavors {
        all { dimension = "calendar" }
        create("persian")
        create("gregorian") { applicationIdSuffix = ".gregorian" }
    }

    sourceSets.getByName("main") {
        java.srcDirs("src/main/java")
        kotlin.srcDirs("src/main/kotlin")
    }
    sourceSets.getByName("persian") {
        res.setSrcDirs(listOf("src/main/res", "src/main/res_persian"))
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
    implementation("androidx.activity:activity-ktx:1.5.1")
    implementation("androidx.emoji2:emoji2:1.2.0")
    implementation("com.google.android.material:material:1.6.1")
}
// Trying to exclude the unnecessary modules from material seems to be impossible!
