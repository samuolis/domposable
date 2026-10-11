import com.vanniktech.maven.publish.MavenPublishBaseExtension
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.dokka)
}

val releaseVersion = Properties().apply {
    rootProject.file("version.properties").inputStream().use(::load)
}.getProperty("VERSION")

allprojects {
    group = "io.github.samuolis.domposable"
    version = releaseVersion
}

subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral()
            // Local consumer verification remains usable without release secrets.
            if (providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKey").isPresent) {
                signAllPublications()
            }
            pom {
                name.set(project.name)
                description.set("One Compose API for apps and semantic HTML")
                inceptionYear.set("2026")
                url.set("https://github.com/samuolis/domposable")
                licenses {
                    license { name.set("Apache-2.0"); url.set("https://www.apache.org/licenses/LICENSE-2.0") }
                }
                developers { developer { id.set("samuolis"); name.set("Samuolis") } }
                scm {
                    url.set("https://github.com/samuolis/domposable")
                    connection.set("scm:git:https://github.com/samuolis/domposable.git")
                    developerConnection.set("scm:git:ssh://git@github.com/samuolis/domposable.git")
                }
            }
        }
    }
    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

dependencies {
    listOf("foundation", "ui", "design-material", "adaptive", "screens", "navigation3", "lifecycle-viewmodel", "ksp")
        .forEach { dokka(project(":domposable-$it")) }
}
