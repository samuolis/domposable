plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
}
kotlin {
    js { browser() }
    sourceSets.jsMain.dependencies {
        implementation(project(":domposable-design-material"))
        implementation(libs.kobweb.core)
    }
}
