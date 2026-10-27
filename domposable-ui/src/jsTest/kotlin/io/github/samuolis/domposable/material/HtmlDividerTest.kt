package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.ui.DomposableDivider
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlDividerTest {
    @Test
    fun defaultVerticalDividerUsesTheInlineBorderWithoutBrowserMargins() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme { DomposableDivider(DomposableOrientation.Vertical) }
            }
        try {
            val divider = root.querySelector("hr") as HTMLElement
            val style = kotlinx.browser.window.getComputedStyle(divider)
            assertEquals("vertical", divider.getAttribute("aria-orientation"))
            assertEquals("1px", style.width)
            assertEquals("0px", style.marginTop)
            assertEquals("0px", style.marginBottom)
            assertEquals("0px", style.borderTopWidth)
            assertEquals("1px", style.getPropertyValue("border-inline-start-width"))
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
