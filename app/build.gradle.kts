plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "ir.mahdiparastesh.fortuna"
    compileSdk = 35
    buildToolsVersion = System.getenv("ANDROID_BUILD_TOOLS_VERSION")

    defaultConfig {
        applicationId = "ir.mahdiparastesh.fortuna"
        minSdk = 26
        targetSdk = 35
        versionCode = 13
        versionName = "11.8.0"
    }

    sourceSets.getByName("main") {
        manifest.srcFile("src/AndroidManifest.xml")
        kotlin.srcDirs("src/kotlin")
    }
    setFlavorDimensions(listOf("calendar"))
    productFlavors {
        all { dimension = "calendar" }
        create("iranian") { isDefault = true }
        create("gregorian") { applicationIdSuffix = ".gregorian" }
    }
    sourceSets.getByName("iranian") { res.setSrcDirs(listOf("src/res", "src/res_iranian")) }
    sourceSets.getByName("gregorian") { res.setSrcDirs(listOf("src/res", "src/res_gregorian")) }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_22
        targetCompatibility = JavaVersion.VERSION_22
    }
    kotlinOptions { jvmTarget = "22" }

    buildFeatures { buildConfig = true; viewBinding = true }
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("JKS_PATH"))
            storePassword = System.getenv("JKS_PASS")
            keyAlias = "fortuna"
            keyPassword = System.getenv("JKS_PASS")
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
    implementation(libs.activity.ktx)
    implementation(libs.core.ktx)
    implementation(libs.material)
}
