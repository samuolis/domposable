package io.github.samuolis.domposable.material

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MeasuredRowsTest {
    @Test fun measuredHeightsUpdatePrefixOffsetsAndFollowKeysThroughReordering() {
        val saved = mutableMapOf<String, Double>()
        val rows = MeasuredRows(listOf("a", "b", "c"), 50.0, 5.0, saved)
        assertEquals(160.0, rows.extent)
        assertEquals(55.0, rows.offset(1))
        assertEquals(0, rows.indexAt(54.0))
        assertEquals(1, rows.indexAt(55.0))
        assertTrue(rows.update(0, 100.0))
        assertTrue(rows.update(1, 30.0))
        assertFalse(rows.update(0, 100.1))
        assertEquals(190.0, rows.extent)
        assertEquals(105.0, rows.offset(1))
        val reordered = MeasuredRows(listOf("c", "a", "b"), 50.0, 5.0, saved)
        assertEquals(100.0, reordered.height(1))
        assertEquals(30.0, reordered.height(2))
        assertEquals(160.0, reordered.offset(2))
        assertEquals(190.0, reordered.extent)
    }

    @Test fun emptyLargeOffsetsAndOverscanRemainValid() {
        val empty = MeasuredRows(emptyList(), 50.0, 5.0, mutableMapOf())
        assertEquals(0.0, empty.extent)
        assertEquals(0, empty.indexAt(100.0))
        assertTrue(empty.range(0.0, 200.0, 3).isEmpty())
        val rows = MeasuredRows(List(10000) { "$it" }, 50.0, 5.0, mutableMapOf())
        assertEquals(9999, rows.indexAt(Double.MAX_VALUE))
        assertEquals(0, rows.indexAt(-100.0))
        assertEquals(0..9999, rows.range(5000.0, 200.0, Int.MAX_VALUE))
        val end = rows.range(Double.MAX_VALUE, 200.0, 3)
        assertEquals(9999, end.last)
        assertTrue(end.count() < 12)
    }

    @Test fun invalidMeasurementAndGeometryFailBeforeCorruptingOffsets() {
        assertFailsWith<IllegalArgumentException> { MeasuredRows(listOf("a", "a"), 50.0, 0.0, mutableMapOf()) }
        assertFailsWith<IllegalArgumentException> { MeasuredRows(listOf("a"), Double.NaN, 0.0, mutableMapOf()) }
        assertFailsWith<IllegalArgumentException> { MeasuredRows(listOf("a"), 50.0, -1.0, mutableMapOf()) }
        val rows = MeasuredRows(listOf("a"), 50.0, 0.0, mutableMapOf())
        assertFailsWith<IllegalArgumentException> { rows.update(0, 0.0) }
        assertFailsWith<IllegalArgumentException> { rows.indexAt(Double.NaN) }
        assertFailsWith<IllegalArgumentException> { rows.offset(2) }
        assertFailsWith<IllegalArgumentException> { rows.range(0.0, 0.0, 3) }
        assertFailsWith<IllegalArgumentException> { rows.range(0.0, 100.0, 0) }
        assertEquals(50.0, rows.extent)
    }
}
