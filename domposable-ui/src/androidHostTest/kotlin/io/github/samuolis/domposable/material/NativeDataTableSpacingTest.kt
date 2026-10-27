package io.github.samuolis.domposable.material

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableTableAppearance
import io.github.samuolis.domposable.ui.DomposableDataTable
import io.github.samuolis.domposable.ui.DomposableDataTableColumn
import io.github.samuolis.domposable.ui.DomposableDataTableRow
import io.github.samuolis.domposable.ui.DomposableDataTableSize
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTheme
import io.github.samuolis.domposable.ui.width
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeDataTableSpacingTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun dataTableAppearanceSpacingOverridesPresetGapsWithoutOuterSpacing() {
        val zeroPadding = DomposableInsets(DomposableLength(0f))
        val appearance = mutableStateOf(DomposableTableAppearance(cellPadding = zeroPadding))
        compose.setContent {
            DomposableTheme {
                DomposableDataTable(
                    columns =
                        listOf(
                            DomposableDataTableColumn("first", "First"),
                            DomposableDataTableColumn("second", "Second"),
                        ),
                    rows = listOf(DomposableDataTableRow("one"), DomposableDataTableRow("two")),
                    modifier = DomposableModifier.width(DomposableLength(200f)),
                    allowHorizontalOverflow = false,
                    appearance = appearance.value,
                ) { row, column ->
                    Text(
                        row.key,
                        Modifier.testTag("${row.key}-${column.key}"),
                    )
                }
            }
        }

        fun bounds(tag: String) = compose.onNodeWithTag(tag).getUnclippedBoundsInRoot()

        val defaultFirstColumn = bounds("one-first")
        val defaultSecondColumn = bounds("one-second")
        val defaultNextRow = bounds("two-first")
        assertEquals(100f, (defaultSecondColumn.left - defaultFirstColumn.left).value, 1f)
        assertEquals(8f, (defaultNextRow.top - defaultFirstColumn.bottom).value, 1f)

        compose.runOnIdle {
            appearance.value =
                DomposableTableAppearance(
                    cellPadding = zeroPadding,
                    rowSpacing = DomposableLength(0f),
                    columnSpacing = DomposableLength(8f),
                )
        }
        val spacedFirstColumn = bounds("one-first")
        val spacedSecondColumn = bounds("one-second")
        val spacedNextRow = bounds("two-first")
        assertEquals(104f, (spacedSecondColumn.left - spacedFirstColumn.left).value, 1f)
        assertEquals(0f, (spacedNextRow.top - spacedFirstColumn.bottom).value, 1f)
    }

    @Test
    fun dataTableColumnSpacingContributesToHorizontalOverflowWidth() {
        compose.setContent {
            DomposableTheme {
                DomposableDataTable(
                    columns =
                        listOf(
                            DomposableDataTableColumn("first", "First"),
                            DomposableDataTableColumn("second", "Second"),
                        ),
                    rows = listOf(DomposableDataTableRow("one")),
                    modifier = DomposableModifier.width(DomposableLength(200f)),
                    allowHorizontalOverflow = true,
                    appearance =
                        DomposableTableAppearance(
                            cellPadding = DomposableInsets(DomposableLength(0f)),
                            columnSpacing = DomposableLength(8f),
                        ),
                ) { row, column ->
                    Text(row.key, Modifier.testTag("${row.key}-${column.key}"))
                }
            }
        }

        val firstColumn = compose.onNodeWithTag("one-first").getUnclippedBoundsInRoot()
        val secondColumn = compose.onNodeWithTag("one-second").getUnclippedBoundsInRoot()
        assertEquals(168f, (secondColumn.left - firstColumn.left).value, 1f)
    }

    @Test
    fun compactTableRowSpacingControlsFieldGapsAndKeepsSurfaceDefaults() {
        val appearance =
            mutableStateOf(
                DomposableTableAppearance(cellPadding = DomposableInsets(DomposableLength(0f))),
            )
        compose.setContent {
            DomposableTheme {
                DomposableDataTable(
                    columns =
                        listOf(
                            DomposableDataTableColumn("first", "First"),
                            DomposableDataTableColumn("second", "Second"),
                        ),
                    rows = listOf(DomposableDataTableRow("one")),
                    modifier = DomposableModifier.width(DomposableLength(240f)),
                    sizePreset = DomposableDataTableSize.Compact,
                    allowHorizontalOverflow = false,
                    appearance = appearance.value,
                ) { _, column -> Text(column.label, Modifier.testTag(column.key)) }
            }
        }

        fun fieldGap(): Float {
            val first = compose.onNodeWithTag("first").getUnclippedBoundsInRoot()
            val second = compose.onNodeWithTag("second").getUnclippedBoundsInRoot()
            return (second.top - first.bottom).value
        }

        assertEquals(4f, fieldGap(), 1f)
        compose.runOnIdle { appearance.value = appearance.value.copy(rowSpacing = DomposableLength(12f)) }
        assertEquals(12f, fieldGap(), 1f)

        compose.runOnIdle {
            appearance.value =
                DomposableTableAppearance(
                    cellPadding = DomposableInsets(DomposableLength(0f)),
                    compactRowAppearance = DomposableSurfaceAppearance(),
                )
        }
        assertEquals(0f, fieldGap(), 1f)
        compose.runOnIdle { appearance.value = appearance.value.copy(rowSpacing = DomposableLength(12f)) }
        assertEquals(12f, fieldGap(), 1f)
    }
}
