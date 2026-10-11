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
        namespace = "io.github.samuolis.domposable.lifecycle.viewmodel"
        compileSdk { version = release(37) { minorApiLevel = 2 } }
        minSdk = 23
        withHostTest {}
    }
    jvm()
    js { browser();  }
    wasmJs { browser(); }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        val commonMain = getByName("commonMain") {
            dependencies {
                api(libs.compose.runtime)
                api(project(":domposable-screens"))
                api(libs.lifecycle.viewmodel)
                api(libs.lifecycle.runtime)
                api(libs.savedstate)
                api(libs.lifecycle.viewmodel.compose)
                api(libs.lifecycle.runtime.compose)
                api("org.jetbrains.androidx.savedstate:savedstate-compose:1.4.0")
            }
        }
        val commonTest = getByName("commonTest") { dependencies { implementation(kotlin("test")) } }
        jvmTest { dependencies { implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0") } }
        val iosMain = create("iosMain") { dependsOn(commonMain) }
        getByName("iosArm64Main").dependsOn(iosMain)
        getByName("iosSimulatorArm64Main").dependsOn(iosMain)
    }
}

publishing { repositories { maven { name = "Fixture"; url = uri(rootProject.layout.buildDirectory.dir("repository")) } } }
