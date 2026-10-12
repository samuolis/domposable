package io.github.samuolis.domposable.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DataDisplayContractsTest {
    @Test fun explicitIndexesKeepSparseSeriesAndRejectAmbiguousPoints() {
        val values = listOf(DomposableChartValue("Mon", -12f, 0), DomposableChartValue("Wed", 15f, 2))
        val series = DomposableChartSeries("orders", "Orders", values)
        assertEquals(2, series.values.last().axisIndex)
        assertEquals(15f, series.currentValue)
        assertFailsWith<IllegalArgumentException> { DomposableChartValue("bad", Float.NaN) }
        assertFailsWith<IllegalArgumentException> { DomposableChartValue("bad", Float.POSITIVE_INFINITY) }
        assertFailsWith<IllegalArgumentException> { DomposableChartValue("bad", 1f, Int.MAX_VALUE) }
        assertFailsWith<IllegalArgumentException> { DomposableChartValue("bad", 1f, -1) }
        assertFailsWith<IllegalArgumentException> {
            DomposableChartSeries("mixed", "Mixed", listOf(values.first(), DomposableChartValue("Tue", 1f)))
        }
        assertFailsWith<IllegalArgumentException> {
            DomposableChartSeries("duplicate", "Duplicate", listOf(values.first(), values.first()))
        }
    }

    @Test fun duplicateSeriesCannotSilentlyShareRenderIdentity() {
        val series = DomposableChartSeries("orders", "Orders", emptyList())
        assertFailsWith<IllegalArgumentException> { DomposableLineChartProps(listOf(series, series)) }
        assertFailsWith<IllegalArgumentException> {
            DomposableValueChartProps("Orders", "", "Values", listOf(series, series), formatValue = { it.toString() })
        }
        assertFailsWith<IllegalArgumentException> { series.copy(currentValue = Float.NaN) }
    }

    @Test fun tableHeadersAndKeysIdentifyRealCells() {
        val columns = listOf(DomposableDataTableColumn("order", "Order"))
        val rows = listOf(DomposableDataTableRow("A-101"))
        assertEquals(
            "order",
            DomposableDataTableProps(columns, rows, rowHeaderColumnKey = "order") { _, _ -> }.rowHeaderColumnKey,
        )
        assertFailsWith<IllegalArgumentException> {
            DomposableDataTableProps(columns + columns, rows) { _, _ -> }
        }
        assertFailsWith<IllegalArgumentException> {
            DomposableDataTableProps(columns, rows + rows) { _, _ -> }
        }
        assertFailsWith<IllegalArgumentException> {
            DomposableDataTableProps(columns, rows, rowHeaderColumnKey = "missing") { _, _ -> }
        }
    }

    @Test fun segmentsValidateTheirPerOptionContractAndAllowNoSelection() {
        val props = DomposableSegmentedButtonsProps(listOf("Standard", "Express"), -1, {}, "Delivery")
        assertEquals(listOf(true, true), props.optionEnabled)
        assertFailsWith<IllegalArgumentException> { props.copy(optionEnabled = listOf(true)) }
        assertFailsWith<IllegalArgumentException> { props.copy(optionTestTags = emptyList()) }
        assertFailsWith<IllegalArgumentException> { props.copy(groupName = " ") }
        assertFailsWith<IllegalArgumentException> { props.copy(selectedIndex = 2) }
    }
}
