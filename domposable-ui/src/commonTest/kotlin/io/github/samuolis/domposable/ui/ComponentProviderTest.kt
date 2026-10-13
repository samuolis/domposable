package io.github.samuolis.domposable.ui

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class ComponentProviderTest {
    private data class Card(
        val title: String,
    )

    @Test
    fun keysWithTheSameDisplayNameRemainIndependent() {
        val product = DomposableComponentKey<Card>("Card")
        val account = DomposableComponentKey<Card>("Card")
        val renderer = DomposableComponentRenderer<Card> { _, _, _ -> }
        val provider = domposableComponentProvider { register(product, renderer) }
        assertSame(renderer, provider.renderer(product))
        assertNull(provider.renderer(account))
    }

    @Test
    fun duplicateBindingsAreRejectedBeforeComposition() {
        val key = DomposableComponentKey<Card>("Card")
        val renderer = DomposableComponentRenderer<Card> { _, _, _ -> }
        assertFailsWith<IllegalArgumentException> {
            domposableComponentProvider {
                register(key, renderer)
                register(key, renderer)
            }
        }
    }

    @Test
    fun buildingAProviderDoesNotShareItsMutableBuilderMap() {
        val first = DomposableComponentKey<Card>("First")
        val second = DomposableComponentKey<Card>("Second")
        val renderer = DomposableComponentRenderer<Card> { _, _, _ -> }
        val builder = DomposableComponentProviderBuilder()
        builder.register(first, renderer)
        val provider = builder.build()
        builder.register(second, renderer)
        assertNotNull(provider.renderer(first))
        assertNull(provider.renderer(second))
    }
}
