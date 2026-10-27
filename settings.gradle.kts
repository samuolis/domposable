pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        providers.gradleProperty("domposable.consumer.repository").orNull?.let { repository ->
            maven {
                url = uri(repository)
                content { includeGroup("io.github.samuolis.domposable") }
            }
        }
        google()
        mavenCentral()
    }
}
rootProject.name = "domposable"
include(
    ":domposable-foundation",
    ":domposable-ui",
    ":domposable-design-material",
    ":domposable-adaptive",
    ":domposable-screens",
    ":domposable-navigation3",
    ":domposable-lifecycle-viewmodel",
    ":domposable-ksp",
    ":domposable-export-gradle",
    ":demo",
    ":androidApp",
    ":metro-example",
    ":kobweb-example",
    ":koin-example",
    ":catalog-api",
    ":catalog-impl",
    ":checkout-api",
    ":checkout-impl",
    ":consumer-fixture",
    ":component-wrapper-fixture",
)

// Unique Gradle identities avoid api/impl capability collisions between features.
project(":catalog-api").projectDir = file("feature/catalog/api")
project(":catalog-impl").projectDir = file("feature/catalog/impl")
project(":checkout-api").projectDir = file("feature/checkout/api")
project(":checkout-impl").projectDir = file("feature/checkout/impl")
