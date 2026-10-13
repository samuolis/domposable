package io.github.samuolis.domposable.adaptive

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveTest {
    @Test fun boundariesAndInheritedValues() {
        val thresholds = DomposableBreakpoints()
        assertEquals(DomposableWidthClass.Compact, thresholds.classify(599f))
        assertEquals(DomposableWidthClass.Medium, thresholds.classify(600f))
        assertEquals(DomposableWidthClass.Expanded, thresholds.classify(840f))
        assertEquals(DomposableWidthClass.Large, thresholds.classify(1200f))
        assertEquals("roomy", DomposableResponsive("small", expanded = "roomy").resolve(DomposableWidthClass.Large))
        assertFailsWith<IllegalArgumentException> { DomposableBreakpoints(840f, 600f) }
        assertFailsWith<IllegalArgumentException> { thresholds.classify(Float.NaN) }
        val recipe = DomposablePaneRecipe()
        assertFalse(recipe.isDualPane(thresholds.classify(839f)))
        assertTrue(recipe.isDualPane(thresholds.classify(840f)))
        assertTrue(recipe.isDualPane(thresholds.classify(1200f)))
    }
}
