package io.github.samuolis.domposable.material

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlRichContentStylesTest {
    @Test
    fun richHtmlAndEditorHooksApplyTheirBrowserStyles() {
        val style = document.createElement("style")
        style.textContent = HTML_RICH_CONTENT_CSS
        val root = document.createElement("div") as HTMLElement
        root.setAttribute("style", "--d-outline: rgb(18, 52, 86); --d-primary: rgb(1, 2, 3)")
        root.innerHTML =
            """
            <article class="d-rich-html"><p>Document</p></article>
            <div class="d-rich-text-editor"><div class="d-rich-text-editor__surface"><p>Draft</p></div></div>
            """.trimIndent()
        document.head!!.appendChild(style)
        document.body!!.appendChild(root)

        try {
            val richHtml = root.querySelector(".d-rich-html") as HTMLElement
            val paragraph = richHtml.querySelector("p") as HTMLElement
            val editor = root.querySelector(".d-rich-text-editor") as HTMLElement
            val editorSurface = root.querySelector(".d-rich-text-editor__surface") as HTMLElement

            assertEquals("0px", window.getComputedStyle(richHtml).minWidth)
            assertEquals("16px", window.getComputedStyle(paragraph).marginBottom)
            assertEquals("auto", window.getComputedStyle(editor).overflowY)
            assertEquals("anywhere", window.getComputedStyle(editorSurface).overflowWrap)
        } finally {
            root.parentNode?.removeChild(root)
            style.parentNode?.removeChild(style)
        }
    }
}
