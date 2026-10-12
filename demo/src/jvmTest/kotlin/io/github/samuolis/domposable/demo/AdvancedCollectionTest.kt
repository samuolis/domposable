package io.github.samuolis.domposable.demo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableGridWindow
import io.github.samuolis.domposable.ui.DomposableLazyColumn
import io.github.samuolis.domposable.ui.DomposableLazyGrid
import io.github.samuolis.domposable.ui.DomposableLazyRow
import io.github.samuolis.domposable.ui.DomposableListWindow
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRowWindow
import io.github.samuolis.domposable.ui.DomposableTable
import io.github.samuolis.domposable.ui.DomposableTableColumn
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test

class AdvancedCollectionTest {
    @get:Rule val compose = createComposeRule()

    @Test fun horizontalAndGridWindowsPreserveKeyedState() {
        var grid by mutableStateOf(false)
        var items by mutableStateOf(List(10000) { it })
        compose.setContent {
            DomposableMaterialTheme {
                val content: @androidx.compose.runtime.Composable (Int) -> Unit = { item ->
                    var clicks by rememberSaveable { mutableStateOf(0) }
                    DomposableButton("Item $item: $clicks", { clicks++ })
                }
                if (grid) {
                    DomposableLazyGrid(
                        items,
                        columns = 2,
                        key = { "$it" },
                        modifier = DomposableModifier.testId("items"),
                        window = DomposableGridWindow(64.dp, 240.dp),
                        itemContent = content,
                    )
                } else {
                    DomposableLazyRow(
                        items,
                        key = { "$it" },
                        modifier = DomposableModifier.testId("items"),
                        window = DomposableRowWindow(160.dp, 64.dp, 480.dp),
                        itemContent = content,
                    )
                }
            }
        }
        for (layout in listOf(false, true)) {
            compose.runOnIdle {
                grid = layout
                items = List(10000) { it }
            }
            compose.onNodeWithText("Item 0: 0").performClick()
            compose.onNodeWithTag("items").performScrollToIndex(9500)
            compose.onNodeWithText("Item 9500: 0").assertExists()
            check(compose.onAllNodesWithText("Item ", substring = true).fetchSemanticsNodes().size < 40)
            compose.runOnIdle { items = items.reversed() }
            compose.onNodeWithTag("items").performScrollToIndex(9999)
            compose.onNodeWithText("Item 0: 1").assertExists()
        }
    }

    @Test fun measuredColumnUsesNaturalItemHeight() {
        compose.setContent {
            DomposableMaterialTheme {
                DomposableLazyColumn(
                    listOf(32, 160, 48),
                    key = { "$it" },
                    window = DomposableListWindow(64.dp, 300.dp, measureRows = true),
                ) { height ->
                    DomposableText("Height $height", DomposableModifier.testId("height-$height").height(height.dp))
                }
            }
        }
        val small =
            compose
                .onNodeWithTag("height-32")
                .fetchSemanticsNode()
                .boundsInRoot.height
        val tall =
            compose
                .onNodeWithTag("height-160")
                .fetchSemanticsNode()
                .boundsInRoot.height
        check(tall > small * 4)
    }

    @Test fun tableExposesCollectionCoordinatesAndLazyKeyedCells() {
        var items by mutableStateOf(List(10000) { it })
        compose.setContent {
            DomposableMaterialTheme {
                DomposableTable(
                    "Stock",
                    listOf(DomposableTableColumn("name", "Product")),
                    items,
                    key = { "$it" },
                    window = DomposableListWindow(64.dp, 240.dp, measureRows = true),
                ) { item, _ ->
                    var clicks by rememberSaveable { mutableStateOf(0) }
                    DomposableButton("Stock $item: $clicks", { clicks++ })
                }
            }
        }
        compose.onNodeWithText("Stock").assertExists()
        compose.onNodeWithText("Product").assertExists()
        val list = compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollToIndex))
        val info = list.fetchSemanticsNode().config[SemanticsProperties.CollectionInfo]
        check(info.rowCount == 10000 && info.columnCount == 1)
        compose.onNodeWithText("Stock 0: 0").performClick()
        list.performScrollToIndex(9500)
        compose.onNodeWithText("Stock 9500: 0").assertExists()
        check(compose.onAllNodesWithText("Stock ", substring = true).fetchSemanticsNodes().size < 20)
        compose.runOnIdle { items = items.reversed() }
        list.performScrollToIndex(9999)
        compose.onNodeWithText("Stock 0: 1").assertExists()
    }
}
