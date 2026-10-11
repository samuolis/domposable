@file:OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
}
kotlin { jvmToolchain(21); abiValidation() }
dependencies {
    implementation(libs.ksp.api)
    implementation("com.squareup:kotlinpoet-ksp:2.3.0")
    testImplementation(kotlin("test"))
}
publishing { repositories { maven { name = "Fixture"; url = uri(rootProject.layout.buildDirectory.dir("repository")) } } }
