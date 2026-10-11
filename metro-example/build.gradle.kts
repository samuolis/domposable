plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.metro)
}
kotlin {
    jvmToolchain(21)
    android {
        namespace = "example.metro"
        compileSdk { version = release(37) { minorApiLevel = 2 } }
        minSdk = 24
        withHostTest {}
    }
    jvm()
    js { browser() }
    wasmJs { browser() }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            implementation(project(":domposable-ui"))
            implementation(project(":domposable-screens"))
        }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
