package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.htmlAttributes
import io.github.samuolis.domposable.ui.testId
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlAttributesTest {
    @Test
    fun appAttributesApplyToTextWithoutAddingAModifierWrapper() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableText(
                        "Ranker title",
                        modifier =
                            DomposableModifier.testId("ranker-title").htmlAttributes {
                                classes("app-owned-text")
                                attr("data-app-kind", "title")
                                style { property("color", "rgb(17, 34, 51)") }
                            },
                    )
                }
            }

        try {
            val theme = root.firstElementChild!!
            val text = theme.firstElementChild as HTMLElement

            assertEquals(1, theme.childElementCount)
            assertEquals(0, root.querySelectorAll("[data-domposable-modifier]").length)
            assertEquals("SPAN", text.tagName)
            assertTrue(text.classList.contains("app-owned-text"))
            assertTrue(text.classList.contains("d-text"))
            assertEquals("title", text.getAttribute("data-app-kind"))
            assertEquals("ranker-title", text.getAttribute("data-testid"))
            assertEquals("rgb(17, 34, 51)", window.getComputedStyle(text).color)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
