import org.gradle.api.artifacts.component.ModuleComponentIdentifier

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(21)
    android {
        namespace = "io.github.samuolis.domposable.consumer.fixture"
        compileSdk { version = release(37) { minorApiLevel = 2 } }
        minSdk = 24
        withHostTest {}
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
                }
            }
        val commonTest = getByName("commonTest") { dependencies { implementation(kotlin("test")) } }
        val iosMain = create("iosMain") { dependsOn(commonMain) }
        val composeUiMain = create("composeUiMain") { dependsOn(commonMain) }
        getByName("androidMain").dependsOn(composeUiMain)
        getByName("jvmMain").dependsOn(composeUiMain)
        getByName("wasmJsMain").dependsOn(composeUiMain)
        iosMain.dependsOn(composeUiMain)
        getByName("iosArm64Main").dependsOn(iosMain)
        getByName("iosSimulatorArm64Main").dependsOn(iosMain)
        commonMain {
            dependencies {
                if (providers.gradleProperty("domposable.consumer.repository").isPresent) {
                    listOf(
                        "foundation",
                        "ui",
                        "design-material",
                        "adaptive",
                        "screens",
                        "navigation3",
                        "lifecycle-viewmodel",
                    ).forEach {
                        implementation("io.github.samuolis.domposable:domposable-$it:${project.version}")
                    }
                } else {
                    implementation(project(":domposable-design-material"))
                    implementation(project(":domposable-navigation3"))
                    implementation(project(":domposable-adaptive"))
                    implementation(project(":domposable-lifecycle-viewmodel"))
                }
            }
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
        it.name.startsWith("compile") || it.name.contains("sourcesJar", ignoreCase = true)
    }.configureEach { dependsOn("kspCommonMainKotlinMetadata") }
tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
    enabled = false
}

// Consumer-defined renderers live in their own namespace, beside the published default provider.
ksp {
    arg("domposable.components.package", "example.consumer.components.generated")
    arg("domposable.components.provider", "ConsumerComponents")
}

tasks.register("verifyStableComposeBaseline") {
    group = "verification"
    description = "Rejects prerelease Compose core dependencies resolved by packaged consumers."
    notCompatibleWithConfigurationCache("Inspects resolved consumer dependencies")
    doLast {
        val coreGroups =
            setOf("animation", "foundation", "runtime", "ui")
                .flatMap { listOf("org.jetbrains.compose.$it", "androidx.compose.$it") }
                .toSet()
        kotlin.targets.matching { it.name != "metadata" }.forEach { target ->
            val compilation = target.compilations.getByName("main")
            val configuration = configurations.getByName(compilation.compileDependencyConfigurationName)
            val prereleases =
                configuration.incoming.resolutionResult.allComponents.mapNotNull { component ->
                    (component.id as? ModuleComponentIdentifier)?.takeIf {
                        it.group in coreGroups && '-' in it.version
                    }
                }
            check(prereleases.isEmpty()) {
                "${target.name} resolves prerelease Compose core dependencies: ${prereleases.joinToString()}"
            }
            logger.lifecycle("${target.name}: stable Compose core dependencies verified")
        }
    }
}
