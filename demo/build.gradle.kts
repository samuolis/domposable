plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(21)
    androidLibrary {
        namespace = "io.github.samuolis.domposable.demo.shared"
        compileSdk { version = release(37) { minorApiLevel = 2 } }
        minSdk = 23
        withHostTest {}
    }
    jvm()
    js(IR) { browser(); binaries.executable(); }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(project(":domposable-design-material"))
                api(project(":domposable-adaptive"))
                api(project(":domposable-navigation3"))
                api(project(":domposable-lifecycle-viewmodel"))
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
            }
        }
        getByName("androidMain").dependsOn(composeUiMain)
        getByName("jvmMain").dependsOn(composeUiMain)
        iosMain.dependsOn(composeUiMain)
        jsMain { dependencies { implementation(compose.html.core); implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0") } }
        jvmMain { dependencies { implementation(compose.desktop.currentOs) } }
    }
}

kotlin.targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
    binaries.framework { baseName = "DomposableDemo"; isStatic = true }
}
compose.desktop { application { mainClass = "io.github.samuolis.domposable.demo.MainKt" } }

kotlin.sourceSets.commonMain { kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin") }
dependencies { add("kspCommonMainMetadata", project(":domposable-ksp")) }
tasks.matching { (it.name.startsWith("compile") || it.name.contains("sourcesJar", ignoreCase = true)) && it.name != "kspCommonMainKotlinMetadata" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}

tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
    enabled = false
}
