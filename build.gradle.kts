@file:OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.api.dsl.Lint
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import dev.detekt.gradle.Detekt
import groovy.json.JsonSlurper
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

val releaseVersion =
    Properties()
        .apply {
            rootProject.file("version.properties").inputStream().use(::load)
        }.getProperty("VERSION")

allprojects {
    group = "io.github.samuolis.domposable"
    version = releaseVersion
}

val ktlintTool =
    configurations.create("ktlintTool") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        }
    }
dependencies { ktlintTool("com.pinterest.ktlint:ktlint-cli:${libs.versions.ktlint.get()}:all@jar") }

val handwrittenKotlin =
    fileTree(rootDir) {
        include("**/src/**/*.kt", "**/*.kts")
        exclude("**/build/**", "**/generated/**", "**/.gradle/**", "**/node_modules/**", ".release/**")
    }

fun registerKtlint(
    name: String,
    format: Boolean,
) = tasks.register<JavaExec>(name) {
    group = if (format) "formatting" else "verification"
    description = if (format) "Format handwritten Kotlin and Gradle scripts" else "Check Kotlin formatting"
    classpath = ktlintTool
    mainClass.set("com.pinterest.ktlint.Main")
    inputs.files(handwrittenKotlin)
    inputs.file(".editorconfig")
    val reports = layout.buildDirectory.dir("reports/ktlint/$name")
    if (!format) outputs.dir(reports)
    doFirst {
        reports.get().asFile.mkdirs()
        args =
            buildList {
                if (format) add("--format")
                add("--relative")
                for (reporter in listOf("plain", "checkstyle", "sarif")) {
                    val extension =
                        when (reporter) {
                            "checkstyle" -> "xml"
                            "sarif" -> "sarif"
                            else -> "txt"
                        }
                    add("--reporter=$reporter,output=${reports.get().asFile}/ktlint.$extension")
                }
                addAll(handwrittenKotlin.files.sorted().map { it.absolutePath })
            }
    }
}

val ktlintCheck = registerKtlint("ktlintCheck", false)
registerKtlint("ktlintFormat", true)

val androidLint =
    tasks.register("androidLint") {
        group = "verification"
        description = "Run Android lint on every Android application and KMP library"
    }

fun Lint.configureLibraryChecks() {
    abortOnError = true
    warningsAsErrors = true
    checkDependencies = true
    checkTestSources = true
    // Dependency upgrades are reviewed through Dependabot, rather than forced by lint.
    disable += setOf("AndroidGradlePluginVersion", "GradleDependency")
}

subprojects {
    val lintTaskPath = "$path:lint"
    plugins.withId("com.android.application") {
        extensions.configure<ApplicationExtension> { lint.configureLibraryChecks() }
        androidLint.configure { dependsOn(lintTaskPath) }
    }
    plugins.withId("com.android.kotlin.multiplatform.library") {
        apply(plugin = "com.android.lint")
        extensions.configure<KotlinMultiplatformExtension> {
            targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
                lint.configureLibraryChecks()
            }
        }
        androidLint.configure { dependsOn(lintTaskPath) }
    }
}

val coverageProjects =
    listOf(
        "domposable-foundation",
        "domposable-ui",
        "domposable-design-material",
        "domposable-adaptive",
        "domposable-screens",
        "domposable-navigation3",
        "domposable-lifecycle-viewmodel",
        "domposable-ksp",
        "demo",
        "catalog-api",
        "catalog-impl",
        "checkout-api",
        "checkout-impl",
        "metro-example",
        "koin-example",
    )

subprojects {
    if (name in coverageProjects) apply(plugin = "org.jetbrains.kotlinx.kover")
}

dependencies {
    coverageProjects.forEach { kover(project(":$it")) }
}

val coveragePolicy = JsonSlurper().parse(file("config/coverage.json")) as Map<*, *>
val totalCoverageFloor = coveragePolicy["total"] as Map<*, *>

kover {
    reports {
        filters {
            includes { classes("io.github.samuolis.domposable.*") }
            excludes {
                classes("io.github.samuolis.domposable.demo.*", "*.generated.*", "*.BuildConfig", "*.R", "*.R$*")
            }
        }
        total {
            html { htmlDir = layout.buildDirectory.dir("reports/coverage/html") }
            xml { xmlFile = layout.buildDirectory.file("reports/coverage/coverage.xml") }
            verify {
                rule("Library coverage") {
                    minBound((totalCoverageFloor["line"] as Number).toInt())
                    minBound((totalCoverageFloor["branch"] as Number).toInt(), CoverageUnit.BRANCH)
                }
            }
        }
    }
}

val verifyCoverage =
    tasks.register<Exec>("verifyCoverage") {
        group = "verification"
        description = "Enforce total and individual core package coverage floors"
        dependsOn("koverXmlReport")
        inputs.files("scripts/ci/coverage_report.py", "config/coverage.json")
        commandLine(
            "python3",
            "scripts/ci/coverage_report.py",
            "build/reports/coverage/coverage.xml",
            "--verify",
            "config/coverage.json",
        )
    }

val qualityCheck =
    tasks.register("qualityCheck") {
        group = "verification"
        description = "Check Kotlin formatting, static analysis, Android lint and measured library coverage"
        dependsOn(ktlintCheck, "detekt", androidLint, "koverHtmlReport", "koverVerify", verifyCoverage)
    }

val binaryApiPolicy = JsonSlurper().parse(file("config/binary-api.json")) as Map<*, *>
val binaryApiModules =
    (binaryApiPolicy["runtime_modules"] as List<*>) + (binaryApiPolicy["processor_modules"] as List<*>) +
        (binaryApiPolicy["tooling_modules"] as List<*>)

tasks.register("binaryApiCheck") {
    group = "verification"
    description = "Validate every published module against its reviewed JVM, Android and Klib ABI baselines"
    dependsOn(binaryApiModules.map { ":$it:checkKotlinAbi" })
}

subprojects {
    if (name in binaryApiModules) {
        plugins.withId("org.jetbrains.kotlin.multiplatform") {
            extensions.configure<KotlinMultiplatformExtension> {
                abiValidation {
                    // CI runs on macOS so every target is compiled rather than inferred.
                    keepLocallyUnsupportedTargets.set(
                        !providers.gradleProperty("domposable.abi.strict").map(String::toBoolean).getOrElse(false),
                    )
                }
            }
        }
    }
}

tasks.named("check") { dependsOn(qualityCheck, "binaryApiCheck") }

detekt {
    source.setFrom(subprojects.map { it.file("src") })
    config.setFrom(files("config/detekt.yml"))
    buildUponDefaultConfig = true
    ignoreFailures = false
    basePath.set(rootDir)
}

tasks.withType<Detekt>().configureEach {
    exclude("**/build/**", "**/generated/**")
    reports {
        html.required.set(true)
        checkstyle.required.set(true)
        sarif.required.set(true)
        markdown.required.set(true)
    }
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
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("samuolis")
                        name.set("Samuolis")
                    }
                }
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
