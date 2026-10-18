package io.github.samuolis.domposable.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DomposableVectorIconTest {
    @Test
    fun vectorIconDefaultsToTwentyFourUnitViewportAndNonZeroFill() {
        val icon = DomposableVectorIcon("M0 0h24v24H0z")

        assertEquals(24f, icon.viewportWidth)
        assertEquals(24f, icon.viewportHeight)
        assertEquals(false, icon.evenOdd)
    }

    @Test
    fun vectorIconRequiresPathDataAndPositiveFiniteViewport() {
        assertFailsWith<IllegalArgumentException> { DomposableVectorIcon("  ") }
        assertFailsWith<IllegalArgumentException> { DomposableVectorIcon("M0 0", viewportWidth = 0f) }
        assertFailsWith<IllegalArgumentException> {
            DomposableVectorIcon(
                "M0 0",
                viewportHeight = Float.POSITIVE_INFINITY,
            )
        }
    }
}
