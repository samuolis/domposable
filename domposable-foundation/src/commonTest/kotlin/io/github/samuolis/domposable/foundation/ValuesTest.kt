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
        assertEquals("#000000", DomposableColor(0xFF000000).css)
        assertEquals("#12345680", DomposableColor(0x80123456).css)
        assertEquals("#00000000", DomposableColor(0).css)
        assertFailsWith<IllegalArgumentException> { DomposableColor(-1) }
        assertFailsWith<IllegalArgumentException> { DomposableColor(0x100000000) }
    }

    @Test
    fun offsetsPreserveSignedFiniteCoordinates() {
        assertEquals(DomposableOffset(-12.5f, 4f), DomposableOffset(x = -12.5f, y = 4f))
        listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY).forEach { coordinate ->
            assertFailsWith<IllegalArgumentException> { DomposableOffset(x = coordinate) }
            assertFailsWith<IllegalArgumentException> { DomposableOffset(y = coordinate) }
        }
    }

    @Test
    fun shapeCornersKeepTheirLogicalCornerAssignments() {
        val corners =
            DomposableShape.Corners(
                topStart = 2.dp,
                topEnd = 4.dp,
                bottomEnd = 6.dp,
                bottomStart = 8.dp,
            )
        assertEquals(
            listOf(2.dp, 4.dp, 6.dp, 8.dp),
            listOf(
                corners.topStart,
                corners.topEnd,
                corners.bottomEnd,
                corners.bottomStart,
            ),
        )
        assertEquals(
            DomposableShape.Corners(),
            DomposableShape.Corners(
                topStart = 0.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp,
                bottomStart = 0.dp,
            ),
        )
    }

    @Test
    fun uniformInsetsAndMotionTokensKeepTheirContracts() {
        val insets = DomposableInsets(8.dp)
        assertEquals(listOf(8.dp, 8.dp, 8.dp, 8.dp), listOf(insets.top, insets.end, insets.bottom, insets.start))
        assertEquals(0, DomposableTokens(motionDurationMillis = 0).motionDurationMillis)
        assertFailsWith<IllegalArgumentException> { DomposableTokens(motionDurationMillis = -1) }
    }
}
