package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/** A typed identity for a design-system extension. Keep the key stable across recomposition. */
class DomposableComponentKey<Props : Any>(
    val name: String,
) {
    init {
        require(name.isNotBlank())
    }
}

/** Product renderers receive the same portable modifier as the built-in components. */
fun interface DomposableComponentRenderer<Props : Any> {
    @Composable
    fun Render(
        props: Props,
        modifier: DomposableModifier,
        content: @Composable () -> Unit,
    )
}

/** Immutable, typed renderer bindings. Keys are identities rather than global string names. */
class DomposableComponentProvider internal constructor(
    private val renderers: Map<DomposableComponentKey<*>, DomposableComponentRenderer<*>>,
) {
    @Suppress("UNCHECKED_CAST")
    internal fun <Props : Any> renderer(key: DomposableComponentKey<Props>): DomposableComponentRenderer<Props>? =
        renderers[key] as? DomposableComponentRenderer<Props>
}

class DomposableComponentProviderBuilder {
    private val renderers = mutableMapOf<DomposableComponentKey<*>, DomposableComponentRenderer<*>>()

    fun <Props : Any> register(
        key: DomposableComponentKey<Props>,
        renderer: DomposableComponentRenderer<Props>,
    ) {
        require(key !in renderers) { "Duplicate component renderer: ${key.name}" }
        renderers[key] = renderer
    }

    internal fun build(): DomposableComponentProvider = DomposableComponentProvider(renderers.toMap())
}

fun domposableComponentProvider(
    configure: DomposableComponentProviderBuilder.() -> Unit,
): DomposableComponentProvider = DomposableComponentProviderBuilder().apply(configure).build()

private val LocalDomposableComponentProviders =
    staticCompositionLocalOf<List<DomposableComponentProvider>> {
        emptyList()
    }

/** Nested providers replace individual keys and inherit the rest of their parent's bindings. */
@Composable
fun ProvideDomposableComponentProvider(
    provider: DomposableComponentProvider,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalDomposableComponentProviders provides (listOf(provider) + LocalDomposableComponentProviders.current),
        content = content,
    )
}

/** Shared forwarding entry point used by generated or manually declared product components. */
@Composable
fun <Props : Any> DomposableComponent(
    key: DomposableComponentKey<Props>,
    props: Props,
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable () -> Unit = {},
) {
    val renderer =
        LocalDomposableComponentProviders.current.firstNotNullOfOrNull { it.renderer(key) }
            ?: error("No renderer installed for component ${key.name}; provide a DomposableComponentProvider")
    renderer.Render(props, modifier, content)
}
