import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.javafx)
    application
}

tasks.named<KotlinJvmCompile>("compileKotlin") {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_24)
    }
}

group = "ir.mahdiparastesh"
version = "1.0.8"

sourceSets.getByName("main") {
    kotlin.srcDirs("src/kotlin")
    resources.srcDirs("src/resources")
}

application {
    mainClass.set("ir.mahdiparastesh.fortuna.FortunaKt")
}

javafx {
    version = "24.0.1"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation(project(":core"))
}

tasks.jar {
    archiveBaseName = "Fortuna"
    manifest {
        attributes["Main-Class"] = "ir.mahdiparastesh.fortuna.FortunaKt"
        attributes["Manifest-Version"] = version
    }
    from(configurations.runtimeClasspath.get().map(::zipTree))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// adding "--module-path" and "--add-modules" as JVM arguments will make no difference in the output,
// but it will silence a nasty warning.
