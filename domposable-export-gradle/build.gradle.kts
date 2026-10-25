@file:OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

kotlin {
    jvmToolchain(21)
    abiValidation()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(localGroovy())
}

tasks.processResources {
    from(rootProject.projectDir) {
        include("package.json", "package-lock.json", "tools/page-export/index.mjs", "tools/page-export/cli.mjs")
        into("domposable-export")
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
