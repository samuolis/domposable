package io.github.samuolis.domposable.material

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlShellSlotStylesTest {
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
