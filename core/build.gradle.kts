plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin { jvmToolchain(23) }

sourceSets.getByName("main") {
    //java.srcDirs("java")
    kotlin.srcDirs("kotlin")
}
