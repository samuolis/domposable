package io.github.samuolis.domposable.ksp

import com.google.devtools.ksp.impl.KotlinSymbolProcessing
import com.google.devtools.ksp.processing.KSPJvmConfig
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
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
        options: Map<String, String> = emptyMap(),
        processorProviders: List<SymbolProcessorProvider> = listOf(DomposableProcessorProvider()),
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
                            (if (components) mapOf("domposable.internal.components" to "true") else emptyMap()) +
                            options
                    }.build()
            val logger = Logger()
            val exit = KotlinSymbolProcessing(config, processorProviders, logger).execute()
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
            @Target(AnnotationTarget.FUNCTION) annotation class TestGenerateDependency
            @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION) annotation class TestFunction
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
    fun functionProcessorDefersUnresolvedFunctionsAndEmitsOnce() {
        val calls = mutableMapOf<String, Int>()
        val provider =
            functionProcessorProvider(
                mapOf(
                    "io.github.samuolis.domposable.foundation.TestGenerateDependency" to
                        DomposableFunctionGenerator { _, _ ->
                            listOf(
                                DomposableGeneratedSource(
                                    "feature",
                                    "GeneratedDependency",
                                    "package feature\nclass GeneratedDependency",
                                ),
                            )
                        },
                    "io.github.samuolis.domposable.foundation.TestFunction" to
                        DomposableFunctionGenerator { function, _ ->
                            val name = function.simpleName.asString()
                            calls[name] = (calls[name] ?: 0) + 1
                            listOf(
                                DomposableGeneratedSource(
                                    "feature",
                                    "${name}Generated",
                                    "package feature\nfun ${name.lowercase()}Generated() = Unit",
                                ),
                            )
                        },
                ),
            )
        val result =
            process(
                """
                @io.github.samuolis.domposable.foundation.TestGenerateDependency
                fun createDependency() {}
                @io.github.samuolis.domposable.foundation.TestFunction
                fun screen(value: GeneratedDependency) {}
                """.trimIndent(),
                processorProviders = listOf(provider),
            )

        assertTrue(result.success, result.errors.toString())
        assertEquals(1, calls["screen"])
        assertContains(result.files.getValue("screenGenerated.kt"), "fun screenGenerated()")
    }

    @Test
    fun functionProcessorGeneratesBothAnnotatedOverloads() {
        val calls = mutableListOf<String>()
        val provider =
            functionProcessorProvider(
                mapOf(
                    "io.github.samuolis.domposable.foundation.TestFunction" to
                        DomposableFunctionGenerator { function, _ ->
                            val parameterType =
                                function.parameters
                                    .single()
                                    .type
                                    .resolve()
                                    .declaration.simpleName
                                    .asString()
                            calls += parameterType
                            listOf(
                                DomposableGeneratedSource(
                                    "feature",
                                    "screen$parameterType",
                                    "package feature\nfun screen$parameterType() = Unit",
                                ),
                            )
                        },
                ),
            )
        val result =
            process(
                """
                @io.github.samuolis.domposable.foundation.TestFunction
                fun screen(value: String) {}
                @io.github.samuolis.domposable.foundation.TestFunction
                fun screen(value: Int) {}
                """.trimIndent(),
                processorProviders = listOf(provider),
            )

        assertTrue(result.success, result.errors.toString())
        assertEquals(setOf("String", "Int"), calls.toSet())
        assertContains(result.files.getValue("screenString.kt"), "fun screenString()")
        assertContains(result.files.getValue("screenInt.kt"), "fun screenInt()")
    }

    @Test
    fun domposableScreenOverloadOutputCollisionIsReported() {
        val result =
            process(
                """
                @Composable @DomposableScreen(Route::class) fun Detail(value: String) {}
                @Composable @DomposableScreen(Route::class) fun Detail(value: Int) {}
                """.trimIndent(),
            )

        assertFalse(result.success)
        assertTrue(
            result.errors.any { "feature.generated.detailDestination" in it && "generated" in it },
            result.errors.toString(),
        )
    }

    @Test
    fun functionProcessorReportsNonFunctionAnnotationTargets() {
        val provider =
            functionProcessorProvider(
                mapOf(
                    "io.github.samuolis.domposable.foundation.TestFunction" to
                        DomposableFunctionGenerator { _, _ -> emptyList() },
                ),
            )
        val result =
            process(
                "@io.github.samuolis.domposable.foundation.TestFunction class InvalidTarget",
                processorProviders = listOf(provider),
            )

        assertFalse(result.success)
        assertTrue(result.errors.any { it.contains("can only annotate a function") }, result.errors.toString())
    }

    private fun functionProcessorProvider(
        generators: Map<String, DomposableFunctionGenerator>,
    ): SymbolProcessorProvider =
        object : SymbolProcessorProvider {
            override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
                DomposableFunctionProcessor(environment, generators)
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

    @Test
    fun consumerComponentsUseTypedKeysWithoutReplacingThePublishedProvider() {
        val result =
            process(
                "@DomposableComponent(\"ProductCard\") class ProductCardProps(val title: String)",
                options =
                    mapOf(
                        "domposable.components.package" to "feature.generated",
                        "domposable.components.provider" to "FeatureComponents",
                        "domposable.components.visibility" to "internal",
                    ),
            )
        assertTrue(result.success, result.errors.toString())
        assertFalse("DomposableComponents.kt" in result.files)
        val generated = result.files.getValue("FeatureComponents.kt")
        assertContains(generated, "package feature.generated")
        assertContains(generated, "internal object FeatureComponentsKeys")
        assertContains(generated, "DomposableComponentKey<feature.ProductCardProps>")
        assertContains(generated, "DomposableComponent(FeatureComponentsKeys.ProductCard, props, modifier, content)")
        assertContains(generated, "register(FeatureComponentsKeys.ProductCard, productCard)")
    }

    @Test
    fun invalidConsumerProviderOptionsFailWithDiagnostics() {
        val result =
            process(
                "@DomposableComponent(\"ProductCard\") class ProductCardProps",
                options = mapOf("domposable.components.package" to "feature.invalid-package"),
            )
        assertFalse(result.success)
        assertTrue(result.errors.any { "Invalid Domposable component package" in it })
        assertTrue(result.files.isEmpty())

        val keywordPackage =
            process(
                "@DomposableComponent(\"ProductCard\") class ProductCardProps",
                options = mapOf("domposable.components.package" to "feature.class"),
            )
        assertFalse(keywordPackage.success)
        assertTrue(keywordPackage.errors.any { "non-keyword Kotlin package/name" in it })
        assertTrue(keywordPackage.files.isEmpty())

        val keywordProvider =
            process(
                "@DomposableComponent(\"ProductCard\") class ProductCardProps",
                options =
                    mapOf(
                        "domposable.components.package" to "feature.generated",
                        "domposable.components.provider" to "when",
                    ),
            )
        assertFalse(keywordProvider.success)
        assertTrue(keywordProvider.errors.any { "non-keyword Kotlin package/name" in it })
        assertTrue(keywordProvider.files.isEmpty())
    }

    @Test
    fun unsupportedConsumerPropsDeclarationsFailWithKspDiagnostics() {
        val duplicate =
            process(
                "@DomposableComponent(\"ProductCard\") class FirstProps\n" +
                    "@DomposableComponent(\"ProductCard\") class SecondProps",
                options = mapOf("domposable.components.package" to "feature.generated"),
            )
        assertFalse(duplicate.success)
        assertTrue(duplicate.errors.any { "Duplicate Domposable component name 'ProductCard'" in it })
        assertTrue(duplicate.files.isEmpty())

        val invalidName =
            process(
                "@DomposableComponent(\"bad-name\") class ProductCardProps",
                options = mapOf("domposable.components.package" to "feature.generated"),
            )
        assertFalse(invalidName.success)
        assertTrue(invalidName.errors.any { "non-keyword Kotlin type identifiers" in it })
        assertTrue(invalidName.files.isEmpty())

        val generic =
            process(
                "@DomposableComponent(\"ProductCard\") class ProductCardProps<T>",
                options = mapOf("domposable.components.package" to "feature.generated"),
            )
        assertFalse(generic.success)
        assertTrue(generic.errors.any { "Generic Domposable component Props are unsupported" in it })
        assertTrue(generic.files.isEmpty())

        // KSP2 does not report local classes through getSymbolsWithAnnotation, so generation cannot diagnose them.
        // Keep the qualified-name guard for any future KSP version that does surface one.
        val local =
            process(
                "fun declarations() { @DomposableComponent(\"ProductCard\") class ProductCardProps }",
                options = mapOf("domposable.components.package" to "feature.generated"),
            )
        assertTrue(local.success, local.errors.toString())
        assertTrue(local.files.isEmpty())
    }

    @Test
    fun consumerProviderVisibilityMustExposeItsPropsType() {
        val publicProvider =
            process(
                "@DomposableComponent(\"ProductCard\") internal class ProductCardProps",
                options = mapOf("domposable.components.package" to "feature.generated"),
            )
        assertFalse(publicProvider.success)
        assertTrue(publicProvider.errors.any { "cannot expose internal Props" in it })
        assertTrue(publicProvider.files.isEmpty())

        val internalProvider =
            process(
                "@DomposableComponent(\"ProductCard\") internal class ProductCardProps",
                options =
                    mapOf(
                        "domposable.components.package" to "feature.generated",
                        "domposable.components.visibility" to "internal",
                    ),
            )
        assertTrue(internalProvider.success, internalProvider.errors.toString())
        assertContains(
            internalProvider.files.getValue("GeneratedComponents.kt"),
            "internal typealias ProductCardRenderer",
        )

        val privateProps =
            process(
                "@DomposableComponent(\"ProductCard\") private class ProductCardProps",
                options =
                    mapOf(
                        "domposable.components.package" to "feature.generated",
                        "domposable.components.visibility" to "internal",
                    ),
            )
        assertFalse(privateProps.success)
        assertTrue(privateProps.errors.any { "cannot be private or protected" in it })
        assertTrue(privateProps.files.isEmpty())
    }
}
