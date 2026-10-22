package io.github.samuolis.domposable.material

import kotlinx.browser.document
import org.w3c.dom.HTMLIFrameElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlContentSlotStylesTest {
    @Test
    fun mobileBodyInputVariantPreservesOrdinaryFieldTypography() {
        val frame = document.createElement("iframe") as HTMLIFrameElement
        frame.style.width = "390px"
        document.body!!.appendChild(frame)
        try {
            val content = frame.contentDocument!!
            val style = content.createElement("style")
            style.textContent = HTML_CONTENT_SLOT_CSS
            content.head!!.appendChild(style)
            content.body!!.innerHTML =
                """
                <div class="d-field-control"><input id="ordinary" style="font-size:22px"></div>
                <div class="d-mobile-body-input"><div class="d-field-control"><input id="mobile-body" style="font-size:14px"></div></div>
                """.trimIndent()
            val ordinary = content.getElementById("ordinary")!!
            val mobileBody = content.getElementById("mobile-body")!!
            assertEquals("22px", frame.contentWindow!!.getComputedStyle(ordinary).fontSize)
            assertEquals("16px", frame.contentWindow!!.getComputedStyle(mobileBody).fontSize)
        } finally {
            frame.parentNode?.removeChild(frame)
        }
    }
}
