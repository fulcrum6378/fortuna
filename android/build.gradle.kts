plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "ir.mahdiparastesh.fortuna"
    compileSdk = 37
    buildToolsVersion = System.getenv("ANDROID_BUILD_TOOLS_VERSION")

    defaultConfig {
        applicationId = "ir.mahdiparastesh.fortuna"
        minSdk = 30
        targetSdk = 37
        versionCode = 15
        versionName = "16.9.9"
    }

    flavorDimensions += "calendar"
    productFlavors {
        create("iranian") {
            dimension = "calendar"
            isDefault = true
        }
        create("gregorian") {
            dimension = "calendar"
            applicationIdSuffix = ".gregorian"
        }
    }

    sourceSets.named("main") {
        manifest.srcFile("src/AndroidManifest.xml")
        kotlin.directories += "src/kotlin"
        assets.directories += "../web"
    }
    sourceSets.named("iranian") {
        res.directories += "src/res"
        res.directories += "src/res_iranian"
    }
    sourceSets.named("gregorian") {
        res.directories += "src/res"
        res.directories += "src/res_gregorian"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    signingConfigs {
        create("main") {
            storeFile = file(System.getenv("JKS_PATH"))
            storePassword = System.getenv("JKS_PASS")
            keyAlias = "fortuna"
            keyPassword = System.getenv("JKS_PASS")
        }
    }
    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("main")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("main")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }
}

kotlin {
    compilerOptions {
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24
    }
}

dependencies {
    api(project(":core"))

    implementation(libs.activity.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.drawerlayout)
    implementation(libs.recyclerview)
    implementation(libs.dropbox.android)
    implementation(libs.dropbox.core)
    implementation(libs.material)
    implementation(libs.coroutines.android)
    implementation(libs.nanohttpd)
}
