package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CollectionContractsTest {
    @Test fun validWindowsDescribeTheirGeometryWithoutRequiringAProvider() {
        assertTrue(DomposableListWindow(48.dp, 240.dp, measureRows = true).measureRows)
        val row = DomposableRowWindow(240.dp, 88.dp, 960.dp)
        assertEquals(3, row.overscan)
        assertEquals(240f, row.itemWidth.value)
        val grid = DomposableGridWindow(88.dp, 352.dp)
        assertEquals(3, grid.overscanRows)
        assertEquals(352f, grid.viewportHeight.value)
        assertFailsWith<IllegalArgumentException> { DomposableRowWindow(0.dp, 88.dp, 960.dp) }
        assertFailsWith<IllegalArgumentException> { DomposableRowWindow(240.dp, Float.NaN.dp, 960.dp) }
        assertFailsWith<IllegalArgumentException> { DomposableRowWindow(240.dp, 88.dp, 960.dp, 0) }
        assertFailsWith<IllegalArgumentException> { DomposableGridWindow(0.dp, 352.dp) }
        assertFailsWith<IllegalArgumentException> { DomposableGridWindow(88.dp, Float.POSITIVE_INFINITY.dp) }
        assertFailsWith<IllegalArgumentException> { DomposableGridWindow(88.dp, 352.dp, 0) }
    }

    @Test fun tablesRequireStableDistinctKeysAndAnExactCellForEachColumn() {
        val columns = listOf(DomposableTableColumn("name", "Name"), DomposableTableColumn("note", "Note"))
        val rows = listOf(DomposableTableRow("one", listOf({}, {})))
        val table = DomposableTableProps("Records", columns, rows)
        assertEquals("one", table.rows.single().key)
        assertEquals(2, table.columns.size)
        assertEquals(0, DomposableTableProps("Empty", columns, emptyList()).rows.size)
        assertFailsWith<IllegalArgumentException> { DomposableTableColumn("", "Name") }
        assertFailsWith<IllegalArgumentException> { DomposableTableColumn("name", "") }
        assertFailsWith<IllegalArgumentException> { DomposableTableRow("", emptyList()) }
        assertFailsWith<IllegalArgumentException> { DomposableTableProps("", columns, rows) }
        assertFailsWith<IllegalArgumentException> { DomposableTableProps("Records", emptyList(), emptyList()) }
        assertFailsWith<IllegalArgumentException> { DomposableTableProps("Records", columns + columns.first(), rows) }
        assertFailsWith<IllegalArgumentException> { DomposableTableProps("Records", columns, rows + rows.first()) }
        assertFailsWith<IllegalArgumentException> {
            DomposableTableProps("Records", columns, listOf(DomposableTableRow("one", listOf({}))))
        }
    }
}
