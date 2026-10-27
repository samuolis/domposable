package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableFontWeight
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTypographyRole
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextRun
import io.github.samuolis.domposable.ui.testId
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HtmlTextRunsTest {
    @Test
    fun inlineRunsInheritRoleAndPreserveLiteralText() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableText(
                        runs =
                            listOf(
                                DomposableTextRun("Caption "),
                                DomposableTextRun(
                                    "<strong>literal</strong>",
                                    DomposableFontWeight(750),
                                    DomposableColor(0xFF123456),
                                ),
                            ),
                        modifier = DomposableModifier.testId("runs"),
                        appearance = DomposableTextAppearance(role = DomposableTypographyRole.BodySmall),
                    )
                }
            }
        try {
            val text = root.querySelector("[data-testid=runs]") as HTMLElement
            val runs = text.querySelectorAll("span")
            val first = runs.item(0) as HTMLElement
            val second = runs.item(1) as HTMLElement
            assertEquals("Caption <strong>literal</strong>", text.textContent)
            assertNull(text.querySelector("strong"))
            assertEquals(window.getComputedStyle(text).fontSize, window.getComputedStyle(first).fontSize)
            assertEquals("750", window.getComputedStyle(second).fontWeight)
            assertEquals("rgb(18, 52, 86)", window.getComputedStyle(second).color)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
