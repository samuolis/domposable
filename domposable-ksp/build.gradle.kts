@file:OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
}
kotlin {
    jvmToolchain(21)
    abiValidation()
}
dependencies {
    api(libs.ksp.api)
    implementation(libs.kotlinpoet.ksp)
    testImplementation(kotlin("test"))
    testImplementation("com.google.devtools.ksp:symbol-processing-aa-embeddable:${libs.versions.ksp.get()}")
    testImplementation("com.google.devtools.ksp:symbol-processing-common-deps:${libs.versions.ksp.get()}")
}

tasks.test {
    maxHeapSize = "2g"
}
publishing {
    repositories {
        maven {
            name = "Fixture"
            url = uri(rootProject.layout.buildDirectory.dir("repository"))
        }
    }
}
