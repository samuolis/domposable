package io.github.samuolis.domposable.material

import kotlinx.browser.document
import org.w3c.dom.HTMLIFrameElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlContentSlotStylesTest {
    @Test
    fun bodyInputVariantPreservesOrdinaryAndExplicitTypography() {
        listOf(390, 1280).forEach { width ->
            val frame = document.createElement("iframe") as HTMLIFrameElement
            frame.style.width = "${width}px"
            document.body!!.appendChild(frame)
            try {
                val content = frame.contentDocument!!
                val style = content.createElement("style")
                style.textContent = HTML_CONTENT_SLOT_CSS
                content.head!!.appendChild(style)
                content.body!!.innerHTML =
                    """
                    <div class="d-field-control"><input id="ordinary" style="font-size:22px"></div>
                    <div class="d-field-control"><input id="body-default" class="d-body-input"></div>
                    <div class="d-field-control"><input id="body-custom" class="d-body-input" style="font-size:14px"></div>
                    """.trimIndent()

                fun font(id: String) = frame.contentWindow!!.getComputedStyle(content.getElementById(id)!!).fontSize
                assertEquals("22px", font("ordinary"))
                assertEquals("16px", font("body-default"))
                assertEquals(if (width < 600) "16px" else "14px", font("body-custom"))
            } finally {
                frame.parentNode?.removeChild(frame)
            }
        }
    }
}
