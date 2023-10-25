plugins {
    id("com.android.application") version ("8.1.2") apply (false)
    id("org.jetbrains.kotlin.android") version ("1.9.0") apply (false)
}

tasks.register("clean", Delete::class) {
    delete("$rootDir/build", "$rootDir/app/build")
}
