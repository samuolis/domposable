package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBadge
import io.github.samuolis.domposable.ui.DomposableText
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class HtmlBadgeContentTest {
    @Test
    fun customContentRetainsBadgeLabelAndConfigurableGeometry() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableBadge(
                        label = "Registration closes soon",
                        contentPadding = DomposableInsets(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
                        minWidth = 24.dp,
                        minHeight = 24.dp,
                    ) { DomposableText("Soon") }
                }
            }
        try {
            val badge = root.querySelector(".d-badge") as HTMLElement
            assertEquals("Registration closes soon", badge.getAttribute("aria-label"))
            assertEquals("Soon", badge.textContent?.trim())
            assertFalse(badge.classList.contains("d-badge-dot"))
            assertEquals("2px 8px", badge.style.padding)
            assertEquals("24px", badge.style.minHeight)
            assertEquals("24px", badge.style.minWidth)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
