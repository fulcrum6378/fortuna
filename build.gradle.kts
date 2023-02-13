plugins {
    id("com.android.application") version("7.4.1") apply(false)
    id("org.jetbrains.kotlin.android") version("1.7.21") apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
