package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBorderEdge
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableVisibility
import io.github.samuolis.domposable.ui.border
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.horizontalScroll
import io.github.samuolis.domposable.ui.htmlAttributes
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.region
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.weight
import io.github.samuolis.domposable.ui.width
import kotlinx.browser.document
import org.jetbrains.compose.web.css.style
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlRowStretchTest {
    @Test
    @Suppress("LongMethod")
    fun weightedRowsFillStartAlignedPaddedBoxesOnlyWhenChildWeightFills() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    Div(attrs = { style { property("width", "400px") } }) {
                        DomposableColumn(modifier = DomposableModifier.testId("parent-column")) {
                            DomposableBox(
                                DomposableModifier.padding(16.dp).testId("fill-box"),
                            ) {
                                DomposableRow(modifier = DomposableModifier.testId("fill-row")) {
                                    DomposableText(
                                        "Player name",
                                        modifier = DomposableModifier.weight(1f).padding(4.dp).testId("fill-label"),
                                    )
                                    DomposableText("League points", modifier = DomposableModifier.testId("fill-metric"))
                                }
                                DomposableRow(
                                    modifier = DomposableModifier.horizontalScroll().testId("sibling-scroll-row"),
                                ) {
                                    DomposableText("Scrollable sibling content")
                                }
                            }
                            DomposableBox(
                                DomposableModifier.padding(16.dp).testId("nonfill-box"),
                            ) {
                                DomposableRow(modifier = DomposableModifier.testId("nonfill-row")) {
                                    DomposableText(
                                        "Player name",
                                        modifier =
                                            DomposableModifier
                                                .weight(1f, fill = false)
                                                .padding(4.dp)
                                                .testId("nonfill-label"),
                                    )
                                    DomposableText(
                                        "League points",
                                        modifier = DomposableModifier.testId("nonfill-metric"),
                                    )
                                }
                            }
                            DomposableBox(modifier = DomposableModifier.padding(16.dp)) {
                                DomposableBox(modifier = DomposableModifier.width(200.dp).testId("fixed-box")) {
                                    DomposableRow(modifier = DomposableModifier.testId("fixed-row")) {
                                        DomposableText("Player name", modifier = DomposableModifier.weight(1f))
                                        DomposableText("League points")
                                    }
                                }
                            }
                            DomposableBox(modifier = DomposableModifier.width(160.dp)) {
                                DomposableRow(
                                    modifier = DomposableModifier.horizontalScroll().testId("scroll-row"),
                                ) {
                                    DomposableText(
                                        "Scrollable weighted label",
                                        modifier = DomposableModifier.weight(1f),
                                    )
                                    DomposableText("Trailing content")
                                }
                            }
                        }
                    }
                }
            }

        try {
            val fillBox = root.querySelector("[data-testid=fill-box]") as HTMLElement
            val parentColumn = root.querySelector("[data-testid=parent-column]") as HTMLElement
            val fillRow = root.querySelector("[data-testid=fill-row]") as HTMLElement
            val fillMetric = root.querySelector("[data-testid=fill-metric]") as HTMLElement
            val nonfillBox = root.querySelector("[data-testid=nonfill-box]") as HTMLElement
            val nonfillRow = root.querySelector("[data-testid=nonfill-row]") as HTMLElement
            val nonfillMetric = root.querySelector("[data-testid=nonfill-metric]") as HTMLElement
            val scrollRow = root.querySelector("[data-testid=scroll-row]") as HTMLElement
            val fixedBox = root.querySelector("[data-testid=fixed-box]") as HTMLElement
            val fixedRow = root.querySelector("[data-testid=fixed-row]") as HTMLElement
            val siblingScrollRow = root.querySelector("[data-testid=sibling-scroll-row]") as HTMLElement
            val fillBoxBounds = fillBox.getBoundingClientRect()
            val parentColumnBounds = parentColumn.getBoundingClientRect()
            val fillRowBounds = fillRow.getBoundingClientRect()
            val nonfillBoxBounds = nonfillBox.getBoundingClientRect()
            val nonfillRowBounds = nonfillRow.getBoundingClientRect()
            val fixedBoxBounds = fixedBox.getBoundingClientRect()
            val fixedRowBounds = fixedRow.getBoundingClientRect()

            assertEquals(parentColumnBounds.left + 16, fillRowBounds.left, 0.5)
            assertEquals(parentColumnBounds.right - 16, fillRowBounds.right, 0.5)
            assertEquals(parentColumnBounds.left + 16, fillBoxBounds.left, 0.5)
            assertEquals(parentColumnBounds.right - 16, fillBoxBounds.right, 0.5)
            assertEquals(fillBoxBounds.left, fillRowBounds.left, 0.5)
            assertEquals(fillBoxBounds.right, fillRowBounds.right, 0.5)
            assertEquals(fillRowBounds.right, fillMetric.getBoundingClientRect().right, 0.5)
            assertEquals(nonfillBoxBounds.left, nonfillRowBounds.left, 0.5)
            assertEquals(nonfillRowBounds.right, nonfillMetric.getBoundingClientRect().right, 0.5)
            assertTrue(nonfillRowBounds.width < parentColumnBounds.width - 32)
            assertEquals("max-content", scrollRow.style.width)
            assertEquals("max-content", siblingScrollRow.style.width)
            assertEquals("true", scrollRow.getAttribute("data-domposable-horizontal-scroll"))
            assertEquals("true", siblingScrollRow.getAttribute("data-domposable-horizontal-scroll"))
            assertTrue(fixedBoxBounds.width < parentColumnBounds.width - 32)
            assertEquals(fixedBoxBounds.left, fixedRowBounds.left, 0.5)
            assertEquals(fixedBoxBounds.right, fixedRowBounds.right, 0.5)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun stretchIncludesBrandAttributesBorderAndPaddingWhenListIsTaller() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val listModifier =
            DomposableModifier
                .htmlAttributes { classes("test-list-pane") }
                .border(1.dp, DomposableColor(0xFF000000), DomposableBorderEdge.End)
                .padding(DomposableInsets(end = 20.dp))
        val detailModifier =
            DomposableModifier
                .htmlAttributes { classes("test-detail-pane") }
                .padding(DomposableInsets(start = 20.dp))
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableRow(modifier = DomposableModifier.width(600.dp), stretch = true) {
                        DomposableVisibility(
                            true,
                            DomposableModifier
                                .fillMaxWidth(0.32f)
                                .fillMaxHeight()
                                .region("List")
                                .then(listModifier),
                        ) {
                            DomposableBox(DomposableModifier.height(240.dp)) { DomposableText("tall list") }
                        }
                        DomposableVisibility(
                            true,
                            DomposableModifier
                                .weight(1f)
                                .fillMaxHeight()
                                .region("Detail")
                                .then(detailModifier),
                        ) {
                            DomposableBox(DomposableModifier.height(40.dp)) { DomposableText("short detail") }
                        }
                    }
                }
            }

        try {
            val list = root.querySelector(".test-list-pane") as HTMLElement
            val detail = root.querySelector(".test-detail-pane") as HTMLElement
            assertEquals(240, list.offsetHeight)
            assertEquals(list.offsetHeight, detail.offsetHeight)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun autoHeightPanesStretchTheRetainedListToTheTallerDetail() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableRow(modifier = DomposableModifier.width(600.dp), stretch = true) {
                        DomposableVisibility(
                            true,
                            DomposableModifier.fillMaxWidth(0.32f).fillMaxHeight().region("List"),
                        ) {
                            DomposableBox(DomposableModifier.height(40.dp)) { DomposableText("list") }
                        }
                        DomposableVisibility(true, DomposableModifier.weight(1f).fillMaxHeight().region("Detail")) {
                            DomposableBox(DomposableModifier.height(240.dp)) { DomposableText("detail") }
                        }
                    }
                }
            }
        try {
            val list = root.querySelector("[role=region][aria-label=List]") as HTMLElement
            val detail = root.querySelector("[role=region][aria-label=Detail]") as HTMLElement
            assertEquals(240, detail.offsetHeight)
            assertEquals(detail.offsetHeight, list.offsetHeight)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun stretchAlignsRowsAndKeepsPaneHeightModifiersInTheLayoutTree() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableRow(stretch = true) {
                        DomposableBox(DomposableModifier.fillMaxHeight().region("List")) {
                            DomposableText("List")
                        }
                        DomposableBox(DomposableModifier.fillMaxHeight().region("Detail")) {
                            DomposableText("Detail")
                        }
                    }
                }
            }

        try {
            val divs = root.querySelectorAll("div")
            val row =
                (0 until divs.length)
                    .map { divs.item(it) as HTMLElement }
                    .first { it.style.alignItems == "stretch" }
            assertEquals("stretch", row.style.alignItems)
            assertEquals(2, root.querySelectorAll("[role=region]").length)
            val fillHeightWrappers = root.querySelectorAll("[data-domposable-modifier='FillHeight']")
            assertEquals(2, fillHeightWrappers.length)
            assertEquals("100%", (fillHeightWrappers.item(0) as HTMLElement).style.height)
            assertEquals("100%", (fillHeightWrappers.item(1) as HTMLElement).style.height)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
