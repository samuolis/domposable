plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(21)
    android {
        namespace = "io.github.samuolis.domposable.demo.shared"
        compileSdk { version = release(37) { minorApiLevel = 2 } }
        minSdk = 24
        androidResources { enable = true }
        withHostTest { isIncludeAndroidResources = true }
    }
    jvm()
    js { browser(); binaries.executable(); }
    wasmJs { browser(); binaries.executable(); }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        val commonMain = getByName("commonMain") {
            dependencies {
                api(libs.compose.runtime)
                implementation(compose.components.resources)
                api(project(":domposable-design-material"))
                api(project(":domposable-adaptive"))
                api(project(":domposable-navigation3"))
                api(project(":domposable-lifecycle-viewmodel"))
            }
        }
        jvmTest { dependencies { implementation("org.jetbrains.compose.ui:ui-test-junit4:${libs.versions.compose.get()}"); implementation(compose.desktop.currentOs) } }
        val commonTest = getByName("commonTest") { dependencies { implementation(kotlin("test")) } }
        getByName("androidHostTest") { dependencies {
            implementation("org.robolectric:robolectric:4.17")
            implementation("junit:junit:4.13.2")
            implementation("androidx.compose.ui:ui-test-junit4:1.12.1")
            implementation("androidx.compose.ui:ui-test-manifest:1.12.1")
        } }
        val iosMain = create("iosMain") { dependsOn(commonMain) }
        getByName("iosArm64Main").dependsOn(iosMain)
        getByName("iosSimulatorArm64Main").dependsOn(iosMain)
        val composeUiMain = create("composeUiMain") {
            dependsOn(commonMain)
            dependencies {
                api(libs.compose.ui)
                api(libs.compose.foundation)
            }
        }
        getByName("androidMain").dependsOn(composeUiMain)
        getByName("jvmMain").dependsOn(composeUiMain)
        getByName("wasmJsMain").dependsOn(composeUiMain)
        iosMain.dependsOn(composeUiMain)
        jsMain { dependencies { implementation(libs.compose.html); implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0") } }
        wasmJsMain { dependencies { implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0") } }
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
tasks.withType<Test>().configureEach {
    systemProperty("domposable.screenshots", rootProject.layout.buildDirectory.dir("screenshots").get().asFile.absolutePath)
    jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED", "--add-opens=java.base/java.io=ALL-UNNAMED")
}
