package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableLazyColumn
import io.github.samuolis.domposable.ui.DomposableLazyGrid
import io.github.samuolis.domposable.ui.DomposableLazyRow
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.height
import org.junit.Rule
import org.junit.Test

class KeyedComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun columnKeepsItemStateAcrossReordering() = verifyOrder(CollectionLayout.Column)

    @Test fun rowKeepsItemStateAcrossReordering() = verifyOrder(CollectionLayout.Row)

    @Test fun gridKeepsItemStateAcrossReordering() = verifyOrder(CollectionLayout.Grid)

    private fun verifyOrder(layout: CollectionLayout) {
        var items by mutableStateOf(listOf("one", "two"))
        compose.setContent {
            DomposableMaterialTheme {
                val content: @Composable (String) -> Unit = { id ->
                    var clicks by remember { mutableIntStateOf(0) }
                    DomposableButton("$id: $clicks", { clicks++ })
                }
                val modifier = DomposableModifier.height(200.dp)
                when (layout) {
                    CollectionLayout.Column -> {
                        DomposableLazyColumn(
                            items,
                            key = { it },
                            modifier,
                            itemContent = content,
                        )
                    }

                    CollectionLayout.Row -> {
                        DomposableLazyRow(items, key = { it }, modifier, itemContent = content)
                    }

                    CollectionLayout.Grid -> {
                        DomposableLazyGrid(items, key = { it }, modifier, itemContent = content)
                    }
                }
            }
        }
        compose.onNodeWithText("one: 0").performClick()
        compose.runOnIdle { items = items.reversed() }
        compose.onNodeWithText("one: 1").assertExists()
        compose.onNodeWithText("two: 0").assertExists()
    }

    private enum class CollectionLayout { Column, Row, Grid }
}
