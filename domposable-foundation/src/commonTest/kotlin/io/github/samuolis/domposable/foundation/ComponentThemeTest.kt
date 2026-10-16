package io.github.samuolis.domposable.foundation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ComponentThemeTest {
    @Test
    fun rejectsInvalidInteractionScale() {
        listOf(0f, -1f, 1.1f, Float.NaN, Float.POSITIVE_INFINITY).forEach {
            assertFailsWith<IllegalArgumentException> { DomposableButtonAppearance(pressedScale = it) }
        }
    }

    @Test
    fun themeCopyKeepsUnchangedComponentDefaults() {
        val themed =
            DomposableComponentStyles(
                button = DomposableButtonAppearance(shape = DomposableShape.Circle, minHeight = 48.dp),
            )
        val local = themed.button.copy(content = DomposableColor(0xFFFFFFFF))
        assertEquals(themed.button.shape, local.shape)
        assertEquals(48.dp, local.minHeight)
        assertEquals(null, themed.button.content)
        assertEquals(themed.textField, themed.copy(button = local).textField)
    }
}
