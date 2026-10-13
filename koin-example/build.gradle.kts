plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.ksp)
}

compose.desktop { application { mainClass = "example.koin.MainKt" } }

tasks.withType<Test>().configureEach {
    systemProperty(
        "domposable.integrationCaptures",
        rootProject.layout.buildDirectory
            .dir("integration-captures")
            .get()
            .asFile,
    )
}

kotlin {
    jvmToolchain(21)
    android {
        namespace = "example.koin"
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
            if (providers.gradleProperty("domposable.consumer.repository").isPresent) {
                listOf("design-material", "lifecycle-viewmodel", "navigation3").forEach {
                    implementation("io.github.samuolis.domposable:domposable-$it:${project.version}")
                }
            } else {
                implementation(project(":domposable-design-material"))
                implementation(project(":domposable-lifecycle-viewmodel"))
                implementation(project(":domposable-navigation3"))
            }
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        commonTest.dependencies { implementation(kotlin("test")) }
        jvmMain.dependencies { implementation(compose.desktop.currentOs) }
        jvmTest.dependencies {
            implementation(libs.compose.ui.test)
            implementation(libs.coroutines.swing)
        }
    }
}

kotlin.sourceSets.commonMain { kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin") }
dependencies {
    if (providers.gradleProperty("domposable.consumer.repository").isPresent) {
        add("kspCommonMainMetadata", "io.github.samuolis.domposable:domposable-ksp:${project.version}")
    } else {
        add("kspCommonMainMetadata", project(":domposable-ksp"))
    }
}
tasks
    .matching {
        (it.name.startsWith("compile") || it.name.contains("sourcesJar", ignoreCase = true)) &&
            it.name != "kspCommonMainKotlinMetadata"
    }.configureEach { dependsOn("kspCommonMainKotlinMetadata") }
tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
    enabled = false
}
