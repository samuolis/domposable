package io.github.samuolis.domposable.ksp

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.Modifier

/** Validates annotated Props declarations, then emits built-in and consumer component providers. */
internal class DomposableComponentGenerator(
    private val environment: SymbolProcessorEnvironment,
) {
    private data class ComponentDeclaration(
        val name: String,
        val qualifiedName: String,
        val declaration: KSClassDeclaration,
    )

    private val annotationName = "io.github.samuolis.domposable.foundation.DomposableComponent"
    private val emitted = mutableSetOf<String>()
    private val kotlinKeywords =
        setOf(
            "as",
            "break",
            "class",
            "continue",
            "do",
            "else",
            "false",
            "for",
            "fun",
            "if",
            "in",
            "interface",
            "is",
            "null",
            "object",
            "package",
            "return",
            "super",
            "this",
            "throw",
            "true",
            "try",
            "typealias",
            "typeof",
            "val",
            "var",
            "when",
            "while",
        )

    fun process(resolver: Resolver) {
        val components =
            resolver
                .getSymbolsWithAnnotation(annotationName)
                .filterIsInstance<KSClassDeclaration>()
                .toList()
        if (environment.options["domposable.internal.components"] == "true" && components.isNotEmpty() &&
            emitted.add("components")
        ) {
            validateComponents(components, "public")?.let(::generateComponents)
        }
        val packageName = environment.options["domposable.components.package"]
        if (packageName != null && components.isNotEmpty() && emitted.add("extension-components")) {
            val providerName = environment.options["domposable.components.provider"] ?: "GeneratedComponents"
            val visibility = environment.options["domposable.components.visibility"] ?: "public"
            if (!isValidPackage(packageName) || !isValidProviderName(providerName) ||
                visibility !in setOf("public", "internal")
            ) {
                environment.logger.error(
                    "Invalid Domposable component package, provider name or visibility; " +
                        "use a non-keyword Kotlin package/name and public or internal visibility",
                )
            } else {
                validateComponents(components, visibility)?.let {
                    generateExtensionComponents(it, packageName, providerName, visibility)
                }
            }
        }
    }

    private fun isKotlinIdentifier(value: String): Boolean =
        value.matches(Regex("[A-Za-z_][A-Za-z0-9_]*")) && value !in kotlinKeywords

    private fun isValidProviderName(value: String): Boolean =
        value.matches(Regex("[A-Z][A-Za-z0-9_]*")) && value !in kotlinKeywords

    private fun isValidPackage(value: String): Boolean =
        value.isNotBlank() && value.split('.').all(::isKotlinIdentifier)

    private fun validateComponents(
        components: List<KSClassDeclaration>,
        visibility: String,
    ): List<ComponentDeclaration>? {
        val declarations = components.mapNotNull { validateComponent(it, visibility) }
        val duplicates = declarations.groupBy { it.name }.filterValues { it.size > 1 }
        duplicates.forEach { (name, matches) ->
            matches.forEach { match ->
                environment.logger.error("Duplicate Domposable component name '$name'", match.declaration)
            }
        }
        val allValid = declarations.size == components.size && duplicates.isEmpty()
        return declarations.sortedBy { it.name }.takeIf { allValid }
    }

    private fun validateComponent(
        declaration: KSClassDeclaration,
        visibility: String,
    ): ComponentDeclaration? {
        val annotation =
            declaration.annotations.firstOrNull { candidate ->
                candidate.annotationType
                    .resolve()
                    .declaration.qualifiedName
                    ?.asString() == annotationName
            }
        val name =
            annotation
                ?.arguments
                ?.firstOrNull { it.name?.asString() == "name" }
                ?.value as? String
        // KSP2 does not reliably surface local Props declarations; retain the guard for surfaced symbols.
        val qualifiedName = declaration.qualifiedName?.asString()
        val error =
            when {
                name == null -> {
                    "@DomposableComponent requires a constant component name"
                }

                !name.matches(Regex("[A-Z][A-Za-z0-9]*")) || name in kotlinKeywords -> {
                    "Domposable component names must be non-keyword Kotlin type identifiers (for example, ProductCard)"
                }

                qualifiedName == null -> {
                    "Domposable component Props must have a qualified name (local classes are unsupported)"
                }

                declaration.typeParameters.isNotEmpty() -> {
                    "Generic Domposable component Props are unsupported by generated providers; " +
                        "register the generic component with the typed provider API manually"
                }

                else -> {
                    inaccessiblePropsReason(declaration, visibility)
                }
            }
        if (error != null) environment.logger.error(error, declaration)
        return if (error == null) ComponentDeclaration(name!!, qualifiedName!!, declaration) else null
    }

    private fun inaccessiblePropsReason(
        declaration: KSClassDeclaration,
        visibility: String,
    ): String? =
        generateSequence(declaration as KSDeclaration?) { it.parentDeclaration }
            .mapNotNull { current ->
                when {
                    Modifier.PRIVATE in current.modifiers || Modifier.PROTECTED in current.modifiers -> {
                        "Domposable component Props and enclosing declarations cannot be private or protected"
                    }

                    visibility == "public" && Modifier.INTERNAL in current.modifiers -> {
                        "Public Domposable component providers cannot expose internal Props or enclosing declarations"
                    }

                    else -> {
                        null
                    }
                }
            }.firstOrNull()

    private fun generateComponents(declarations: List<ComponentDeclaration>) {
        val output =
            buildString {
                appendLine("package io.github.samuolis.domposable.ui")
                appendLine("import androidx.compose.runtime.Composable")
                for ((name, props, _) in declarations) {
                    appendLine("fun interface Domposable${name}Renderer {")
                    appendLine(
                        "    @Composable fun Render(props: $props, modifier: DomposableModifier, " +
                            "content: @Composable () -> Unit)",
                    )
                    appendLine("}")
                    appendLine(
                        "@Composable fun Domposable$name(props: $props, modifier: " +
                            "DomposableModifier = DomposableModifier, content: @Composable () -> Unit = {}) {",
                    )
                    appendLine(
                        "    LocalDomposableComponents.current.${name.replaceFirstChar(
                            Char::lowercase,
                        )}.Render(props, modifier, content)",
                    )
                    appendLine("}")
                }
                appendLine("data class DomposableComponents(")
                for ((name, _) in declarations) {
                    appendLine(
                        "    val ${name.replaceFirstChar(Char::lowercase)}: Domposable${name}Renderer,",
                    )
                }
                appendLine(")")
            }
        environment.codeGenerator
            .createNewFile(
                Dependencies(true, *declarations.mapNotNull { it.declaration.containingFile }.toTypedArray()),
                "io.github.samuolis.domposable.ui",
                "DomposableComponents",
            ).writer()
            .use { it.write(output) }
    }

    private fun generateExtensionComponents(
        declarations: List<ComponentDeclaration>,
        packageName: String,
        providerName: String,
        visibility: String,
    ) {
        val output =
            buildString {
                appendLine("package $packageName")
                appendLine("import androidx.compose.runtime.Composable")
                appendLine("import io.github.samuolis.domposable.ui.*")
                appendLine("$visibility object ${providerName}Keys {")
                for ((name, props, _) in declarations) {
                    appendLine("    val $name = DomposableComponentKey<$props>(\"$packageName.$name\")")
                }
                appendLine("}")
                for ((name, props, _) in declarations) {
                    appendLine("$visibility typealias ${name}Renderer = DomposableComponentRenderer<$props>")
                    appendLine(
                        "@Composable $visibility fun Domposable$name(props: $props, " +
                            "modifier: DomposableModifier = DomposableModifier, " +
                            "content: @Composable () -> Unit = {}) {",
                    )
                    appendLine("    DomposableComponent(${providerName}Keys.$name, props, modifier, content)")
                    appendLine("}")
                }
                appendLine("$visibility data class $providerName(")
                for ((name, _) in declarations) {
                    appendLine("    val ${name.replaceFirstChar(Char::lowercase)}: ${name}Renderer,")
                }
                appendLine(") {")
                appendLine("    fun asDomposableComponentProvider(): DomposableComponentProvider =")
                appendLine("        domposableComponentProvider {")
                for ((name, _) in declarations) {
                    appendLine("        register(${providerName}Keys.$name, ${name.replaceFirstChar(Char::lowercase)})")
                }
                appendLine("    }")
                appendLine("}")
            }
        environment.codeGenerator
            .createNewFile(
                Dependencies(
                    true,
                    *declarations.mapNotNull { it.declaration.containingFile }.toTypedArray(),
                ),
                packageName,
                providerName,
            ).writer()
            .use { it.write(output) }
    }
}
