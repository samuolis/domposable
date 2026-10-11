package io.github.samuolis.domposable.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*

class DomposableProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = DomposableProcessor(environment)
}

class DomposableProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val emitted = mutableSetOf<String>()
    // KSP symbols cannot be retained into finish(): later rounds invalidate their analysis tokens.
    private val screens = mutableListOf<ScreenGroupMember>()
    private data class ScreenGroupMember(val packageName: String, val entryName: String, val internal: Boolean, val dependencies: List<ParameterSpec>, val required: Set<String>)
    private data class Screen(val declaration: KSFunctionDeclaration, val route: KSType, val viewModel: Boolean) {
        val packageName get() = declaration.packageName.asString() + ".generated"
        val entryName get() = declaration.simpleName.asString().replaceFirstChar(Char::lowercase) + "Entry"
        fun automatic(parameter: KSValueParameter): Boolean {
            val type = parameter.type.resolve()
            return type == route || (type.declaration.qualifiedName?.asString() == "io.github.samuolis.domposable.screens.DomposableEntryScope" && type.arguments.singleOrNull()?.type?.resolve() == route)
        }
        val dependencies get() = declaration.parameters.filterNot(::automatic)
    }
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val components = resolver.getSymbolsWithAnnotation("io.github.samuolis.domposable.foundation.DomposableComponent")
            .filterIsInstance<KSClassDeclaration>().toList()
        if (environment.options["domposable.internal.components"] == "true" && components.isNotEmpty() && emitted.add("components")) generateComponents(components)
        val declarations = resolver.getSymbolsWithAnnotation("io.github.samuolis.domposable.foundation.DomposableScreen").toList()
        val deferred = declarations.filterNot { it.validate() }
        declarations.filter { it.validate() }.filterIsInstance<KSFunctionDeclaration>().forEach { generateScreen(it, resolver) }
        return deferred
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

    private fun generateScreen(screen: KSFunctionDeclaration, resolver: Resolver) {
        val qualifiedName = screen.qualifiedName?.asString() ?: return
        if (!emitted.add(qualifiedName)) return
        if (Modifier.PRIVATE in screen.modifiers || screen.parentDeclaration != null || screen.extensionReceiver != null || screen.typeParameters.isNotEmpty() || screen.parameters.any { it.isVararg }) {
            environment.logger.error("Domposable screens must be visible, non-generic top-level functions without varargs", screen)
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
        val viewModel = annotation.arguments.firstOrNull { it.name?.asString() == "viewModel" }?.value == true
        if (viewModel && resolver.getClassDeclarationByName(resolver.getKSNameFromString("io.github.samuolis.domposable.lifecycle.DomposableViewModelOwner")) == null) {
            environment.logger.error("viewModel = true requires the optional domposable-lifecycle-viewmodel dependency", screen)
            return
        }
        val name = screen.simpleName.asString().replaceFirstChar(Char::lowercase) + "Destination"
        val packageName = screen.packageName.asString() + ".generated"
        val routeType = route.toTypeName()
        val destination = ClassName("io.github.samuolis.domposable.screens", "DomposableDestination")
        val scope = ClassName("io.github.samuolis.domposable.screens", "DomposableEntryScope").parameterizedBy(routeType)
        val render = ParameterSpec.builder("render", LambdaTypeName.get(receiver = scope, returnType = UNIT)
            .copy(annotations = listOf(AnnotationSpec.builder(ClassName("androidx.compose.runtime", "Composable")).build())))
        if (screen.parameters.all { it.hasDefault }) {
            val call = MemberName(screen.packageName.asString(), screen.simpleName.asString())
            render.defaultValue(if (viewModel) CodeBlock.of("{ %M(this) { %M() } }", MemberName("io.github.samuolis.domposable.lifecycle", "DomposableViewModelEntry"), call) else CodeBlock.of("{ %M() }", call))
        }
        val legacy = FunSpec.builder(name).addParameter(render.build()).returns(destination.parameterizedBy(routeType))
            .addCode("return %T(%T::class, render)\n", destination, routeType)
        if (Modifier.INTERNAL in screen.modifiers) legacy.addModifiers(KModifier.INTERNAL)
        FileSpec.builder(packageName, name).addFunction(legacy.build()).build()
            .writeTo(environment.codeGenerator, Dependencies(false, screen.containingFile!!))
        val model = Screen(screen, route, viewModel)
        screens += ScreenGroupMember(model.packageName, model.entryName, Modifier.INTERNAL in screen.modifiers,
            model.dependencies.map { ParameterSpec.builder(it.name!!.asString(), it.type.toTypeName()).build() },
            model.dependencies.filterNot { it.hasDefault }.map { it.name!!.asString() }.toSet())
        val file = FileSpec.builder(packageName, model.entryName)
        file.addFunction(entryFactory(model, model.dependencies))
        if (model.dependencies.any { it.hasDefault }) file.addFunction(entryFactory(model, model.dependencies.filterNot { it.hasDefault }))
        file.build().writeTo(environment.codeGenerator, Dependencies(false, screen.containingFile!!))
    }

    private fun entryFactory(screen: Screen, parameters: List<KSValueParameter>): FunSpec {
        val destination = ClassName("io.github.samuolis.domposable.screens", "DomposableDestination")
        val routeType = screen.route.toTypeName()
        val function = FunSpec.builder(screen.entryName).returns(destination.parameterizedBy(routeType))
        if (Modifier.INTERNAL in screen.declaration.modifiers) function.addModifiers(KModifier.INTERNAL)
        parameters.forEach { function.addParameter(it.name!!.asString(), it.type.toTypeName()) }
        val content = CodeBlock.builder()
        if (screen.viewModel) content.beginControlFlow("%M(this)", MemberName("io.github.samuolis.domposable.lifecycle", "DomposableViewModelEntry"))
        content.add("%M(\n", MemberName(screen.declaration.packageName.asString(), screen.declaration.simpleName.asString()))
        content.indent()
        screen.declaration.parameters.filter { it in parameters || screen.automatic(it) }.forEach { parameter ->
            val value = if (screen.automatic(parameter)) {
                if (parameter.type.resolve() == screen.route) CodeBlock.of("this.route") else CodeBlock.of("this")
            } else CodeBlock.of("%N", parameter.name!!.asString())
            content.add("%N = %L,\n", parameter.name!!.asString(), value)
        }
        content.unindent().add(")\n")
        if (screen.viewModel) content.endControlFlow()
        function.addCode(CodeBlock.builder().add("return %T(%T::class) {\n", destination, routeType)
            .indent().add(content.build()).unindent().add("}\n").build())
        return function.build()
    }

    override fun finish() {
        // Aggregate entries within each feature package. No reflection, DI assumptions or app registry.
        for ((packageName, group) in screens.groupBy { it.packageName }) {
            val ordered = group.sortedBy { it.entryName }
            val conflicts = group.flatMap { it.dependencies }.groupBy { it.name }
                .filterValues { parameters -> parameters.map { it.type }.distinct().size > 1 }.keys
            val dependencies = linkedMapOf<Pair<String, TypeName>, ParameterSpec>()
            val names = mutableSetOf<String>()
            // Preserve shared dependency names; qualify distinct types with their entry name.
            ordered.forEach { screen ->
                screen.dependencies.forEach { parameter ->
                    val identity = parameter.name to parameter.type
                    if (identity !in dependencies) {
                        val base = if (parameter.name in conflicts) screen.entryName + parameter.name.replaceFirstChar(Char::uppercase) else parameter.name
                        var name = base
                        var suffix = 2
                        while (!names.add(name)) name = base + suffix++
                        dependencies[identity] = ParameterSpec.builder(name, parameter.type).build()
                    }
                }
            }
            val destination = ClassName("io.github.samuolis.domposable.screens", "DomposableDestination").parameterizedBy(STAR)
            fun aggregate(omitDefaults: Boolean): FunSpec {
                fun ScreenGroupMember.parameters() = this.dependencies.filter { !omitDefaults || it.name in required }
                val used = ordered.flatMap { screen -> screen.parameters().map { it.name to it.type } }.toSet()
                val function = FunSpec.builder("domposableEntries").returns(LIST.parameterizedBy(destination))
                if (group.any { it.internal }) function.addModifiers(KModifier.INTERNAL)
                dependencies.filterKeys { it in used }.values.forEach { function.addParameter(it) }
                val code = CodeBlock.builder().add("return listOf(\n").indent()
                ordered.forEach { screen ->
                    code.add("%N(\n", screen.entryName).indent()
                    screen.parameters().forEach { parameter ->
                        code.add("%N = %N,\n", parameter.name, dependencies.getValue(parameter.name to parameter.type).name)
                    }
                    code.unindent().add("),\n")
                }
                return function.addCode(code.unindent().add(")\n").build()).build()
            }
            val full = aggregate(omitDefaults = false)
            val required = aggregate(omitDefaults = true)
            val file = FileSpec.builder(packageName, "DomposableEntries").addFunction(full)
            // Kotlin default expressions stay in the original composable, inside the entry's owners.
            if (required.parameters.size < full.parameters.size) file.addFunction(required)
            file.build().writeTo(environment.codeGenerator, Dependencies.ALL_FILES)
        }
    }
}
