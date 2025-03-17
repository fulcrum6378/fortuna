plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.javafx)
    alias(libs.plugins.modularity)
    alias(libs.plugins.jlink)
}

kotlin { jvmToolchain(23) }

group = "ir.mahdiparastesh"
version = "0.5.0"

sourceSets.getByName("main") {
    java.srcDirs("src/main/java")
    kotlin.srcDirs("src/main/kotlin")
}

application {
    mainModule.set("ir.mahdiparastesh.fortuna")
    mainClass.set("ir.mahdiparastesh.fortuna.FortunaKt")
}

javafx {
    version = "23.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

jlink {
    launcher { name = "Fortuna" }
}

// some fucking painful lessons:
// - do NOT alter the /src/main/* structure.
// - do NOT remove the modularity plugin.
// - do NOT enable the configuration cache.
