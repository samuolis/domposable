package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableGridWindow
import io.github.samuolis.domposable.ui.DomposableLazyColumn
import io.github.samuolis.domposable.ui.DomposableLazyGrid
import io.github.samuolis.domposable.ui.DomposableLazyRow
import io.github.samuolis.domposable.ui.DomposableListWindow
import io.github.samuolis.domposable.ui.DomposableMeasure
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRowWindow
import io.github.samuolis.domposable.ui.DomposableSelect
import io.github.samuolis.domposable.ui.DomposableSelectOption
import io.github.samuolis.domposable.ui.DomposableSwitch
import io.github.samuolis.domposable.ui.DomposableTable
import io.github.samuolis.domposable.ui.DomposableTableColumn
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.verticalScroll

private val collectionModes = listOf("Measured column", "Horizontal row", "Responsive grid", "Semantic table")
private val inventoryColumns =
    listOf(DomposableTableColumn("item", "Item"), DomposableTableColumn("note", "Private draft"))

/** Natural row heights, horizontal windows, adaptive columns and real HTML tables share item keys. */
@Composable
@DomposableScreen(DemoRoute.AdvancedCollections::class)
internal fun AdvancedCollections(initialMode: String = collectionModes.first()) {
    var mode by rememberSaveable { mutableStateOf(initialMode) }
    var count by rememberSaveable { mutableStateOf("10000") }
    var reversed by rememberSaveable { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val items = remember(count, reversed) { List(count.toInt()) { if (reversed) count.toInt() - it - 1 else it } }
    DomposableColumn(DomposableModifier.fillMaxWidth().padding(16.dp).verticalScroll(), gap = 12.dp) {
        DomposableText("Collection recipes", headingLevel = 1)
        DomposableText("10,000 keyed items. Drafts follow their item when rows leave the viewport or change order.")
        DomposableSelect(
            mode,
            { mode = it },
            "Collection layout",
            collectionModes.map { DomposableSelectOption(it, it) },
        )
        DomposableSelect(count, {
            count = it
        }, "Recipe item count", listOf("5", "10000").map { DomposableSelectOption(it, it) })
        DomposableButton("Reverse recipe items", { reversed = !reversed })
        DomposableSwitch(expanded, { expanded = it }, "Expanded item descriptions")
        key(mode) {
            when (mode) {
                "Measured column" -> {
                    DomposableLazyColumn(
                        items,
                        key = { "item-$it" },
                        modifier = DomposableModifier.testId("measured-items").fillMaxWidth(),
                        gap = 8.dp,
                        window = DomposableListWindow(144.dp, 360.dp, measureRows = true),
                    ) { CollectionItem(it, expanded) }
                }

                "Horizontal row" -> {
                    DomposableLazyRow(
                        items,
                        key = { "item-$it" },
                        modifier = DomposableModifier.testId("horizontal-items"),
                        gap = 8.dp,
                        window = DomposableRowWindow(240.dp, 240.dp, 720.dp),
                    ) { CollectionItem(it, false) }
                }

                "Responsive grid" -> {
                    DomposableMeasure(DomposableModifier.fillMaxWidth()) { size ->
                        DomposableLazyGrid(
                            items,
                            columns = if (size.width >= 700) 4 else 2,
                            key = { "item-$it" },
                            modifier = DomposableModifier.testId("grid-items").fillMaxWidth(),
                            gap = 8.dp,
                            window = DomposableGridWindow(240.dp, 360.dp),
                        ) { CollectionItem(it, false) }
                    }
                }

                "Semantic table" -> {
                    DomposableTable(
                        "Inventory drafts",
                        inventoryColumns,
                        items,
                        key = { "item-$it" },
                        modifier = DomposableModifier.testId("inventory-table").fillMaxWidth(),
                        window = DomposableListWindow(128.dp, 360.dp, measureRows = true),
                    ) { item, column ->
                        when (column.key) {
                            "item" -> DomposableText("Item ${item + 1}")
                            "note" -> CollectionDraft(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionItem(
    item: Int,
    expanded: Boolean,
) {
    DomposableColumn(DomposableModifier.fillMaxWidth().padding(8.dp), gap = 8.dp) {
        DomposableText("Item ${item + 1}")
        if (expanded || item % 3 == 0) {
            DomposableText("A description with natural height. ".repeat(if (expanded) 10 else 2))
        }
        CollectionDraft(item)
    }
}

@Composable
private fun CollectionDraft(item: Int) {
    var note by rememberSaveable { mutableStateOf("") }
    DomposableTextField(note, { note = it }, "Item ${item + 1} draft", DomposableModifier.fillMaxWidth())
}
