package io.github.samuolis.domposable.export

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/** Build-time HTML export of explicit public routes using the application's existing browser renderer. */
@DisableCachingByDefault(because = "Anonymous public data can change independently of the application build")
abstract class ExportSite : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val directory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val canonicalOrigin: Property<String>

    @get:Input
    abstract val pages: ListProperty<String>

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val pagesFile: RegularFileProperty

    @get:Input
    abstract val rootSelector: Property<String>

    @get:Input
    abstract val markerAttribute: Property<String>

    @get:Input
    abstract val nodeExecutable: Property<String>

    @get:Input
    abstract val npmExecutable: Property<String>

    @get:Inject
    protected abstract val execOperations: ExecOperations

    init {
        group = "distribution"
        description = "Export public pages as HTML using the existing browser application"
        outputDirectory.convention(directory)
        rootSelector.convention("#root")
        markerAttribute.convention("data-domposable-prerender")
        pages.convention(emptyList())
        nodeExecutable.convention("node")
        npmExecutable.convention(if (System.getProperty("os.name").startsWith("Windows")) "npm.cmd" else "npm")
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun export() {
        val publicPages =
            pages.get() +
                pagesFile.orNull
                    ?.asFile
                    ?.readLines()
                    .orEmpty()
                    .map(String::trim)
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
        require(publicPages.isNotEmpty()) { "Declare public pages using pagesFile or pages" }
        val runtime = temporaryDir.resolve("runtime")
        for (name in listOf(
            "package.json",
            "package-lock.json",
            "tools/page-export/index.mjs",
            "tools/page-export/cli.mjs",
        )) {
            val target = runtime.resolve(name)
            target.parentFile.mkdirs()
            checkNotNull(
                javaClass.getResourceAsStream("/domposable-export/$name"),
            ) { "Missing bundled exporter: $name" }
                .use { source -> target.outputStream().use(source::copyTo) }
        }
        val config = temporaryDir.resolve("export.json")
        config.writeText(
            JsonOutput.toJson(
                mapOf(
                    "directory" to directory.get().asFile.absolutePath,
                    "outputDir" to outputDirectory.get().asFile.absolutePath,
                    "canonicalOrigin" to canonicalOrigin.get(),
                    "pages" to publicPages,
                    "root" to rootSelector.get(),
                    "markerAttribute" to markerAttribute.get(),
                ),
            ),
        )
        // The published task carries the reviewed exporter and lockfile; consumers need no npm project.
        execOperations.exec {
            it.workingDir(runtime)
            it.commandLine(npmExecutable.get(), "ci", "--omit=dev", "--ignore-scripts", "--no-audit", "--no-fund")
        }
        execOperations.exec {
            it.workingDir(runtime)
            it.commandLine(
                npmExecutable.get(),
                "exec",
                "--offline",
                "--",
                "playwright",
                "install",
                "chromium",
                "--only-shell",
            )
        }
        execOperations.exec {
            it.workingDir(runtime)
            it.commandLine(nodeExecutable.get(), "tools/page-export/cli.mjs", config.absolutePath)
        }
    }
}
