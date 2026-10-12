package io.github.samuolis.domposable.demo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableLazyColumn
import io.github.samuolis.domposable.ui.DomposableListWindow
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test

class CollectionWindowTest {
    @get:Rule val compose = createComposeRule()

    @Test fun nativeListKeepsKeyedDraftsAfterScrollingAndReorder() {
        var items by mutableStateOf(List(10000) { it })
        compose.setContent {
            DomposableMaterialTheme {
                DomposableLazyColumn(
                    items,
                    key = { "row-$it" },
                    modifier = DomposableModifier.testId("rows"),
                    window = DomposableListWindow(48.dp, 240.dp),
                ) { item ->
                    var clicks by rememberSaveable { mutableStateOf(0) }
                    DomposableButton("Row $item: $clicks", { clicks++ })
                }
            }
        }
        compose.onNodeWithText("Row 0: 0").performClick()
        compose.onNodeWithTag("rows").performScrollToIndex(9500)
        compose.onNodeWithText("Row 9500: 0").performClick()
        compose.onNodeWithText("Row 0: 1").assertDoesNotExist()
        check(compose.onAllNodesWithText("Row ", substring = true).fetchSemanticsNodes().size < 20)
        compose.runOnIdle { items = items.reversed() }
        compose.onNodeWithTag("rows").performScrollToIndex(9999)
        compose.onNodeWithText("Row 0: 1").assertExists()
    }
}
