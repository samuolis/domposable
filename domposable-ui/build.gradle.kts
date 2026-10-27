@file:OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)

plugins {
    alias(libs.plugins.ksp)
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
        namespace = "io.github.samuolis.domposable.ui"
        compileSdk { version = release(37) { minorApiLevel = 2 } }
        minSdk = 24
        withHostTest { isIncludeAndroidResources = true }
    }
    jvm()
    js {
        browser { testTask { useKarma { useChromeHeadless() } } }
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
                    implementation(libs.coroutines.core)
                    api(project(":domposable-foundation"))
                    implementation(libs.graphics.shapes)
                    implementation(libs.androidx.collection)
                }
            }
        val commonTest =
            getByName("commonTest") {
                dependencies {
                    implementation(kotlin("test"))
                    implementation(libs.coroutines.test)
                }
            }
        getByName("jsTest") {
            dependencies {
                implementation(project(":domposable-adaptive"))
                implementation(libs.coroutines.test)
            }
        }
        getByName("androidHostTest") {
            dependencies {
                implementation(project(":domposable-adaptive"))
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
        getByName("jvmMain").dependsOn(composeUiMain)
        getByName("wasmJsMain").dependsOn(composeUiMain)
        iosMain.dependsOn(composeUiMain)
        jsMain {
            dependencies {
                api(libs.compose.html)
                api(libs.kotlinx.browser)
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

kotlin.sourceSets.commonMain { kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin") }
dependencies { add("kspCommonMainMetadata", project(":domposable-ksp")) }
ksp { arg("domposable.internal.components", "true") }
tasks
    .matching {
        (it.name.startsWith("compile") || it.name.contains("sourcesJar", ignoreCase = true)) &&
            it.name != "kspCommonMainKotlinMetadata"
    }.configureEach {
        dependsOn("kspCommonMainKotlinMetadata")
    }

tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
    enabled = false
}

kotlin.sourceSets {
    androidMain { dependencies { implementation(libs.ktor.client.okhttp) } }
    getByName("jvmMain") { dependencies { implementation(libs.ktor.client.java) } }
    getByName("iosMain") { dependencies { implementation(libs.ktor.client.darwin) } }
    getByName("wasmJsMain") {
        dependencies {
            implementation(libs.ktor.client.js)
            implementation(libs.kotlinx.browser)
        }
    }
}

// Robolectric API 36 accesses FileDescriptor internals only in host tests.
tasks.withType<Test>().matching { it.name == "testAndroidHostTest" }.configureEach {
    jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED", "--add-opens=java.base/java.io=ALL-UNNAMED")
}
