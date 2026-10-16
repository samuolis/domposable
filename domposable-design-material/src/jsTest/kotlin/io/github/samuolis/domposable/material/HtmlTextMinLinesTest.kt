package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableTextAppearance
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
