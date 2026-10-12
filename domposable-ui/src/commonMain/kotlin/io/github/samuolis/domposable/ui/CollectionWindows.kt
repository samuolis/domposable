package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.DomposableLength

/** Fixed cells in a horizontally scrolling collection. */
data class DomposableRowWindow(
    val itemWidth: DomposableLength,
    val itemHeight: DomposableLength,
    val viewportWidth: DomposableLength,
    val overscan: Int = 3,
) {
    init {
        listOf(itemWidth, itemHeight, viewportWidth).forEach { require(it.value.isFinite() && it.value > 0f) }
        require(overscan > 0)
    }
}

/** Fixed-height rows; the collection's [DomposableLazyGridProps.columns] controls responsive width. */
data class DomposableGridWindow(
    val rowHeight: DomposableLength,
    val viewportHeight: DomposableLength,
    val overscanRows: Int = 3,
) {
    init {
        listOf(rowHeight, viewportHeight).forEach { require(it.value.isFinite() && it.value > 0f) }
        require(overscanRows > 0)
    }
}
