plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.metro)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(21)
    android {
        namespace = "example.shop.checkout.impl"
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
            implementation(project(":checkout-api"))
            implementation(project(":domposable-lifecycle-viewmodel"))
            implementation(libs.metrox.viewmodel.compose)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.coroutines.test)
        }
    }
}

kotlin.sourceSets.commonMain { kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin") }
dependencies { add("kspCommonMainMetadata", project(":domposable-ksp")) }
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
