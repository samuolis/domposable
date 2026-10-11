plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(21)
    android {
        namespace = "io.github.samuolis.domposable.custom.fixture"
        compileSdk { version = release(37) { minorApiLevel = 2 } }
        minSdk = 23
        withHostTest {}
    }
    jvm()
    js { browser();  }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        val commonMain = getByName("commonMain") {
            dependencies {
                api(libs.compose.runtime)
            }
        }
        val commonTest = getByName("commonTest") { dependencies { implementation(kotlin("test")) } }
        val iosMain = create("iosMain") { dependsOn(commonMain) }
        getByName("iosArm64Main").dependsOn(iosMain)
        getByName("iosSimulatorArm64Main").dependsOn(iosMain)
        commonMain { dependencies {
            if (providers.gradleProperty("domposable.consumer.repository").isPresent) implementation("io.github.samuolis.domposable:domposable-ui:${project.version}")
            else implementation(project(":domposable-ui"))
        } }
    }
}

