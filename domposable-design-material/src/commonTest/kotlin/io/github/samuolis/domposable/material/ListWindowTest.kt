package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableListWindow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ListWindowTest {
    private val window = DomposableListWindow(48.dp, 240.dp, overscan = 2)

    @Test
    fun emptyStartMiddleAndEndHaveBoundedValidRanges() {
        assertTrue(listWindowRange(0, window, 0.dp, 0.0).isEmpty())
        assertEquals(0..6, listWindowRange(10000, window, 0.dp, 0.0))
        assertEquals(98..106, listWindowRange(10000, window, 0.dp, 4800.0))
        assertEquals(9993..9999, listWindowRange(10000, window, 0.dp, Double.MAX_VALUE))
        assertEquals(0..2, listWindowRange(3, window, 8.dp, 4800.0))
        assertEquals(0..6, listWindowRange(10000, window, 0.dp, -100.0))
    }

    @Test
    fun gapsDoNotAddAnExtraTrailingRowOrOverflowTheRange() {
        assertEquals(160.0, listExtent(3, window, 8.dp))
        assertEquals(0.0, listExtent(0, window, 8.dp))
        val large = DomposableListWindow(48.dp, 240.dp, overscan = Int.MAX_VALUE)
        assertEquals(0..2, listWindowRange(3, large, 0.dp, 0.0))
    }

    @Test
    fun invalidGeometryIsRejectedInsteadOfProducingInvalidCss() {
        assertFailsWith<IllegalArgumentException> { DomposableListWindow(0.dp, 240.dp) }
        assertFailsWith<IllegalArgumentException> { DomposableListWindow(48.dp, Float.NaN.dp) }
        assertFailsWith<IllegalArgumentException> { DomposableListWindow(Float.POSITIVE_INFINITY.dp, 240.dp) }
        assertFailsWith<IllegalArgumentException> { DomposableListWindow(48.dp, 240.dp, -1) }
        assertFailsWith<IllegalArgumentException> { DomposableListWindow(48.dp, 240.dp, 0) }
        assertFailsWith<IllegalArgumentException> { listExtent(-1, window, 0.dp) }
        assertFailsWith<IllegalArgumentException> { listExtent(10, window, (-1).dp) }
        assertFailsWith<IllegalArgumentException> { listWindowRange(10, window, 0.dp, Double.NaN) }
    }
}
