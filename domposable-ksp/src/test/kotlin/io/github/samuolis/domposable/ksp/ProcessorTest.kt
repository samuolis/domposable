package io.github.samuolis.domposable.ksp

import com.google.devtools.ksp.impl.KotlinSymbolProcessing
import com.google.devtools.ksp.processing.KSPJvmConfig
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Real KSP analysis and the production provider run inside the instrumented test JVM. */
class ProcessorTest {
    private data class Result(
        val success: Boolean,
        val files: Map<String, String>,
        val errors: List<String>,
    )

    private class Logger : KSPLogger {
        val errors = mutableListOf<String>()

        override fun logging(
            message: String,
            symbol: KSNode?,
        ) = Unit

        override fun info(
            message: String,
            symbol: KSNode?,
        ) = Unit

        override fun warn(
            message: String,
            symbol: KSNode?,
        ) = Unit

        override fun error(
            message: String,
            symbol: KSNode?,
        ) {
            errors += message
        }

        override fun exception(e: Throwable) {
            errors += e.stackTraceToString()
        }
    }

    private fun process(
        source: String,
        lifecycle: Boolean = false,
        components: Boolean = false,
    ): Result {
        val root = Files.createTempDirectory("domposable-ksp-test").toFile()
        try {
            val sources = File(root, "src").apply { mkdirs() }
            writeSources(sources, source, lifecycle)
            val config =
                KSPJvmConfig
                    .Builder()
                    .apply {
                        moduleName = "processor-test"
                        sourceRoots = listOf(sources)
                        projectBaseDir = root
                        outputBaseDir = File(root, "output")
                        cachesDir = File(root, "caches")
                        classOutputDir = File(root, "classes")
                        kotlinOutputDir = File(root, "kotlin")
                        javaOutputDir = File(root, "java")
                        resourceOutputDir = File(root, "resources")
                        languageVersion = "2.4"
                        apiVersion = "2.4"
                        jvmTarget = "21"
                        jdkHome = File(System.getProperty("java.home"))
                        libraries =
                            listOf(
                                File(
                                    Unit::class.java.protectionDomain.codeSource.location
                                        .toURI(),
                                ),
                            )
                        processorOptions =
                            if (components) mapOf("domposable.internal.components" to "true") else emptyMap()
                    }.build()
            val logger = Logger()
            val exit = KotlinSymbolProcessing(config, listOf(DomposableProcessorProvider()), logger).execute()
            val files =
                config.kotlinOutputDir
                    .walkTopDown()
                    .filter { it.isFile }
                    .associate { it.name to it.readText() }
            return Result(exit == KotlinSymbolProcessing.ExitCode.OK, files, logger.errors)
        } finally {
            root.deleteRecursively()
        }
    }

    private fun writeSources(
        sources: File,
        source: String,
        lifecycle: Boolean,
    ) {
        File(sources, "Compose.kt").writeText(
            "package androidx.compose.runtime\n" +
                "@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE) annotation class Composable",
        )
        File(sources, "Annotations.kt").writeText(
            """
            package io.github.samuolis.domposable.foundation
            import kotlin.reflect.KClass
            annotation class DomposableScreen(val route: KClass<*>, val viewModel: Boolean = false)
            annotation class DomposableComponent(val name: String)
            """.trimIndent(),
        )
        File(sources, "Screens.kt").writeText(
            """
            package io.github.samuolis.domposable.screens
            import androidx.compose.runtime.Composable
            class DomposableEntryScope<R : Any>(val route: R)
            class DomposableDestination<R : Any>(
                type: kotlin.reflect.KClass<R>,
                render: @Composable DomposableEntryScope<R>.() -> Unit,
            )
            """.trimIndent(),
        )
        if (lifecycle) {
            File(sources, "Lifecycle.kt").writeText(
                """
                package io.github.samuolis.domposable.lifecycle
                import androidx.compose.runtime.Composable
                import io.github.samuolis.domposable.screens.DomposableEntryScope
                class DomposableViewModelOwner
                @Composable fun <R : Any> DomposableViewModelEntry(
                    scope: DomposableEntryScope<R>, content: @Composable () -> Unit,
                ) = content()
                """.trimIndent(),
            )
        }
        File(sources, "Feature.kt").writeText(
            """
            package feature
            import androidx.compose.runtime.Composable
            import io.github.samuolis.domposable.foundation.DomposableScreen
            import io.github.samuolis.domposable.foundation.DomposableComponent
            import io.github.samuolis.domposable.screens.DomposableEntryScope
            data class Route(val id: String)
            class Model
            fun injected(): Model = Model()
            $source
            """.trimIndent(),
        )
    }

    @Test
    fun routeAndEntryScopeAreSuppliedWithoutFactories() {
        val result =
            process(
                "@Composable @DomposableScreen(Route::class) " +
                    "fun Detail(route: Route, scope: DomposableEntryScope<Route>) {}",
            )
        assertTrue(result.success, result.errors.toString())
        val entry = result.files.getValue("detailEntry.kt")
        assertContains(entry, "fun detailEntry(): DomposableDestination<Route>")
        assertContains(entry, "route = this.route")
        assertContains(entry, "scope = this")
        assertContains(
            result.files.getValue("DomposableEntries.kt"),
            "fun domposableEntries(): List<DomposableDestination<*>>",
        )
    }

    @Test
    fun injectionDefaultsStayInsideTheViewModelOwner() {
        val result =
            process(
                "@Composable @DomposableScreen(Route::class, viewModel = true) " +
                    "internal fun Detail(route: Route, model: Model = injected()) {}",
                lifecycle = true,
            )
        assertTrue(result.success, result.errors.toString())
        val entry = result.files.getValue("detailEntry.kt")
        assertContains(entry, "internal fun detailEntry(model: Model)")
        assertContains(entry, "internal fun detailEntry():")
        assertContains(entry, "DomposableViewModelEntry(this)")
        assertEquals(1, Regex("model = model").findAll(entry).count())
        assertFalse("injected()" in entry)
        assertContains(result.files.getValue("DomposableEntries.kt"), "internal fun domposableEntries():")
    }

    @Test
    fun requiredDependenciesCannotBeOmitted() {
        val result =
            process(
                "@Composable @DomposableScreen(Route::class) fun Detail(model: Model, title: String = \"Product\") {}",
            )
        assertTrue(result.success, result.errors.toString())
        val entry = result.files.getValue("detailEntry.kt")
        assertContains(entry, "fun detailEntry(model: Model, title: String)")
        assertContains(entry, "fun detailEntry(model: Model)")
        assertFalse("fun detailEntry()" in entry)
        assertContains(result.files.getValue("DomposableEntries.kt"), "fun domposableEntries(model: Model)")
    }

    @Test
    fun aggregateSharesDependenciesAndQualifiesConflictingTypes() {
        val result =
            process(
                """
                @Composable @DomposableScreen(Route::class) fun Alpha(model: Model, value: String) {}
                @Composable @DomposableScreen(Route::class) fun Beta(model: Model, value: Int) {}
                """.trimIndent(),
            )
        assertTrue(result.success, result.errors.toString())
        val group = result.files.getValue("DomposableEntries.kt")
        assertContains(group, "model: Model")
        assertEquals(1, Regex("model: Model").findAll(group).count())
        assertContains(group, "alphaEntryValue: String")
        assertContains(group, "betaEntryValue: Int")
        assertTrue(group.indexOf("alphaEntry(") < group.indexOf("betaEntry("))
    }

    @Test
    fun missingLifecycleDependencyHasAnActionableDiagnostic() {
        val result = process("@Composable @DomposableScreen(Route::class, viewModel = true) fun Detail() {}")
        assertFalse(result.success)
        assertTrue(result.errors.any { "requires the optional domposable-lifecycle-viewmodel dependency" in it })
        assertTrue(result.files.isEmpty())
    }

    @Test
    fun invalidScreenDeclarationsFailBeforeGeneratingEntries() {
        val declarations =
            listOf(
                "@Composable @DomposableScreen(Route::class) private fun Detail() {}",
                "class Holder { @Composable @DomposableScreen(Route::class) fun Detail() {} }",
                "@Composable @DomposableScreen(Route::class) fun Model.Detail() {}",
                "@Composable @DomposableScreen(Route::class) fun <T> Detail(model: T) {}",
                "@Composable @DomposableScreen(Route::class) fun Detail(vararg model: Model) {}",
            )
        declarations.forEach { declaration ->
            val result = process(declaration)
            assertFalse(result.success, declaration)
            assertTrue(
                result.errors.any {
                    "visible, non-generic top-level functions without varargs" in it
                },
                result.errors.toString(),
            )
            assertTrue(result.files.isEmpty(), declaration)
        }
    }

    @Test
    fun composableAndUnitReturnTypeAreRequired() {
        val missing = process("@DomposableScreen(Route::class) fun Detail() {}")
        assertFalse(missing.success)
        assertTrue(missing.errors.any { "requires @Composable" in it })
        val returnsModel = process("@Composable @DomposableScreen(Route::class) fun Detail(): Model = Model()")
        assertFalse(returnsModel.success)
        assertTrue(returnsModel.errors.any { "A screen returns Unit" in it })
        assertTrue(missing.files.isEmpty() && returnsModel.files.isEmpty())
    }

    @Test
    fun legacyDestinationUsesDefaultsOnlyWhenAllArgumentsAreOptional() {
        val result = process("@Composable @DomposableScreen(Route::class) fun Detail(model: Model = injected()) {}")
        assertTrue(result.success, result.errors.toString())
        assertContains(result.files.getValue("detailDestination.kt"), "= { Detail() }")
        val required = process("@Composable @DomposableScreen(Route::class) fun Detail(model: Model) {}")
        assertTrue(required.success, required.errors.toString())
        assertFalse("= { Detail() }" in required.files.getValue("detailDestination.kt"))
    }

    @Test
    fun componentGenerationIsOptInAndSorted() {
        val source =
            "@DomposableComponent(\"Text\") class TextProps\n" +
                "@DomposableComponent(\"Button\") class ButtonProps"
        assertTrue(process(source).files.isEmpty())
        val result = process(source, components = true)
        assertTrue(result.success, result.errors.toString())
        val components = result.files.getValue("DomposableComponents.kt")
        assertContains(components, "fun interface DomposableButtonRenderer")
        assertContains(components, "LocalDomposableComponents.current.text.Render(props, modifier, content)")
        assertTrue(components.indexOf("val button:") < components.indexOf("val text:"))
    }
}
