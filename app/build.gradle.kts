plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "ir.mahdiparastesh.fortuna"
        minSdk = 26
        targetSdk = 31
        versionCode = 1
        versionName = "2.1"
    }
    sourceSets {
        getByName("main").java.srcDirs("src/main/java")
        getByName("main").kotlin.srcDirs("src/main/kotlin")
    }

    setFlavorDimensions(listOf("calendar"))
    productFlavors {
        all { dimension = "calendar" }
        create("persian") {
            // applicationIdSuffix = ".persian"
        }
        create("gregorian") {
            applicationIdSuffix = ".gregorian"
        }
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
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("com.google.code.gson:gson:2.9.0")
}
