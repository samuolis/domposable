package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableInteractionAppearance
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableTableAppearance
import io.github.samuolis.domposable.ui.DomposableDataTable
import io.github.samuolis.domposable.ui.DomposableDataTableColumn
import io.github.samuolis.domposable.ui.DomposableDataTableRow
import io.github.samuolis.domposable.ui.DomposableLink
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTheme
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.width
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeDataDisplaysTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun dataTableIncludesHeaderInCollectionAndOffsetsDataRowIndexes() {
        val activatedRows = mutableStateOf(emptyList<String>())
        val columns =
            listOf(
                DomposableDataTableColumn("order", "Order"),
                DomposableDataTableColumn("total", "Total"),
            )
        val rows =
            listOf(
                DomposableDataTableRow("A-101", accessibleLabel = "First order"),
                DomposableDataTableRow("A-102", accessibleLabel = "Second order"),
            )
        compose.setContent {
            DomposableTheme {
                DomposableDataTable(
                    columns = columns,
                    rows = rows,
                    rowHeaderColumnKey = "order",
                    onRowActivate = { key -> activatedRows.value = activatedRows.value + key },
                ) { row, column ->
                    Text(if (column.key == "order") row.key else "42")
                }
            }
        }

        compose
            .onNode(
                SemanticsMatcher("table collection has three rows and two columns") { node ->
                    node.config.getOrNull(SemanticsProperties.CollectionInfo)?.let {
                        it.rowCount == 3 && it.columnCount == 2
                    } == true
                },
            ).assertIsDisplayed()
        for (rowIndex in 0..2) {
            compose
                .onNode(
                    rowMatcher(rowIndex),
                    useUnmergedTree = true,
                ).assertExists()
        }

        val firstDataRow =
            compose.onNode(
                rowMatcher(1),
                useUnmergedTree = true,
            )
        firstDataRow.assertIsDisplayed().performTouchInput { click(center) }
        compose.runOnIdle { assertEquals(listOf("A-101"), activatedRows.value) }
    }

    @Test
    fun nonOverflowingTableLeavesScrollOwnershipWithItsVerticalParent() {
        compose.setContent {
            DomposableTheme {
                Column(Modifier.height(120.dp).verticalScroll(rememberScrollState())) {
                    DomposableDataTable(
                        columns = listOf(DomposableDataTableColumn("name", "Name")),
                        rows = List(20) { DomposableDataTableRow("Row $it") },
                        allowHorizontalOverflow = false,
                    ) { row, _ -> Text(row.key) }
                }
            }
        }

        compose
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange))
            .assertCountEquals(0)
        compose.onNodeWithText("Row 19").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun overflowingTableKeepsHorizontalScrollSemantics() {
        compose.setContent {
            DomposableTheme {
                DomposableDataTable(
                    columns = listOf(DomposableDataTableColumn("name", "Name")),
                    rows = listOf(DomposableDataTableRow("First row")),
                    modifier = DomposableModifier.width(DomposableLength(120f)),
                    allowHorizontalOverflow = true,
                ) { row, _ -> Text(row.key) }
            }
        }

        compose
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange))
            .assertCountEquals(1)
    }

    @Test
    fun compactDataTableUsesSurfaceAppearanceAndKeepsRowActionSemantics() {
        val activatedRows = mutableStateOf(emptyList<String>())
        compose.setContent {
            DomposableTheme {
                DomposableDataTable(
                    columns = listOf(DomposableDataTableColumn("name", "Name")),
                    rows = listOf(DomposableDataTableRow("one", accessibleLabel = "First row", selected = true)),
                    modifier = DomposableModifier.width(DomposableLength(272f)).testId("compact-table"),
                    sizePreset = io.github.samuolis.domposable.ui.DomposableDataTableSize.Compact,
                    onRowActivate = { key -> activatedRows.value = activatedRows.value + key },
                    appearance =
                        DomposableTableAppearance(
                            compactRowAppearance =
                                DomposableSurfaceAppearance(
                                    container = DomposableColor(0xFF112233),
                                    border = DomposableColor(0xFF445566),
                                    borderWidth = DomposableLength(1f),
                                    shape = DomposableShape.Rounded(DomposableLength(12f)),
                                ),
                            rowInteraction =
                                DomposableInteractionAppearance(
                                    focusRingColor = DomposableColor(0xFF778899),
                                    focusRingWidth = DomposableLength(4f),
                                ),
                        ),
                ) { row, _ -> Text(row.key) }
            }
        }

        val selectedRow =
            compose.onNode(
                SemanticsMatcher("selected data table row") { node ->
                    node.config.getOrNull(SemanticsProperties.Selected) == true &&
                        node.config.getOrNull(SemanticsProperties.Role) == Role.Button
                },
                useUnmergedTree = true,
            )
        selectedRow.assertIsDisplayed().assertHasClickAction().performTouchInput { click(center) }
        compose.runOnIdle { assertEquals(listOf("one"), activatedRows.value) }
    }

    @Test
    fun defaultLinkMatchesMaterialTextButtonTypographyAndTouchTarget() {
        val openedUris = mutableListOf<String>()
        val uriHandler =
            object : UriHandler {
                override fun openUri(uri: String) {
                    openedUris += uri
                }
            }
        compose.setContent {
            androidx.compose.runtime.CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                DomposableTheme {
                    Column {
                        DomposableLink(
                            label = "Reference link",
                            href = "https://example.test/reference",
                            modifier = DomposableModifier.testId("default-link"),
                        )
                        TextButton(onClick = {}, modifier = Modifier.testTag("material-reference")) {
                            Text("Reference link")
                        }
                    }
                }
            }
        }

        val link = compose.onNodeWithTag("default-link")
        val materialReference = compose.onNodeWithTag("material-reference")
        link.assertIsDisplayed().assertHasClickAction()
        materialReference.assertIsDisplayed().assertHasClickAction()

        val linkBounds = link.getUnclippedBoundsInRoot()
        val materialBounds = materialReference.getUnclippedBoundsInRoot()
        val linkHeight = (linkBounds.bottom - linkBounds.top).value
        val materialHeight = (materialBounds.bottom - materialBounds.top).value
        assertTrue(linkHeight >= materialHeight)
        assertEquals(materialHeight, linkHeight, absoluteTolerance = 1f)
        assertEquals(textFontSize(link), textFontSize(materialReference))

        link.performTouchInput { click(center) }
        compose.runOnIdle { assertEquals(listOf("https://example.test/reference"), openedUris) }
    }

    @Test
    fun suppliedLinkCallbackTakesPriorityAndTracksLatestValue() {
        val callbackCount = mutableIntStateOf(0)
        val currentCallback = mutableStateOf<() -> Unit>({ callbackCount.intValue = 1 })
        val openedUris = mutableListOf<String>()
        val uriHandler =
            object : UriHandler {
                override fun openUri(uri: String) {
                    openedUris += uri
                }
            }
        compose.setContent {
            androidx.compose.runtime.CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                DomposableTheme {
                    DomposableLink(
                        label = "Custom action",
                        href = "https://example.test/action",
                        modifier = DomposableModifier.testId("custom-action-link"),
                        onClick = currentCallback.value,
                    )
                }
            }
        }

        val link = compose.onNodeWithTag("custom-action-link")
        link.performTouchInput { click(center) }
        compose.runOnIdle { assertEquals(1, callbackCount.intValue) }
        compose.runOnIdle { currentCallback.value = { callbackCount.intValue = 2 } }
        link.performTouchInput { click(center) }
        compose.runOnIdle {
            assertEquals(2, callbackCount.intValue)
            assertTrue(openedUris.isEmpty())
        }
    }

    @Test
    fun buttonAppearanceKeepsNativeLinkActionAndMinimumHeight() {
        val callbackCount = mutableIntStateOf(0)
        val openedUris = mutableListOf<String>()
        val uriHandler =
            object : UriHandler {
                override fun openUri(uri: String) {
                    openedUris += uri
                }
            }
        compose.setContent {
            androidx.compose.runtime.CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                DomposableTheme {
                    DomposableLink(
                        label = "Continue",
                        href = "https://example.test/next",
                        modifier = DomposableModifier.testId("button-appearance-link"),
                        onClick = { callbackCount.intValue++ },
                        buttonAppearance =
                            DomposableButtonAppearance(
                                container = DomposableColor(0xFF123456),
                                minHeight = DomposableLength(56f),
                            ),
                        buttonStyle = DomposableButtonStyle.Outlined,
                    )
                }
            }
        }

        val link = compose.onNodeWithTag("button-appearance-link")
        link.assertIsDisplayed().assertHasClickAction().performTouchInput { click(center) }
        val bounds = link.getUnclippedBoundsInRoot()
        assertTrue((bounds.bottom - bounds.top).value >= 56f)
        compose.runOnIdle {
            assertEquals(1, callbackCount.intValue)
            assertTrue(openedUris.isEmpty())
        }
    }

    private fun rowMatcher(rowIndex: Int) =
        SemanticsMatcher("table row $rowIndex spans two columns") { node ->
            node.config.getOrNull(SemanticsProperties.CollectionItemInfo)?.let {
                it.rowIndex == rowIndex && it.rowSpan == 1 && it.columnIndex == 0 && it.columnSpan == 2
            } == true
        }

    private fun textFontSize(node: androidx.compose.ui.test.SemanticsNodeInteraction): TextUnit {
        val results = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            assertTrue(action(results))
        }
        return results
            .single()
            .layoutInput.style.fontSize
    }
}
