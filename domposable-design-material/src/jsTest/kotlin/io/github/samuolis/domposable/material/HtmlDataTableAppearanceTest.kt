package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableColor
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
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlDataTableAppearanceTest {
    @Test
    fun compactRowsUseSurfaceAndInteractionAppearanceAndKeepKeyboardActivation() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        var activated = ""
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableDataTable(
                        columns = listOf(DomposableDataTableColumn("name", "Name")),
                        rows = listOf(DomposableDataTableRow("one", accessibleLabel = "First row", selected = true)),
                        modifier = DomposableModifier.testId("table"),
                        sizePreset = DomposableDataTableSize.Compact,
                        rowHeaderColumnKey = "name",
                        onRowActivate = { activated = it },
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
                        cellContent = { _, _ ->
                            org.jetbrains.compose.web.dom
                                .Text("Example")
                        },
                    )
                }
            }
        try {
            val table = root.querySelector("[data-testid=table] table") as HTMLElement
            val row = root.querySelector("tr[data-row-key=one]") as HTMLElement
            assertTrue(table.classList.contains("d-data-table"))
            assertEquals("TR", row.tagName)
            assertEquals(null, row.getAttribute("role"))
            assertEquals("0", row.getAttribute("tabindex"))
            assertEquals("true", row.getAttribute("aria-selected"))
            assertEquals("First row", row.getAttribute("aria-label"))
            assertEquals("row", row.querySelector("th")?.getAttribute("scope"))
            assertEquals("96px", row.style.getPropertyValue("--d-data-compact-label-width"))
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
}
