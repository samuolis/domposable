package io.github.samuolis.domposable.foundation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ComponentAppearanceTest {
    @Test
    fun textAppearanceDefaultsPreserveRendererBehaviorAndOverrides() {
        val defaults = DomposableTextAppearance()
        assertNull(defaults.fontSize)
        assertNull(defaults.lineHeight)
        assertNull(defaults.fontWeight)
        assertNull(defaults.alignment)
        assertNull(defaults.decoration)
        assertNull(defaults.maxLines)
        assertEquals(DomposableTextOverflow.Clip, defaults.overflow)
        assertNull(defaults.selectable)
        assertNull(defaults.liveRegion)

        val appearance =
            DomposableTextAppearance(
                fontSize = 18f,
                lineHeight = 26f,
                fontWeight = DomposableFontWeight.SemiBold,
                alignment = DomposableTextAlignment.End,
                decoration = DomposableTextDecoration.Underline,
                maxLines = 3,
                overflow = DomposableTextOverflow.Ellipsis,
                selectable = true,
                liveRegion = DomposableLiveRegion.Polite,
            )
        assertEquals(18f, appearance.fontSize)
        assertEquals(26f, appearance.lineHeight)
        assertEquals(DomposableFontWeight.SemiBold, appearance.fontWeight)
        assertEquals(DomposableTextAlignment.End, appearance.alignment)
        assertEquals(DomposableTextDecoration.Underline, appearance.decoration)
        assertEquals(3, appearance.maxLines)
        assertEquals(DomposableTextOverflow.Ellipsis, appearance.overflow)
        assertEquals(true, appearance.selectable)
        assertEquals(DomposableLiveRegion.Polite, appearance.liveRegion)
    }

    @Test
    fun textAppearanceRejectsInvalidRenderSizesAndLineLimits() {
        listOf(-1f, 0f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY).forEach { size ->
            assertFailsWith<IllegalArgumentException> { DomposableTextAppearance(fontSize = size) }
            assertFailsWith<IllegalArgumentException> { DomposableTextAppearance(lineHeight = size) }
        }
        listOf(0, -1).forEach { lines ->
            assertFailsWith<IllegalArgumentException> { DomposableTextAppearance(maxLines = lines) }
        }
        listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY).forEach { spacing ->
            assertFailsWith<IllegalArgumentException> { DomposableTextAppearance(letterSpacing = spacing) }
        }
        assertEquals(-0.5f, DomposableTextAppearance(letterSpacing = -0.5f).letterSpacing)
    }
}
