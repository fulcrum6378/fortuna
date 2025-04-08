plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "ir.mahdiparastesh.fortuna"
    compileSdk = 36
    buildToolsVersion = System.getenv("ANDROID_BUILD_TOOLS_VERSION")

    defaultConfig {
        applicationId = "ir.mahdiparastesh.fortuna"
        minSdk = 26
        targetSdk = 36
        versionCode = 15
        versionName = "13.0.2"

        val dropboxKey = System.getenv("FORTUNA_DROPBOX_KEY")
            ?: logger.warn("Dropbox app key was not found!")
        buildConfigField("String", "DROPBOX_APP_KEY", "\"${dropboxKey}\"")
        manifestPlaceholders.put("dropboxKey", dropboxKey)
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
        sourceCompatibility = JavaVersion.VERSION_23
        targetCompatibility = JavaVersion.VERSION_23
    }
    kotlinOptions { jvmTarget = "23" }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
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
    implementation(project(":core"))

    implementation(libs.activity.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.drawerlayout)
    implementation(libs.recyclerview)
    implementation(libs.dropbox.android)
    implementation(libs.dropbox.core)
    implementation(libs.material)
    implementation(libs.coroutines.android)
}
