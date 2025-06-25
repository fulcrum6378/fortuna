import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

tasks.named<KotlinJvmCompile>("compileKotlin") {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_24)
    }
}

sourceSets.getByName("main") {
    kotlin.srcDirs("src/kotlin")
    resources {
        srcDirs("$rootDir/android-shared/res")  // "$rootDir/android-shared/res_iranian"
        include("font/**", "raw/**", "values/strings.xml")
    }
}

dependencies {
    implementation(project(":core"))

    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.components.resources)
}

compose {
    desktop {
        application {
            mainClass = "ir.mahdiparastesh.fortuna.MainPageKt"
        }
    }
    resources {
        packageOfResClass = "ir.mahdiparastesh.fortuna"
        nameOfResClass = "R"
        customDirectory(
            sourceSetName = "main",
            directoryProvider = provider {
                layout.projectDirectory.dir("../android-shared/res")
            }
        )
    }
}
