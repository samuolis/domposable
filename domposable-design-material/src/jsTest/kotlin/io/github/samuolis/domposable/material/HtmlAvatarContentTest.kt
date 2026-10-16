package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.ui.DomposableAvatar
import io.github.samuolis.domposable.ui.DomposableText
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlAvatarContentTest {
    @Test
    fun callerContentReplacesInitialsAndRetainsAvatarSemantics() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableAvatar("AB", "Alex Brown", content = { DomposableText("Profile photo") })
                }
            }

        try {
            val avatar = root.querySelector(".d-avatar") as HTMLElement
            val style = kotlinx.browser.window.getComputedStyle(avatar)
            assertEquals("hidden", style.getPropertyValue("overflow"))
            assertEquals("relative", style.position)
            assertEquals("img", avatar.getAttribute("role"))
            assertEquals("Alex Brown", avatar.getAttribute("aria-label"))
            assertEquals("Profile photo", avatar.textContent?.trim())
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
