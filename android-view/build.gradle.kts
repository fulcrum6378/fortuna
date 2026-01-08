import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
        minSdk = 30
        targetSdk = 36
        versionCode = 15
        versionName = "15.0.0"

        val dropboxKey = System.getenv("FORTUNA_DROPBOX_KEY")
            ?: logger.warn("Dropbox app key was not found!")
        buildConfigField("String", "DROPBOX_APP_KEY", "\"${dropboxKey}\"")
        manifestPlaceholders["dropboxKey"] = dropboxKey
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
        kotlin.srcDirs("src/kotlin", "$rootDir/android-shared/kotlin")
        assets.srcDir("src/assets")
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
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
    kotlin {
        target {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_25)
                freeCompilerArgs.add("-Xannotation-default-target=param-property")
            }
        }
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
                "$rootDir/android-shared/proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("main")
        }
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
