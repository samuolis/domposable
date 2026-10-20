package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.dp
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ViewportModifierTest {
    @Test
    fun viewportBoundsRejectInvalidFractionsAndReservedSpace() {
        listOf(0f, -1f, 2f, Float.NaN, Float.POSITIVE_INFINITY).forEach { fraction ->
            assertFailsWith<IllegalArgumentException> { DomposableModifier.viewportHeight(fraction) }
            assertFailsWith<IllegalArgumentException> { DomposableModifier.minViewportHeight(fraction) }
            assertFailsWith<IllegalArgumentException> { DomposableModifier.maxViewportHeight(fraction) }
        }
        assertFailsWith<IllegalArgumentException> { DomposableModifier.viewportHeight(subtract = (-1).dp) }
    }
}
