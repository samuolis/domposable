package io.github.samuolis.domposable.foundation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GradientTest {
    private val blue = DomposableColor(0xFF4059AD)
    private val mint = DomposableColor(0xFF00D7A0)

    @Test
    fun acceptsOrderedFiniteStopsOnEitherAxis() {
        val vertical =
            DomposableLinearGradient(
                DomposableLayoutAxis.Vertical,
                listOf(DomposableGradientStop(blue, 0f), DomposableGradientStop(mint, 1f)),
            )
        assertEquals(DomposableLayoutAxis.Vertical, vertical.axis)
        assertEquals(listOf(DomposableGradientStop(blue, 0f), DomposableGradientStop(mint, 1f)), vertical.stops)
        val horizontal =
            DomposableLinearGradient(
                DomposableLayoutAxis.Horizontal,
                listOf(DomposableGradientStop(blue, 0f), DomposableGradientStop(mint, 1f)),
            )
        assertEquals(DomposableLayoutAxis.Horizontal, horizontal.axis)
    }

    @Test
    fun rejectsInvalidStopListsAndPositions() {
        assertFailsWith<IllegalArgumentException> {
            DomposableLinearGradient(
                DomposableLayoutAxis.Vertical,
                emptyList(),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            DomposableLinearGradient(DomposableLayoutAxis.Vertical, listOf(DomposableGradientStop(blue, 0f)))
        }
        assertFailsWith<IllegalArgumentException> { DomposableGradientStop(blue, Float.NaN) }
        assertFailsWith<IllegalArgumentException> { DomposableGradientStop(blue, -0.01f) }
        assertFailsWith<IllegalArgumentException> { DomposableGradientStop(blue, 1.01f) }
        assertFailsWith<IllegalArgumentException> {
            DomposableLinearGradient(
                DomposableLayoutAxis.Vertical,
                listOf(DomposableGradientStop(blue, 0.8f), DomposableGradientStop(mint, 0.2f)),
            )
        }
    }
}
