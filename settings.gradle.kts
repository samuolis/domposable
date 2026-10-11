pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositories {
        providers.gradleProperty("domposable.consumer.repository").orNull?.let { repository ->
            maven { url = uri(repository); content { includeGroup("io.github.samuolis.domposable") } }
        }
        google(); mavenCentral()
    }
}
rootProject.name = "domposable"
include(
    ":domposable-foundation", ":domposable-ui", ":domposable-design-material",
    ":domposable-adaptive", ":domposable-screens", ":domposable-navigation3",
    ":domposable-lifecycle-viewmodel", ":domposable-ksp", ":demo", ":androidApp",
    ":metro-example", ":kobweb-example",
    ":consumer-fixture", ":custom-provider-fixture",
)
