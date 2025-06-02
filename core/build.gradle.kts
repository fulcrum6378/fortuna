import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
}

tasks.named<KotlinJvmCompile>("compileKotlin") {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_24)
    }
}

sourceSets.getByName("main") {
    kotlin.srcDirs("kotlin")
}
sourceSets.getByName("test") {
    kotlin.srcDirs("test")
}

dependencies {
    api(files("libs/iranian-chronology-1.1.jar"))
}
