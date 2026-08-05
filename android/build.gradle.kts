import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
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
        versionName = "17.0.5"
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
    lint { checkReleaseBuilds = false }

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
