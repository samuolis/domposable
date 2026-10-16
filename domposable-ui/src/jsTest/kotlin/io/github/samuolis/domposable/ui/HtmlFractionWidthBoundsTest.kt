package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.dp
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class HtmlFractionWidthBoundsTest {
    @Test
    fun fractionWidthBoundsNeverExceedAvailableParentWidth() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                listOf(200, 600, 1000).forEach { availableWidth ->
                    Div(attrs = { style { property("width", "${availableWidth}px") } }) {
                        DomposableHtmlModifiers(
                            DomposableModifier.fillMaxWidth(0.32f, min = 260.dp, max = 360.dp),
                        ) {
                            Div(attrs = { attr("data-width-case", availableWidth.toString()) }) { }
                        }
                    }
                }
            }

        try {
            val expectedWidths = mapOf(200 to 200.0, 600 to 260.0, 1000 to 320.0)
            expectedWidths.forEach { (availableWidth, expectedWidth) ->
                val content = root.querySelector("[data-width-case='$availableWidth']") as HTMLElement
                val measuredWidth = (content.parentElement as HTMLElement).getBoundingClientRect().width
                assertTrue(
                    abs(measuredWidth - expectedWidth) < 1.0,
                    "For a ${availableWidth}px parent, expected ${expectedWidth}px but got ${measuredWidth}px",
                )
            }
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
