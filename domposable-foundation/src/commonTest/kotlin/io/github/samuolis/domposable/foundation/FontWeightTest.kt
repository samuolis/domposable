package io.github.samuolis.domposable.foundation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FontWeightTest {
    @Test
    fun variableFontWeightsDoNotRoundToHundreds() {
        assertEquals(750, DomposableFontWeight(750).weight)
        assertEquals(DomposableFontWeight(600), DomposableFontWeight.SemiBold)
        assertEquals(1, DomposableFontWeight(1).weight)
        assertEquals(1000, DomposableFontWeight(1000).weight)
        assertFailsWith<IllegalArgumentException> { DomposableFontWeight(0) }
        assertFailsWith<IllegalArgumentException> { DomposableFontWeight(1001) }
    }
}
