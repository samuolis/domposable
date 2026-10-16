package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.region
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlRowStretchTest {
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
