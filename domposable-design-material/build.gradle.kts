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
                api(project(":domposable-ui"))
            }
        }
        val commonTest = getByName("commonTest") { dependencies { implementation(kotlin("test")) } }
        val iosMain = create("iosMain") { dependsOn(commonMain) }
        getByName("iosArm64Main").dependsOn(iosMain)
        getByName("iosSimulatorArm64Main").dependsOn(iosMain)
        val composeUiMain = create("composeUiMain") {
            dependsOn(commonMain)
            dependencies {
                api(libs.compose.ui)
                api(libs.compose.foundation)
                implementation(libs.material3)
                implementation("io.coil-kt.coil3:coil-compose:3.6.2")
                implementation("io.coil-kt.coil3:coil-network-ktor3:3.6.2")
            }
        }
        getByName("androidMain").dependsOn(composeUiMain)
        wasmJsMain { dependencies { implementation("io.ktor:ktor-client-js:3.5.2") } }
        androidMain { dependencies { implementation("io.ktor:ktor-client-okhttp:3.5.2") } }
        jvmMain { dependencies { implementation("io.ktor:ktor-client-java:3.5.2") } }
        iosMain { dependencies { implementation("io.ktor:ktor-client-darwin:3.5.2") } }
        getByName("jvmMain").dependsOn(composeUiMain)
        getByName("wasmJsMain").dependsOn(composeUiMain)
        iosMain.dependsOn(composeUiMain)
        jsMain { dependencies { implementation(libs.compose.html); implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0") } }
    }
}

publishing { repositories { maven { name = "Fixture"; url = uri(rootProject.layout.buildDirectory.dir("repository")) } } }
