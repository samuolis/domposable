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
        namespace = "io.github.samuolis.domposable.navigation3"
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
                api(project(":domposable-ui"))
                api(libs.navigation3.runtime)
            }
        }
        val commonTest by getting { dependencies { implementation(kotlin("test")) } }
        val iosMain by creating { dependsOn(commonMain) }
        getByName("iosArm64Main").dependsOn(iosMain)
        getByName("iosSimulatorArm64Main").dependsOn(iosMain)
        val composeUiMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(compose.ui)
                api(compose.foundation)
                implementation(libs.navigation3.ui)
            }
        }
        getByName("androidMain").dependsOn(composeUiMain)
        getByName("jvmMain").dependsOn(composeUiMain)
        iosMain.dependsOn(composeUiMain)
        jsMain { dependencies { implementation(compose.html.core); implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0"); implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0") } }
    }
}

publishing { repositories { maven { name = "Fixture"; url = uri(rootProject.layout.buildDirectory.dir("repository")) } } }
