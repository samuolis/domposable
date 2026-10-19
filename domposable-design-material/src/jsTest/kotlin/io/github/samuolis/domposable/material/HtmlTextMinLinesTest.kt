package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextOverflow
import io.github.samuolis.domposable.ui.DomposableText
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlTextMinLinesTest {
    @Test
    fun textOverflowAppearanceSetsEllipsisOnlyForEllipsisMode() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableText(
                        "Ellipsized text",
                        appearance = DomposableTextAppearance(maxLines = 2, overflow = DomposableTextOverflow.Ellipsis),
                    )
                    DomposableText(
                        "Clipped text",
                        appearance = DomposableTextAppearance(maxLines = 2, overflow = DomposableTextOverflow.Clip),
                    )
                    DomposableText(
                        "Visible text",
                        appearance = DomposableTextAppearance(maxLines = 2, overflow = DomposableTextOverflow.Visible),
                    )
                }
            }

        try {
            val textNodes = root.querySelectorAll(".d-text")
            val ellipsis = textNodes.item(0) as HTMLElement
            val clip = textNodes.item(1) as HTMLElement
            val visible = textNodes.item(2) as HTMLElement

            assertEquals("ellipsis", window.getComputedStyle(ellipsis).textOverflow)
            assertEquals("2", window.getComputedStyle(ellipsis).getPropertyValue("-webkit-line-clamp"))
            assertEquals("hidden", window.getComputedStyle(ellipsis).overflow)
            assertEquals("clip", window.getComputedStyle(clip).textOverflow)
            assertEquals("hidden", window.getComputedStyle(clip).overflow)
            assertEquals("clip", window.getComputedStyle(visible).textOverflow)
            assertEquals("visible", window.getComputedStyle(visible).overflow)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun appearanceReservesMinimumLinesWithoutChangingDefaultHeight() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableText(
                        "Single line",
                        appearance = DomposableTextAppearance(fontSize = 20f, lineHeight = 20f),
                    )
                    DomposableText(
                        "Single line",
                        appearance = DomposableTextAppearance(fontSize = 20f, lineHeight = 20f, minLines = 3),
                    )
                }
            }

        try {
            val textNodes = root.querySelectorAll(".d-text")
            val default = textNodes.item(0) as HTMLElement
            val threeLines = textNodes.item(1) as HTMLElement
            val style = window.getComputedStyle(threeLines)
            val minimumHeight = style.minHeight.removeSuffix("px").toFloat()
            val lineHeight = style.lineHeight.removeSuffix("px").toFloat()

            assertEquals("", default.style.getPropertyValue("min-height"))
            assertTrue(minimumHeight >= lineHeight * 3f - 0.5f)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
