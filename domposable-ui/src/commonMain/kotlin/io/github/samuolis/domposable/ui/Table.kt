package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableComponent

data class DomposableTableColumn(
    val key: String,
    val label: String,
) {
    init {
        require(key.isNotBlank() && label.isNotBlank())
    }
}

data class DomposableTableRow(
    val key: String,
    val cells: List<@Composable () -> Unit>,
) {
    init {
        require(key.isNotBlank())
    }
}

/** A semantic HTML table and an accessible native row/column collection. */
@DomposableComponent("Table")
data class DomposableTableProps(
    val caption: String,
    val columns: List<DomposableTableColumn>,
    val rows: List<DomposableTableRow>,
    val window: DomposableListWindow? = null,
) {
    init {
        require(caption.isNotBlank())
        require(columns.isNotEmpty() && columns.map { it.key }.distinct().size == columns.size)
        require(rows.map { it.key }.distinct().size == rows.size)
        require(rows.all { it.cells.size == columns.size }) { "Every table row must supply a cell for each column" }
    }
}

@Composable
fun <T> DomposableTable(
    caption: String,
    columns: List<DomposableTableColumn>,
    items: List<T>,
    key: (T) -> String,
    modifier: DomposableModifier = DomposableModifier,
    window: DomposableListWindow? = null,
    cellContent: @Composable (T, DomposableTableColumn) -> Unit,
) {
    val rows =
        items.map { item ->
            val cells: List<@Composable () -> Unit> = columns.map { column -> { cellContent(item, column) } }
            DomposableTableRow(key(item), cells)
        }
    DomposableTable(DomposableTableProps(caption, columns, rows, window), modifier)
}
