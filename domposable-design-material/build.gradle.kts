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
        browser()
    }
    wasmJs {
        browser()
    }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        val commonMain =
            getByName("commonMain") {
                dependencies {
                    api(libs.compose.runtime)
                    api(project(":domposable-ui"))
                }
            }
        val commonTest = getByName("commonTest") { dependencies { implementation(kotlin("test")) } }
        getByName("androidHostTest") {
            dependencies {
                implementation(libs.robolectric)
                implementation(libs.junit)
                implementation(libs.androidx.test.runner)
                implementation(libs.android.compose.ui.test)
                implementation(libs.android.compose.ui.test.manifest)
            }
        }
        val iosMain = create("iosMain") { dependsOn(commonMain) }
        getByName("iosArm64Main").dependsOn(iosMain)
        getByName("iosSimulatorArm64Main").dependsOn(iosMain)
        val composeUiMain =
            create("composeUiMain") {
                dependsOn(commonMain)
                dependencies {
                    api(libs.compose.ui)
                    api(libs.compose.foundation)
                    implementation(libs.material3)
                    implementation(libs.coil.compose)
                    implementation(libs.coil.network.ktor)
                }
            }
        getByName("androidMain").dependsOn(composeUiMain)
        wasmJsMain { dependencies { implementation(libs.ktor.client.js) } }
        androidMain { dependencies { implementation(libs.ktor.client.okhttp) } }
        jvmMain { dependencies { implementation(libs.ktor.client.java) } }
        iosMain { dependencies { implementation(libs.ktor.client.darwin) } }
        getByName("jvmMain").dependsOn(composeUiMain)
        getByName("wasmJsMain").dependsOn(composeUiMain)
        iosMain.dependsOn(composeUiMain)
        jsMain {
            dependencies {
                implementation(libs.compose.html)
                implementation(libs.kotlinx.browser)
                implementation(libs.compose.runtime.saveable)
            }
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

// Robolectric API 36 accesses FileDescriptor internals; these exports apply only to host tests.
tasks.withType<Test>().matching { it.name == "testAndroidHostTest" }.configureEach {
    jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED", "--add-opens=java.base/java.io=ALL-UNNAMED")
}
