plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    namespace = "ir.mahdiparastesh.fortuna"
    compileSdk = 34
    buildToolsVersion = "35.0.0-rc2"

    defaultConfig {
        applicationId = "ir.mahdiparastesh.fortuna"
        minSdk = 26
        targetSdk = 34
        versionCode = 12
        versionName = "11.4.0"
    }

    sourceSets.getByName("main") {
        manifest.srcFile("src/AndroidManifest.xml")
        kotlin.srcDirs("src/kotlin")
    }
    setFlavorDimensions(listOf("calendar"))
    productFlavors {
        all { dimension = "calendar" }
        create("iranian")
        create("gregorian") { applicationIdSuffix = ".gregorian" }
    }
    sourceSets.getByName("iranian") { res.setSrcDirs(listOf("src/res", "src/res_iranian")) }
    sourceSets.getByName("gregorian") { res.setSrcDirs(listOf("src/res", "src/res_gregorian")) }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21; targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions { jvmTarget = "21" }
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
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.google.android.material:material:1.11.0")
}
