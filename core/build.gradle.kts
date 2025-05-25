plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin { jvmToolchain(23) }

sourceSets.getByName("main") {
    kotlin.srcDirs("kotlin")
}
sourceSets.getByName("test") {
    kotlin.srcDirs("test")
}

dependencies {
    api(files("libs/iranian-chronology-1.1.jar"))
}
