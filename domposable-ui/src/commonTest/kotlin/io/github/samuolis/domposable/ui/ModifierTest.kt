package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ModifierTest {
    @Test fun paddingAndBackgroundRemainOrderedAndComposable() {
        val color = DomposableColor(0xFF4059AD)
        val chain = DomposableModifier.padding(8.dp).background(color).padding(16.dp)
        assertEquals(
            listOf(
                DomposableModifierOperation.Padding(DomposableInsets(8.dp)),
                DomposableModifierOperation.Background(color),
                DomposableModifierOperation.Padding(DomposableInsets(16.dp)),
            ),
            chain.operations,
        )
        assertEquals(chain.operations, DomposableModifier.then(chain).operations)
        assertTrue(DomposableModifier.operations.isEmpty())
        assertFailsWith<IllegalArgumentException> { DomposableModifier.alpha(2f) }
    }
}
