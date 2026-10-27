package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.ui.DomposableIconButton
import io.github.samuolis.domposable.ui.DomposableLink
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.htmlAttributes
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlHiddenControlTest {
    @Test
    fun materialDisplayRulesPreserveTheHtmlHiddenAttribute() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val hidden = DomposableModifier.htmlAttributes { attr("hidden", "") }
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableLink(
                        "Hidden destination",
                        "/next",
                        modifier = hidden,
                        buttonAppearance = DomposableButtonAppearance(),
                    )
                    DomposableIconButton(onClick = {}, label = "Hidden action", modifier = hidden) { Text("+") }
                }
            }
        try {
            listOf("a", "button").forEach { selector ->
                val control = root.querySelector(selector) as HTMLElement
                assertEquals("none", window.getComputedStyle(control).display)
                assertEquals(0.0, control.getBoundingClientRect().width)
            }
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
