plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ir.mahdiparastesh.fortuna"
    compileSdk = 33
    buildToolsVersion = "34.0.0-rc3"

    defaultConfig {
        applicationId = "ir.mahdiparastesh.fortuna"
        minSdk = 26
        targetSdk = 33
        versionCode = 2
        versionName = "8.4.2"
    }

    setFlavorDimensions(listOf("calendar"))
    productFlavors {
        all { dimension = "calendar" }
        create("iranian")
        create("gregorian") { applicationIdSuffix = ".gregorian" }
    }

    sourceSets.getByName("main") {
        java.srcDirs("src/main/java")
        kotlin.srcDirs("src/main/kotlin")
    }
    sourceSets.getByName("iranian") {
        res.setSrcDirs(listOf("src/main/res", "src/main/res_iranian"))
    }
    sourceSets.getByName("gregorian") {
        res.setSrcDirs(listOf("src/main/res", "src/main/res_gregorian"))
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { viewBinding = true }
    packaging { resources.excludes.add("META-INF/DEPENDENCIES") }
}

@Suppress("SpellCheckingInspection")
dependencies {
    implementation("androidx.activity:activity-ktx:1.7.0")
    implementation("androidx.emoji2:emoji2:1.3.0")
    implementation("com.google.android.material:material:1.8.0")

    // DriveApi
    implementation("com.google.android.gms:play-services-auth:20.5.0")
    implementation("com.google.http-client:google-http-client-gson:1.42.0") // for GsonFactory
    implementation(
        "com.google.api-client:google-api-client-android:1.26.0"
    ) {
        exclude("org.apache.httpcomponents")
    } // for AndroidHttp and GoogleAccountCredential
    implementation(
        "com.google.apis:google-api-services-drive:v3-rev136-1.25.0"
    ) {
        exclude("org.apache.httpcomponents")
    } // for Drive and DriveScopes
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
}
