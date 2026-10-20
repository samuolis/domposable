package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableInteractionAppearance
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableTableAppearance
import io.github.samuolis.domposable.ui.DomposableDataTable
import io.github.samuolis.domposable.ui.DomposableDataTableColumn
import io.github.samuolis.domposable.ui.DomposableDataTableRow
import io.github.samuolis.domposable.ui.DomposableDataTableSize
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.testId
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val SPACED_TABLE_WIDTH = 328.0
private const val TABLE_COLUMN_GAP = 8.0
private const val TABLE_ROW_GAP = 6.0
private const val GEOMETRY_TOLERANCE = 0.5

class HtmlDataTableAppearanceTest {
    @Test
    fun compactRowsUseSurfaceAndInteractionAppearanceAndKeepKeyboardActivation() {
        val root = document.createElement("div") as HTMLElement
        root.style.width = "300px"
        document.body!!.appendChild(root)
        var activated = ""
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    CompactInteractionTable { activated = it }
                }
            }
        try {
            val table = root.querySelector("[data-testid=table] table") as HTMLElement
            val row = root.querySelector("tr[data-row-key=one]") as HTMLElement
            val container = root.querySelector(".d-data-table-container") as HTMLElement
            assertTrue(table.classList.contains("d-data-table"))
            assertFalse(container.classList.contains("d-data-table-spaced"))
            assertTrue(container.classList.contains("d-data-table-compact-active"))
            assertEquals("TR", row.tagName)
            assertEquals(null, row.getAttribute("role"))
            assertEquals("0", row.getAttribute("tabindex"))
            assertEquals("true", row.getAttribute("aria-selected"))
            assertEquals("First row", row.getAttribute("aria-label"))
            assertEquals("row", row.querySelector("th")?.getAttribute("scope"))
            assertEquals("96px", row.style.getPropertyValue("--d-data-compact-label-width"))
            assertCompactRowStyle(row)
            val labelValueCell = row.querySelector("th[scope=row]") as HTMLElement
            assertEquals("8px", window.getComputedStyle(labelValueCell).columnGap)
            assertEquals("rgba(17,34,51,1)", row.style.getPropertyValue("--d-data-compact-container"))
            assertEquals("rgba(68,85,102,1)", row.style.getPropertyValue("--d-data-compact-border"))
            assertEquals("rgba(119,136,153,1)", row.style.getPropertyValue("--d-data-focus-ring-color"))
            row.dispatchEvent(js("new KeyboardEvent('keydown', {key:'Enter', bubbles:true})") as Event)
            assertEquals("one", activated)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun explicitTrackSpacingUsesAccessibleGridRowsAndKeepsContinuousSelectionStyling() {
        val root = document.createElement("div") as HTMLElement
        root.style.width = "300px"
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableDataTable(
                        columns =
                            listOf(
                                DomposableDataTableColumn("name", "Name"),
                                DomposableDataTableColumn("score", "Score"),
                            ),
                        rows =
                            listOf(
                                DomposableDataTableRow("one", selected = true),
                                DomposableDataTableRow("two"),
                            ),
                        rowHeaderColumnKey = "name",
                        modifier = DomposableModifier.testId("spaced-table"),
                        appearance =
                            DomposableTableAppearance(
                                headerContainer = DomposableColor(0xFF010203),
                                selectedContainer = DomposableColor(0xFF123456),
                                border = DomposableColor(0xFFAABBCC),
                                cellPadding = DomposableInsets(DomposableLength(0f)),
                                rowSpacing = DomposableLength(6f),
                                columnSpacing = DomposableLength(8f),
                            ),
                    ) { _, _ ->
                        Text("Cell")
                    }
                }
            }
        try {
            val table = root.querySelector("[data-testid=spaced-table] table") as HTMLElement
            val headerRow = root.querySelector("thead tr") as HTMLElement
            val firstDataRow = root.querySelector("tbody tr[data-row-key=one]") as HTMLElement
            val headerFirstCell = root.querySelector("thead th[scope=col]") as HTMLElement
            assertExplicitTableGeometry(table)
            assertExplicitTableAppearance(headerRow, firstDataRow)
            assertExplicitTableSemantics(root, table, headerRow, firstDataRow, headerFirstCell)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun standardTableWithoutExplicitSpacingKeepsCollapsedTableLayout() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableDataTable(
                        columns = listOf(DomposableDataTableColumn("name", "Name")),
                        rows = listOf(DomposableDataTableRow("one")),
                    ) { _, _ ->
                        Text("Cell")
                    }
                }
            }
        try {
            val container = root.querySelector(".d-data-table-container") as HTMLElement
            val table = root.querySelector("table") as HTMLElement
            val row = root.querySelector("tbody tr") as HTMLElement
            assertFalse(container.classList.contains("d-data-table-spaced"))
            assertEquals(
                "table",
                window.getComputedStyle(table).display,
            )
            assertEquals(
                "collapse",
                window.getComputedStyle(table).borderCollapse,
            )
            assertEquals(null, row.getAttribute("role"))
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun compactExplicitSpacingUsesCardAndLabelGapsWithoutOuterMargins() {
        val root = document.createElement("div") as HTMLElement
        root.style.width = "300px"
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableDataTable(
                        columns =
                            listOf(
                                DomposableDataTableColumn("name", "Name"),
                                DomposableDataTableColumn("score", "Score"),
                            ),
                        rows = listOf(DomposableDataTableRow("one"), DomposableDataTableRow("two")),
                        sizePreset = DomposableDataTableSize.Compact,
                        rowHeaderColumnKey = "name",
                        appearance =
                            DomposableTableAppearance(
                                rowSpacing = DomposableLength(12f),
                                columnSpacing = DomposableLength(4f),
                            ),
                    ) { row, _ ->
                        Text(row.key)
                    }
                }
            }
        try {
            val container = root.querySelector(".d-data-table-container") as HTMLElement
            val table = root.querySelector("table") as HTMLElement
            val rows = root.querySelectorAll("tbody tr")
            val firstRow = rows.item(0) as HTMLElement
            val secondRow = rows.item(1) as HTMLElement
            val labelValueCell = firstRow.querySelector("th[scope=row]") as HTMLElement
            val nextFieldCell = firstRow.querySelector("td") as HTMLElement
            assertTrue(container.classList.contains("d-data-table-compact-active"))
            assertEquals(
                0.0,
                firstRow.getBoundingClientRect().top - table.getBoundingClientRect().top,
                0.5,
            )
            assertEquals("0px", window.getComputedStyle(firstRow).marginTop)
            assertEquals(12.0, secondRow.getBoundingClientRect().top - firstRow.getBoundingClientRect().bottom, 0.5)
            assertEquals(
                12.0,
                nextFieldCell.getBoundingClientRect().top - labelValueCell.getBoundingClientRect().bottom,
                0.5,
            )
            assertEquals("4px", window.getComputedStyle(labelValueCell).columnGap)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}

@Composable
private fun CompactInteractionTable(onRowActivate: (String) -> Unit) {
    DomposableDataTable(
        columns = listOf(DomposableDataTableColumn("name", "Name")),
        rows = listOf(DomposableDataTableRow("one", accessibleLabel = "First row", selected = true)),
        modifier = DomposableModifier.testId("table"),
        sizePreset = DomposableDataTableSize.Compact,
        rowHeaderColumnKey = "name",
        onRowActivate = onRowActivate,
        appearance =
            DomposableTableAppearance(
                selectedContainer = DomposableColor(0xFF102030),
                selectedBorder = DomposableColor(0xFF405060),
                selectedBorderWidth = DomposableLength(2f),
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
        cellContent = { _, _ -> Text("Example") },
    )
}

private fun assertCompactRowStyle(row: HTMLElement) {
    assertEquals("8px", window.getComputedStyle(row).marginTop)
}

private fun assertExplicitTableGeometry(table: HTMLElement) {
    val headerRow = table.querySelector("thead tr") as HTMLElement
    val firstDataRow = table.querySelector("tbody tr[data-row-key=one]") as HTMLElement
    val secondDataRow = table.querySelector("tbody tr[data-row-key=two]") as HTMLElement
    val headerFirstCell = table.querySelector("thead th[scope=col]") as HTMLElement
    val headerLastCell = table.querySelector("thead th:last-child") as HTMLElement
    val headerNextCell = table.querySelector("thead th:nth-child(2)") as HTMLElement
    val tableBounds = table.getBoundingClientRect()
    val firstCellBounds = headerFirstCell.getBoundingClientRect()
    val lastCellBounds = headerLastCell.getBoundingClientRect()
    assertEquals(SPACED_TABLE_WIDTH, tableBounds.width, GEOMETRY_TOLERANCE)
    assertEquals(tableBounds.left, firstCellBounds.left, GEOMETRY_TOLERANCE)
    assertEquals(tableBounds.right, lastCellBounds.right, GEOMETRY_TOLERANCE)
    assertEquals(
        TABLE_COLUMN_GAP,
        headerNextCell.getBoundingClientRect().left - firstCellBounds.right,
        GEOMETRY_TOLERANCE,
    )
    assertEquals(
        TABLE_ROW_GAP,
        firstDataRow.getBoundingClientRect().top - headerRow.getBoundingClientRect().bottom,
        GEOMETRY_TOLERANCE,
    )
    assertEquals(
        TABLE_ROW_GAP,
        secondDataRow.getBoundingClientRect().top - firstDataRow.getBoundingClientRect().bottom,
        GEOMETRY_TOLERANCE,
    )
}

private fun assertExplicitTableAppearance(
    headerRow: HTMLElement,
    firstDataRow: HTMLElement,
) {
    assertEquals("rgb(1, 2, 3)", window.getComputedStyle(headerRow).backgroundColor)
    assertEquals("rgb(18, 52, 86)", window.getComputedStyle(firstDataRow).backgroundColor)
    assertEquals("rgb(170, 187, 204)", window.getComputedStyle(firstDataRow).borderBottomColor)
}

private fun assertExplicitTableSemantics(
    root: HTMLElement,
    table: HTMLElement,
    headerRow: HTMLElement,
    firstDataRow: HTMLElement,
    headerFirstCell: HTMLElement,
) {
    assertEquals("table", table.getAttribute("role"))
    assertEquals("rowgroup", root.querySelector("thead")?.getAttribute("role"))
    assertEquals("row", headerRow.getAttribute("role"))
    assertEquals("1", headerRow.getAttribute("aria-rowindex"))
    assertEquals("columnheader", headerFirstCell.getAttribute("role"))
    assertEquals("rowgroup", root.querySelector("tbody")?.getAttribute("role"))
    assertEquals("row", firstDataRow.getAttribute("role"))
    assertEquals("rowheader", firstDataRow.querySelector("th[scope=row]")?.getAttribute("role"))
    assertEquals("cell", firstDataRow.querySelector("td")?.getAttribute("role"))
}
