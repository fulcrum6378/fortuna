plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.javafx) apply false
}

tasks.register("clean", Delete::class) {
    delete("$rootDir/build", "$rootDir/app/build", "$rootDir/javafx/build")
}
