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
        namespace = "io.github.samuolis.domposable.rive"
        compileSdk { version = release(37) { minorApiLevel = 2 } }
        minSdk = 24
    }
    jvm()
    js { browser() }
    wasmJs { browser() }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            api(project(":domposable-ui"))
            api(libs.compose.runtime)
        }
        val composeUiMain = create("composeUiMain") {
            dependsOn(getByName("commonMain"))
            dependencies {
                implementation(libs.compose.ui)
                implementation(libs.compose.foundation)
            }
        }
        getByName("androidMain").dependsOn(composeUiMain)
        getByName("jvmMain").dependsOn(composeUiMain)
        getByName("wasmJsMain").dependsOn(composeUiMain)
        val iosMain = create("iosMain") { dependsOn(composeUiMain) }
        getByName("iosArm64Main").dependsOn(iosMain)
        getByName("iosSimulatorArm64Main").dependsOn(iosMain)
        getByName("androidMain").dependencies { implementation("app.rive:rive-android:11.16.0") }
        getByName("jsMain").dependencies {
            implementation(libs.compose.html)
            implementation(npm("@rive-app/canvas", "2.44.0"))
        }
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
