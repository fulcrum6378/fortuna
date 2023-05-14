plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ir.mahdiparastesh.fortuna"
    compileSdk = 33
    buildToolsVersion = "34.0.0 rc4"

    defaultConfig {
        applicationId = "ir.mahdiparastesh.fortuna"
        minSdk = 26
        targetSdk = 33
        versionCode = 3
        versionName = "9.0.0"
    }
    sourceSets.getByName("main") { kotlin.srcDirs("src/main/kotlin") }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { viewBinding = true }

    setFlavorDimensions(listOf("calendar"))
    productFlavors {
        all { dimension = "calendar" }
        create("iranian")
        create("gregorian") { applicationIdSuffix = ".gregorian" }
    }
    sourceSets.getByName("iranian") {
        res.setSrcDirs(listOf("src/main/res", "src/main/res_iranian"))
    }
    sourceSets.getByName("gregorian") {
        res.setSrcDirs(listOf("src/main/res", "src/main/res_gregorian"))
    }

    signingConfigs {
        create("release") {
            storeFile = file("D:\\Experimental\\mahdiparastesh.jks")
            storePassword = System.getenv("JERK")
            keyAlias = "fortuna"
            keyPassword = System.getenv("JERK")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.7.1")
    implementation("androidx.emoji2:emoji2:1.3.0")
    implementation("com.google.android.material:material:1.9.0")
}
