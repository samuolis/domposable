package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composition
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableDialog
import io.github.samuolis.domposable.ui.DomposableModalAlignment
import io.github.samuolis.domposable.ui.DomposableModalLayout
import io.github.samuolis.domposable.ui.DomposableText
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.Window
import kotlin.js.unsafeCast
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlModalWidthTest {
    @Test
    fun compactBottomSheetUsesAll320Pixels() {
        withViewport(320) { root, frameWindow ->
            val dialog = root.querySelector("dialog")!!.unsafeCast<HTMLElement>()
            assertEquals(320.0, dialog.getBoundingClientRect().width, 1.0)
            assertEquals("0px", frameWindow.getComputedStyle(dialog).getPropertyValue("--d-modal-inline-inset").trim())
        }
    }

    @Test
    fun desktopBottomSheetKeepsInsetsAt800And1280Pixels() {
        listOf(800 to 768.0, 1280 to 900.0).forEach { (viewportWidth, expectedWidth) ->
            withViewport(viewportWidth) { root, frameWindow ->
                val dialog = root.querySelector("dialog")!!.unsafeCast<HTMLElement>()
                assertEquals(expectedWidth, dialog.getBoundingClientRect().width, 1.0)
                assertEquals("", frameWindow.getComputedStyle(dialog).getPropertyValue("--d-modal-inline-inset").trim())
            }
        }
    }

    @Test
    fun centeredDialogRetainsViewportInsets() {
        withViewport(800, DomposableModalAlignment.Center) { root, _ ->
            val dialog = root.querySelector("dialog")!!.unsafeCast<HTMLElement>()
            assertEquals(768.0, dialog.getBoundingClientRect().width, 1.0)
        }
    }

    @Test
    fun explicitMaximumWidthRemainsEffectiveOnCompactSheets() {
        withViewport(320, maxWidth = 280.dp) { root, _ ->
            val dialog = root.querySelector("dialog")!!.unsafeCast<HTMLElement>()
            assertEquals(280.0, dialog.getBoundingClientRect().width, 1.0)
        }
    }

    private fun withViewport(
        width: Int,
        alignment: DomposableModalAlignment = DomposableModalAlignment.BottomOnCompact,
        maxWidth: DomposableLength = 900.dp,
        verify: (HTMLElement, Window) -> Unit,
    ) {
        val frame = document.createElement("iframe") as HTMLIFrameElement
        frame.style.width = "${width}px"
        frame.style.height = "800px"
        frame.style.border = "0"
        document.body!!.appendChild(frame)
        val frameDocument = frame.contentDocument!!
        val root = frameDocument.createElement("div").unsafeCast<HTMLElement>()
        frameDocument.body!!.appendChild(root)
        val composition: Composition =
            renderComposable(root) {
                ProvideDomposableMaterialComponents(DomposableTokens()) {
                    DomposableDialog(
                        "Width test",
                        {},
                        layout =
                            DomposableModalLayout(
                                maxWidth = maxWidth,
                                fillViewportWidth = true,
                                viewportInset = 16.dp,
                                alignment = alignment,
                            ),
                    ) { DomposableText("Body") }
                }
            }
        val globalStyles = document.head!!.querySelectorAll("style")
        for (index in 0 until globalStyles.length) {
            frameDocument.head!!.appendChild(globalStyles.item(index)!!.cloneNode(true))
        }
        try {
            val frameWindow = frame.contentWindow!!
            assertEquals(width, frameWindow.innerWidth)
            verify(root, frameWindow)
        } finally {
            composition.dispose()
            frame.remove()
        }
    }
}
