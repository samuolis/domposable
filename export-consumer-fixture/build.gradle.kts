import io.github.samuolis.domposable.export.ExportSite

buildscript {
    val releaseVersion =
        java.util
            .Properties()
            .apply {
                file("../version.properties").inputStream().use(::load)
            }.getProperty("VERSION")
    repositories {
        maven {
            url = uri(providers.gradleProperty("domposable.consumer.repository").getOrElse("../build/repository"))
        }
        mavenCentral()
    }
    dependencies { classpath("io.github.samuolis.domposable:domposable-export-gradle:$releaseVersion") }
}

val stageSite =
    tasks.register<Sync>("stageSite") {
        from("web")
        into(layout.buildDirectory.dir("site"))
    }

tasks.register<ExportSite>("exportPublicPages") {
    dependsOn(stageSite)
    directory.set(layout.buildDirectory.dir("site"))
    outputDirectory.set(layout.buildDirectory.dir("exported"))
    canonicalOrigin.set("https://example.com")
    pages.addAll("/about/", "/events/42")
}
