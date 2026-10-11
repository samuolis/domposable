package io.github.samuolis.domposable.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

class DomposableProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = DomposableProcessor(environment)
}

class DomposableProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val emitted = mutableSetOf<String>()
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val components = resolver.getSymbolsWithAnnotation("io.github.samuolis.domposable.foundation.DomposableComponent")
            .filterIsInstance<KSClassDeclaration>().toList()
        if (environment.options["domposable.internal.components"] == "true" && components.isNotEmpty() && emitted.add("components")) generateComponents(components)
        resolver.getSymbolsWithAnnotation("io.github.samuolis.domposable.foundation.DomposableScreen")
            .filterIsInstance<KSFunctionDeclaration>().forEach(::generateScreen)
        return emptyList()
    }

    private fun generateComponents(components: List<KSClassDeclaration>) {
        val declarations = components.map { declaration ->
            val name = declaration.annotations.first { it.shortName.asString() == "DomposableComponent" }
                .arguments.first { it.name?.asString() == "name" }.value as String
            require(name.matches(Regex("[A-Z][A-Za-z0-9]*"))) { "Component names must be Kotlin identifiers" }
            name to declaration
        }.sortedBy { it.first }
        require(declarations.map { it.first }.distinct().size == declarations.size) { "Duplicate component name" }
        val output = buildString {
            appendLine("package io.github.samuolis.domposable.ui")
            appendLine("import androidx.compose.runtime.Composable")
            for ((name, declaration) in declarations) {
                val props = declaration.qualifiedName!!.asString()
                appendLine("fun interface Domposable${name}Renderer {")
                appendLine("    @Composable fun Render(props: $props, modifier: DomposableModifier, content: @Composable () -> Unit)")
                appendLine("}")
                appendLine("@Composable fun Domposable$name(props: $props, modifier: DomposableModifier = DomposableModifier, content: @Composable () -> Unit = {}) {")
                appendLine("    LocalDomposableComponents.current.${name.replaceFirstChar(Char::lowercase)}.Render(props, modifier, content)")
                appendLine("}")
            }
            appendLine("data class DomposableComponents(")
            for ((name, _) in declarations) appendLine("    val ${name.replaceFirstChar(Char::lowercase)}: Domposable${name}Renderer,")
            appendLine(")")
        }
        environment.codeGenerator.createNewFile(
            Dependencies(true, *components.mapNotNull { it.containingFile }.toTypedArray()),
            "io.github.samuolis.domposable.ui", "DomposableComponents",
        ).writer().use { it.write(output) }
    }

    private fun generateScreen(screen: KSFunctionDeclaration) {
        val qualifiedName = screen.qualifiedName?.asString() ?: return
        if (!emitted.add(qualifiedName)) return
        if (Modifier.PRIVATE in screen.modifiers || screen.parentDeclaration != null || screen.extensionReceiver != null || screen.typeParameters.isNotEmpty()) {
            environment.logger.error("Domposable screens must be visible, non-generic top-level functions", screen)
            return
        }
        if (screen.annotations.none { it.shortName.asString() == "Composable" }) {
            environment.logger.error("@DomposableScreen requires @Composable", screen)
            return
        }
        if (screen.returnType?.resolve()?.declaration?.qualifiedName?.asString() != "kotlin.Unit") {
            environment.logger.error("A screen returns Unit; application models are supplied by the application", screen)
            return
        }
        val annotation = screen.annotations.first { it.shortName.asString() == "DomposableScreen" }
        val route = annotation.arguments.first { it.name?.asString() == "route" }.value as KSType
        val routeName = route.declaration.qualifiedName!!.asString()
        val name = screen.simpleName.asString().replaceFirstChar(Char::lowercase) + "Destination"
        val default = if (screen.parameters.all { it.hasDefault }) " = { $qualifiedName() }" else ""
        val visibility = if (Modifier.INTERNAL in screen.modifiers) "internal " else ""
        val packageName = screen.packageName.asString() + ".generated"
        val code = """
            package $packageName
            import androidx.compose.runtime.Composable
            import io.github.samuolis.domposable.screens.*
            ${visibility}fun $name(render: @Composable DomposableEntryScope<$routeName>.() -> Unit$default): DomposableDestination<$routeName> =
                DomposableDestination($routeName::class, render)
        """.trimIndent()
        environment.codeGenerator.createNewFile(Dependencies(false, screen.containingFile!!), packageName, name)
            .writer().use { it.write(code) }
    }
}
