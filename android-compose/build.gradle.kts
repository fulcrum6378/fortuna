import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "ir.mahdiparastesh.fortuna"
    compileSdk = 36
    buildToolsVersion = System.getenv("ANDROID_BUILD_TOOLS_VERSION")

    defaultConfig {
        applicationId = "ir.mahdiparastesh.fortuna.compose"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = project.extra["fortuna.compose.version"]!!

        val dropboxKey = System.getenv("FORTUNA_DROPBOX_KEY")
            ?: logger.warn("Dropbox app key was not found!")
        buildConfigField("String", "DROPBOX_APP_KEY", "\"${dropboxKey}\"")
        manifestPlaceholders.put("dropboxKey", dropboxKey)

        resValue("string", "app_name", "Fortuna (compose)")
    }

    setFlavorDimensions(listOf("calendar"))
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

    sourceSets.getByName("main") {
        manifest.srcFile("$rootDir/android-shared/AndroidManifest.xml")
        kotlin.srcDirs(
            "src/kotlin",
            "$rootDir/android-shared/kotlin",
            "$rootDir/compose-shared/kotlin",
        )
    }
    sourceSets.getByName("iranian") {
        res.srcDirs(
            "src/res", "$rootDir/android-shared/res", "$rootDir/android-shared/res_iranian"
        )
    }
    sourceSets.getByName("gregorian") {
        res.srcDirs(
            "src/res", "$rootDir/android-shared/res", "$rootDir/android-shared/res_gregorian"
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }
    kotlin {
        target {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_24)
            }
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
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
            isDebuggable = true
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "$rootDir/android-shared/proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("main")
        }
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.activity.compose)
    implementation(libs.activity.ktx)
    implementation(libs.compose.material3)
    implementation(libs.dropbox.android)
    implementation(libs.dropbox.core)
    implementation(libs.core.ktx)
    implementation(libs.coroutines.android)
}
