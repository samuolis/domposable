package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBorderEdge
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableVisibility
import io.github.samuolis.domposable.ui.border
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.htmlAttributes
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.region
import io.github.samuolis.domposable.ui.weight
import io.github.samuolis.domposable.ui.width
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlRowStretchTest {
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
