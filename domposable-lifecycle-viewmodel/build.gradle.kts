@file:OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
}

kotlin {
    abiValidation()
    jvmToolchain(21)
    androidLibrary {
        namespace = "io.github.samuolis.domposable.lifecycle.viewmodel"
        compileSdk { version = release(37) { minorApiLevel = 2 } }
        minSdk = 23
        withHostTest {}
    }
    jvm()
    js(IR) { browser();  }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(project(":domposable-screens"))
                api(libs.lifecycle.viewmodel)
                api(libs.lifecycle.runtime)
                api(libs.savedstate)
                api(libs.lifecycle.viewmodel.compose)
                api(libs.lifecycle.runtime.compose)
                api("org.jetbrains.androidx.savedstate:savedstate-compose:1.4.0")
            }
        }
        val commonTest by getting { dependencies { implementation(kotlin("test")) } }
        val iosMain by creating { dependsOn(commonMain) }
        getByName("iosArm64Main").dependsOn(iosMain)
        getByName("iosSimulatorArm64Main").dependsOn(iosMain)
    }
}

publishing { repositories { maven { name = "Fixture"; url = uri(rootProject.layout.buildDirectory.dir("repository")) } } }
