package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableGradientStop
import io.github.samuolis.domposable.foundation.DomposableLayoutAxis
import io.github.samuolis.domposable.foundation.DomposableLinearGradient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GradientHtmlInteropTest {
    @Test
    fun serializesGradientDirectionAndOrderedStops() {
        val gradient =
            DomposableLinearGradient(
                DomposableLayoutAxis.Vertical,
                listOf(
                    DomposableGradientStop(DomposableColor(0x00000000), 0f),
                    DomposableGradientStop(DomposableColor(0xFF102030), 0.5f),
                    DomposableGradientStop(DomposableColor(0xFFFFFFFF), 1f),
                ),
            )

        assertEquals(
            "linear-gradient(to bottom, rgba(0,0,0,0.0) 0.0%, rgba(16,32,48,1.0) 50.0%, rgba(255,255,255,1.0) 100.0%)",
            gradient.css(),
        )
        assertEquals(
            "linear-gradient(to right, rgba(0,0,0,0.0) 0.0%, rgba(16,32,48,1.0) 50.0%, rgba(255,255,255,1.0) 100.0%)",
            gradient.copy(axis = DomposableLayoutAxis.Horizontal).css(),
        )
    }

    @Test
    fun mapsLogicalBorderEdgesToCssLogicalProperties() {
        assertEquals("border-block-start", DomposableBorderEdge.Top.cssProperty())
        assertEquals("border-inline-end", DomposableBorderEdge.End.cssProperty())
        assertEquals("border-block-end", DomposableBorderEdge.Bottom.cssProperty())
        assertEquals("border-inline-start", DomposableBorderEdge.Start.cssProperty())
    }

    @Test
    fun pointerPassthroughRestoresPointerEventsOnInteractiveDescendants() {
        assertTrue(POINTER_PASSTHROUGH_CSS.contains("[data-domposable-pointer-passthrough]{pointer-events:none}"))
        assertTrue(
            POINTER_PASSTHROUGH_CSS.contains(
                "[data-domposable-pointer-passthrough] :is(a,button,input,select,textarea," +
                    "[role=button],[role=link],[tabindex]:not([tabindex='-1'])){pointer-events:auto}",
            ),
        )
    }
}
