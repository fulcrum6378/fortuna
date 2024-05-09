plugins {
    id("com.android.application") version ("8.4.0") apply (false)
    id("org.jetbrains.kotlin.android") version ("1.9.23") apply (false)
}

tasks.register("clean", Delete::class) {
    delete("$rootDir/build", "$rootDir/app/build")
}
