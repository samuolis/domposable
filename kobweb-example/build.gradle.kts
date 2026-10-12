import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kobweb.application)
}

group = "example.kobweb"

kobweb {
    pagesPackage.set("example.kobweb.pages")
    app {
        index { description.set("Domposable features inside a Kobweb application") }
        export { addExtraRoute("/contacts/alex") }
    }
}

kotlin {
    configAsKobwebApplication("domposable-kobweb")
    sourceSets.jsMain.dependencies {
        implementation(project(":domposable-design-material"))
        implementation(libs.compose.html)
        implementation(libs.kobweb.core)
    }
}
