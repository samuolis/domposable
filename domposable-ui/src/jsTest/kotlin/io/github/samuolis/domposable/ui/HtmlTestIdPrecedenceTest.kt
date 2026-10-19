package io.github.samuolis.domposable.ui

import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.renderComposable
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlTestIdPrecedenceTest {
    @Test
    fun keepsOutermostTestIdAndAllowsExplicitHtmlOverride() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val taggedModifier = DomposableModifier.testId("outer").testId("inner")
        val explicitOverrideModifier =
            DomposableModifier
                .testId("outer")
                .htmlAttributes { attr("data-testid", "explicit") }
        val composition =
            renderComposable(root) {
                Div(attrs = {
                    domposableAttributes(taggedModifier)
                    attr("data-case", "tagged")
                }) {}
                Div(attrs = {
                    domposableAttributes(explicitOverrideModifier)
                    attr("data-case", "explicit")
                }) {}
            }

        try {
            assertEquals("outer", root.querySelector("[data-case='tagged']")?.getAttribute("data-testid"))
            assertEquals("explicit", root.querySelector("[data-case='explicit']")?.getAttribute("data-testid"))
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
