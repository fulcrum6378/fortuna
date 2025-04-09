plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin { jvmToolchain(23) }

sourceSets.getByName("main") {
    kotlin.srcDirs("kotlin")
}

dependencies {
    api(files("libs/iranian-chronology-1.0.jar"))
}
