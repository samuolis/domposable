package io.github.samuolis.domposable.foundation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ValuesTest {
    @Test
    fun lengthsRejectValuesThatCannotBeRenderedConsistently() {
        assertEquals(0f, 0.dp.value)
        assertEquals(12.5f, 12.5f.dp.value)
        listOf(-1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY).forEach { value ->
            assertFailsWith<IllegalArgumentException> { DomposableLength(value) }
        }
    }

    @Test
    fun colorsKeepChannelsAndAlphaWhenConvertedToCss() {
        assertEquals("rgba(0,0,0,0.0)", DomposableColor(0).css())
        assertEquals("rgba(255,255,255,1.0)", DomposableColor(0xFFFFFFFF).css())
        assertEquals("rgba(18,52,86,${128 / 255.0})", DomposableColor(0x80123456).css())
        assertFailsWith<IllegalArgumentException> { DomposableColor(-1) }
        assertFailsWith<IllegalArgumentException> { DomposableColor(0x100000000) }
    }

    @Test
    fun uniformInsetsAndMotionTokensKeepTheirContracts() {
        val insets = DomposableInsets(8.dp)
        assertEquals(listOf(8.dp, 8.dp, 8.dp, 8.dp), listOf(insets.top, insets.end, insets.bottom, insets.start))
        assertEquals(0, DomposableTokens(motionDurationMillis = 0).motionDurationMillis)
        assertFailsWith<IllegalArgumentException> { DomposableTokens(motionDurationMillis = -1) }
    }
}
