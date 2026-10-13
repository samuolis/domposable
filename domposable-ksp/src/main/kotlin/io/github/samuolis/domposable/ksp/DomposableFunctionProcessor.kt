package io.github.samuolis.domposable.ksp

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import java.io.OutputStreamWriter

/** Per-function source emitted with isolating dependencies on the annotated function's file. */
data class DomposableGeneratedSource(
    val packageName: String,
    val fileName: String,
    val source: String,
)

/**
 * Generates isolating source files for one resolved annotated function. Contract validation stays in the callback;
 * generators must not aggregate symbols across functions. Put aggregate output in a separate processor finish path.
 */
fun interface DomposableFunctionGenerator {
    fun generate(
        function: KSFunctionDeclaration,
        resolver: Resolver,
    ): List<DomposableGeneratedSource>
}

/** Discovers annotated functions, defers unresolved declarations, and writes their generated sources. */
class DomposableFunctionProcessor(
    private val environment: SymbolProcessorEnvironment,
    private val generators: Map<String, DomposableFunctionGenerator>,
) : SymbolProcessor {
    private val emitted = mutableSetOf<Pair<String, String>>()
    private val generatedFiles = mutableMapOf<Pair<String, String>, String>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val declarations =
            generators.keys
                .asSequence()
                .flatMap { annotation ->
                    resolver.getSymbolsWithAnnotation(annotation).map { annotation to it }
                }.toList()
        val (deferredDeclarations, validDeclarations) = declarations.partition { (_, symbol) -> !symbol.validate() }
        validDeclarations.forEach { (annotation, symbol) ->
            val qualifiedName = (symbol as? KSDeclaration)?.qualifiedName?.asString()
            val location = symbol.location as? FileLocation
            val signature = (symbol as? KSFunctionDeclaration)?.resolvedSignature().orEmpty()
            val identity =
                listOfNotNull(
                    location?.filePath,
                    location?.lineNumber?.toString(),
                    annotation,
                    qualifiedName ?: (symbol as? KSDeclaration)?.simpleName?.asString(),
                    signature.takeIf(String::isNotEmpty),
                ).joinToString(":")
            if (!emitted.add(annotation to identity)) return@forEach
            val function = symbol as? KSFunctionDeclaration
            if (function == null) {
                environment.logger.error("@$annotation can only annotate a function", symbol)
                return@forEach
            }
            if (qualifiedName == null) {
                environment.logger.error("@$annotation requires a named function", function)
                return@forEach
            }
            val sourceFile = function.containingFile
            if (sourceFile == null) {
                environment.logger.error("@$annotation function must be declared in a source file", function)
                return@forEach
            }
            generators.getValue(annotation).generate(function, resolver).forEach sourceLoop@{ generated ->
                val generatedName = generated.packageName to generated.fileName
                val previous = generatedFiles.putIfAbsent(generatedName, identity)
                if (previous != null) {
                    environment.logger.error(
                        "@$annotation generated ${generated.packageName}.${generated.fileName} " +
                            "for both $previous and $identity",
                        function,
                    )
                    return@sourceLoop
                }
                environment.codeGenerator
                    .createNewFile(
                        Dependencies(aggregating = false, sourceFile),
                        generated.packageName,
                        generated.fileName,
                    ).use { output -> OutputStreamWriter(output).use { it.write(generated.source) } }
            }
        }
        return deferredDeclarations.map { it.second }
    }

    private fun KSFunctionDeclaration.resolvedSignature(): String =
        buildString {
            append(simpleName.asString())
            append('(')
            extensionReceiver?.resolve()?.let { append(it.stableName()) }
            append(';')
            append(parameters.joinToString(",") { it.type.resolve().stableName() })
            append(')')
            append(returnType?.resolve()?.stableName().orEmpty())
        }

    private fun KSType.stableName(): String =
        buildString {
            append(declaration.qualifiedName?.asString() ?: declaration.simpleName.asString())
            if (arguments.isNotEmpty()) {
                append(
                    arguments.joinToString(",", "<", ">") { argument ->
                        argument.type?.resolve()?.stableName() ?: "*"
                    },
                )
            }
            append(':')
            append(nullability.name)
        }
}
