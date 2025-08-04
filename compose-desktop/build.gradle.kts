import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

group = "ir.mahdiparastesh"
version = project.extra["fortuna.compose.version"]!!
val calendar = "iranian"

tasks.named<KotlinJvmCompile>("compileKotlin") {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_24)
    }
}

sourceSets.getByName("main") {
    kotlin.srcDirs("src/kotlin", "$rootDir/compose-shared/kotlin")
    resources.srcDir("src/resources")
}

dependencies {
    api(project(":core"))

    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
}


// arrange a temporary folder for compose resources
val composeResourcesDir = layout.buildDirectory.dir("composeResources")
val prepareComposeResources = tasks.register("prepareComposeResources") {
    val inputDir1 = file("$rootDir/android-shared/res")
    val inputDir2 = file("$rootDir/android-shared/res_$calendar")
    val outputDir = composeResourcesDir.get().asFile

    inputs.dir(inputDir1)
    inputs.dir(inputDir2)
    outputs.dir(outputDir)

    doLast {
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        // prepare string resources
        val valuesDir = File(outputDir, "values").apply { mkdirs() }
        val stringsXml = File(valuesDir, "strings.xml")
        var strings = StringBuilder(
            File(inputDir1, "values/strings.xml").readText()
        )
        strings.deleteRange(strings.length - 12, strings.length)
        strings.appendLine()
        strings.append(
            File(inputDir2, "values/strings.xml").readText().substring(51)
        )
        stringsXml.writeText(strings.toString())
        // TODO this algorithm doesn't assemble non-English XML files

        // prepare font resources
        copy {
            from(File(inputDir1, "font"))
            into(File(outputDir, "font").apply { mkdirs() })
        }
    }
}
tasks.named("convertXmlValueResourcesForMain") {
    dependsOn(prepareComposeResources)
}
tasks.named("copyNonXmlValueResourcesForMain") {
    dependsOn(prepareComposeResources)
}

compose {
    desktop {
        application {
            mainClass = "ir.mahdiparastesh.fortuna.MainKt"
        }
    }
    resources {
        packageOfResClass = "ir.mahdiparastesh.fortuna"
        nameOfResClass = "R"
        customDirectory("main", composeResourcesDir)
    }
}

tasks.jar {
    archiveBaseName = "Fortuna"
    /*from(configurations.runtimeClasspath.get().map(::zipTree))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE*/
}
