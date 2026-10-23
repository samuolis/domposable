package io.github.samuolis.domposable.material

import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlShellSlotStylesTest {
    @Test
    fun focusedSkipLinkPaintsAboveTheStickyAppHeader() {
        val root = document.createElement("div") as HTMLElement
        root.style.cssText = "position:fixed;inset:0;z-index:2147483647"
        document.body!!.appendChild(root)
        val composition = renderComposable(root) { InstallDomposableMaterialStyles() }
        root.innerHTML =
            """<a class="d-skip-link" href="#content">Skip to content</a><header class="d-app-header" style="height:80px;background:white">Header</header>"""
        try {
            val link = root.querySelector("a") as HTMLElement
            assertTrue(link.getBoundingClientRect().bottom < 0)
            link.focus()
            val bounds = link.getBoundingClientRect()
            assertTrue(bounds.top >= 0)
            assertEquals(
                link,
                document.elementFromPoint(bounds.left + bounds.width / 2, bounds.top + bounds.height / 2),
            )
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun customNavigationPanelDoesNotChangeMaterialDrawerPadding() {
        val style = document.createElement("style")
        style.textContent = ".d-navigation-drawer{padding:16px}" + DOMPOSABLE_HTML_SHELL_SLOT_CSS
        val root = document.createElement("div") as HTMLElement
        root.innerHTML = """<div class="d-navigation-panel"></div><div class="d-navigation-drawer"></div>"""
        document.head!!.appendChild(style)
        document.body!!.appendChild(root)
        try {
            val panel = root.querySelector(".d-navigation-panel") as HTMLElement
            val drawer = root.querySelector(".d-navigation-drawer") as HTMLElement
            assertEquals("0px", window.getComputedStyle(panel).paddingLeft)
            assertEquals("16px", window.getComputedStyle(drawer).paddingLeft)
            assertEquals("16px", window.getComputedStyle(drawer).paddingTop)
        } finally {
            root.parentNode?.removeChild(root)
            style.parentNode?.removeChild(style)
        }
    }
}
