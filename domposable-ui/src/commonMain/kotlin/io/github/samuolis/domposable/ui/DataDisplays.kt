package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableChartAppearance
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableComponent
import io.github.samuolis.domposable.foundation.DomposableTableAppearance
import io.github.samuolis.domposable.foundation.DomposableTextAlignment

enum class DomposableChartSize {
    Thumbnail,
    Compact,
    Standard,
    Expanded,
}

enum class DomposableDataTableSize {
    Compact,
    Standard,
    Expanded,
}

enum class DomposableChartTone {
    Primary,
    Secondary,
    Tertiary,
    Success,
    Info,
    Warning,
    Error,
}

data class DomposableChartValue(
    val label: String,
    val value: Float,
    val axisIndex: Int? = null,
) {
    init {
        require(value.isFinite()) { "Chart values must be finite" }
        require(axisIndex == null || axisIndex in 0 until Int.MAX_VALUE) {
            "Chart axis indexes must be non-negative and indexable"
        }
    }
}

data class DomposableChartSeries(
    val key: String,
    val label: String,
    val values: List<DomposableChartValue>,
    val tone: DomposableChartTone = DomposableChartTone.Primary,
    val color: DomposableColor? = null,
    val currentValue: Float? = values.lastOrNull()?.value,
) {
    init {
        require(key.isNotBlank()) { "Chart series keys must not be blank" }
        require(currentValue == null || currentValue.isFinite()) { "Current chart values must be finite" }
        val indexes = values.map { it.axisIndex }
        require(indexes.all { it == null } || indexes.all { it != null }) {
            "A series must use either implicit or explicit axis indexes consistently"
        }
        val explicitIndexes = indexes.filterNotNull()
        require(explicitIndexes.size == explicitIndexes.toSet().size) {
            "Explicit chart axis indexes must be unique within a series"
        }
    }
}

private fun validateChartSeries(series: List<DomposableChartSeries>) {
    val keys = series.map { it.key }
    require(keys.size == keys.toSet().size) { "Chart series keys must be unique" }
}

@DomposableComponent("LineChart")
@Suppress("LongParameterList")
data class DomposableLineChartProps(
    val series: List<DomposableChartSeries>,
    val title: String? = null,
    val description: String? = null,
    val xAxisLabels: List<String>? = null,
    val yAxisFormatter: (Float) -> String = { it.toInt().toString() },
    val sizePreset: DomposableChartSize = DomposableChartSize.Thumbnail,
    val displayTitle: Boolean = true,
    val accessibilityDescription: String? = description,
    val onSelectedIndexChange: ((Int?) -> Unit)? = null,
    val appearance: DomposableChartAppearance? = null,
) {
    init {
        validateChartSeries(series)
    }
}

@DomposableComponent("ValueChart")
@Suppress("LongParameterList")
data class DomposableValueChartProps(
    val title: String,
    val description: String,
    val dataTableLabel: String,
    val series: List<DomposableChartSeries>,
    val formatValue: (Float) -> String,
    val sizePreset: DomposableChartSize = DomposableChartSize.Standard,
    val appearance: DomposableChartAppearance? = null,
) {
    init {
        validateChartSeries(series)
    }
}

data class DomposableDataTableColumn(
    val key: String,
    val label: String,
    val accessibleLabel: String = label,
    val alignment: DomposableTextAlignment = DomposableTextAlignment.Start,
) {
    init {
        require(key.isNotBlank()) { "Data table column keys must not be blank" }
    }
}

data class DomposableDataTableRow(
    val key: String,
    val accessibleLabel: String? = null,
    val selected: Boolean = false,
) {
    init {
        require(key.isNotBlank()) { "Data table row keys must not be blank" }
    }
}

@DomposableComponent("DataTable")
@Suppress("LongParameterList")
data class DomposableDataTableProps(
    val columns: List<DomposableDataTableColumn>,
    val rows: List<DomposableDataTableRow>,
    val sizePreset: DomposableDataTableSize = DomposableDataTableSize.Standard,
    val rowHeaderColumnKey: String? = null,
    val allowHorizontalOverflow: Boolean = true,
    val onRowActivate: ((String) -> Unit)? = null,
    val cellAction: (rowKey: String, columnKey: String) -> (() -> Unit)? = { _, _ -> null },
    val appearance: DomposableTableAppearance? = null,
    val cellContent: @Composable (row: DomposableDataTableRow, column: DomposableDataTableColumn) -> Unit,
) {
    init {
        val columnKeys = columns.map { it.key }
        val rowKeys = rows.map { it.key }
        require(columnKeys.size == columnKeys.toSet().size) { "Data table column keys must be unique" }
        require(rowKeys.size == rowKeys.toSet().size) { "Data table row keys must be unique" }
        require(rowHeaderColumnKey == null || rowHeaderColumnKey in columnKeys) {
            "The row header key must name a data table column"
        }
    }
}

/** Interactive line chart. Axis labels and accessible descriptions are supplied by the caller. */
@Composable
@Suppress("LongParameterList")
fun DomposableLineChart(
    series: List<DomposableChartSeries>,
    modifier: DomposableModifier = DomposableModifier,
    title: String? = null,
    description: String? = null,
    xAxisLabels: List<String>? = null,
    yAxisFormatter: (Float) -> String = { it.toInt().toString() },
    sizePreset: DomposableChartSize = DomposableChartSize.Thumbnail,
    displayTitle: Boolean = true,
    accessibilityDescription: String? = description,
    onSelectedIndexChange: ((Int?) -> Unit)? = null,
    appearance: DomposableChartAppearance? = null,
) = DomposableLineChart(
    DomposableLineChartProps(
        series,
        title,
        description,
        xAxisLabels,
        yAxisFormatter,
        sizePreset,
        displayTitle,
        accessibilityDescription,
        onSelectedIndexChange,
        appearance = appearance,
    ),
    modifier,
)

/** Accessible value bars accompanied by the caller's localized labels and value formatter. */
@Composable
@Suppress("LongParameterList")
fun DomposableValueChart(
    title: String,
    description: String,
    dataTableLabel: String,
    series: List<DomposableChartSeries>,
    formatValue: (Float) -> String,
    modifier: DomposableModifier = DomposableModifier,
    sizePreset: DomposableChartSize = DomposableChartSize.Standard,
    appearance: DomposableChartAppearance? = null,
) = DomposableValueChart(
    DomposableValueChartProps(
        title,
        description,
        dataTableLabel,
        series,
        formatValue,
        sizePreset,
        appearance = appearance,
    ),
    modifier,
)

/** Responsive semantic table with caller-owned cells and optional row or cell actions. */
@Composable
@Suppress("LongParameterList")
fun DomposableDataTable(
    columns: List<DomposableDataTableColumn>,
    rows: List<DomposableDataTableRow>,
    modifier: DomposableModifier = DomposableModifier,
    sizePreset: DomposableDataTableSize = DomposableDataTableSize.Standard,
    rowHeaderColumnKey: String? = null,
    allowHorizontalOverflow: Boolean = true,
    onRowActivate: ((String) -> Unit)? = null,
    cellAction: (rowKey: String, columnKey: String) -> (() -> Unit)? = { _, _ -> null },
    appearance: DomposableTableAppearance? = null,
    cellContent: @Composable (row: DomposableDataTableRow, column: DomposableDataTableColumn) -> Unit,
) = DomposableDataTable(
    DomposableDataTableProps(
        columns,
        rows,
        sizePreset,
        rowHeaderColumnKey,
        allowHorizontalOverflow,
        onRowActivate,
        cellAction,
        appearance = appearance,
        cellContent = cellContent,
    ),
    modifier,
)
