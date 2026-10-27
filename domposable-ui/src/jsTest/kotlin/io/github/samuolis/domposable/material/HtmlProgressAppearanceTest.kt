package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableProgressAppearance
import io.github.samuolis.domposable.ui.DomposableProgress
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlProgressAppearanceTest {
    @Test
    fun explicitLinearStopIndicatorKeepsProgressSemanticsAndDefaultRendering() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableProgress(value = 0.5f, circular = false)
                    DomposableProgress(
                        value = 0.5f,
                        circular = false,
                        appearance = DomposableProgressAppearance(showStopIndicator = false),
                    )
                    DomposableProgress(
                        value = 0.5f,
                        circular = false,
                        appearance =
                            DomposableProgressAppearance(
                                color = DomposableColor(0xFF112233),
                                track = DomposableColor(0xFF445566),
                                strokeWidth = DomposableLength(8f),
                                showStopIndicator = true,
                            ),
                    )
                }
            }

        try {
            val theme = root.firstElementChild!!
            val default = theme.children.item(0) as HTMLElement
            val withoutStop = theme.children.item(1) as HTMLElement
            val withStop = theme.children.item(2) as HTMLElement

            assertEquals(3, theme.childElementCount)
            assertEquals("DIV", default.tagName)
            assertEquals("50", default.getAttribute("aria-valuenow"))
            assertEquals("true", default.querySelector(".d-progress-stop-indicator")?.getAttribute("aria-hidden"))
            assertEquals("DIV", withoutStop.tagName)
            assertEquals("DIV", withStop.tagName)
            assertEquals("progressbar", withoutStop.getAttribute("role"))
            assertEquals("50", withoutStop.getAttribute("aria-valuenow"))
            assertEquals("50", withStop.getAttribute("aria-valuenow"))
            assertEquals(null, withoutStop.querySelector(".d-progress-stop-indicator"))
            assertEquals("true", withStop.querySelector(".d-progress-stop-indicator")?.getAttribute("aria-hidden"))
            assertEquals("8px", withStop.style.height)
            assertTrue(withStop.style.getPropertyValue("--d-progress-color").isNotEmpty())
            assertTrue(withStop.style.getPropertyValue("--d-progress-track").isNotEmpty())
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
