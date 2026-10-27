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
    android {
        namespace = "io.github.samuolis.domposable.design.material"
        compileSdk { version = release(37) { minorApiLevel = 2 } }
        minSdk = 24
        withHostTest { isIncludeAndroidResources = true }
    }
    jvm()
    js {
        browser {
            testTask { useKarma { useChromeHeadless() } }
        }
    }
    wasmJs {
        browser()
    }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies { api(project(":domposable-ui")) }
    }
}

publishing {
    repositories {
        maven {
            name = "Fixture"
            url = uri(rootProject.layout.buildDirectory.dir("repository"))
        }
    }
}
