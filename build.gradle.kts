plugins {
    alias(libs.plugins.android.application) apply false
    //alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    //alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}

tasks.register("clean", Delete::class) {
    delete(
        "$rootDir/.kotlin",
        "$rootDir/build",
        //"$rootDir/android-compose/build",
        "$rootDir/android-view/build",
        //"$rootDir/compose-desktop/build",
        "$rootDir/core/build",
    )
}
