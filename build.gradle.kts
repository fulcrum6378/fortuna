plugins {
    id("com.android.application") version("8.0.2") apply(false)
    id("org.jetbrains.kotlin.android") version("1.8.20") apply(false)
}

task clean(type: Delete) {
    delete rootProject.buildDir
    delete "${rootDir}/app/build/"
}
