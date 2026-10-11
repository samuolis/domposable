plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(21)
    android {
        namespace = "example.shop.checkout.api"
        compileSdk { version = release(37) { minorApiLevel = 2 } }
        minSdk =
            24
        withHostTest {}
    }
    jvm()
    js { browser() }
    wasmJs { browser() }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            api(project(":catalog-api"))
            api(libs.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.coroutines.test)
        }
    }
}
