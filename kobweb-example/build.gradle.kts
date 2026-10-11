plugins { alias(libs.plugins.kotlin.multiplatform); alias(libs.plugins.compose.compiler) }
kotlin {
    js { browser() }
    sourceSets.jsMain.dependencies {
        implementation(project(":domposable-design-material"))
        implementation("com.varabyte.kobweb:kobweb-core:0.25.1")
    }
}
