plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.javafx) apply false
}

tasks.register("clean", Delete::class) {
    delete(
        "$rootDir/.kotlin",
        "$rootDir/build",
        "$rootDir/app/build",
        "$rootDir/core/build",
        "$rootDir/javafx/build",
    )
}
