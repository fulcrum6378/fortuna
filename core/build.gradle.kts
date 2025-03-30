plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin { jvmToolchain(23) }

sourceSets.getByName("main") {
    java.srcDirs("java")
    kotlin.srcDirs("kotlin")
}

// The Java Module System strictly prohibits a single package from
// being split across multiple named modules. A package can belong to only one module.
// You can only have "ir.mahdiparastesh.fortuna" inside one module using "module-info.java"!
