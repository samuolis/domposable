package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.dp
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class HtmlViewportHeightTest {
    @Test
    fun viewportBoundsReserveChromeAndMaximumKeepsShortContentSized() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableHtmlModifiers(DomposableModifier.viewportHeight(0.5f, subtract = 24.dp)) {
                    Div(attrs = { attr("data-case", "fixed") }) {}
                }
                DomposableHtmlModifiers(
                    DomposableModifier.minViewportHeight(0.25f, unit = DomposableViewportUnit.Small),
                ) {
                    Div(attrs = {
                        attr("data-case", "minimum")
                        style { property("height", "16px") }
                    }) {}
                }
                DomposableHtmlModifiers(DomposableModifier.maxViewportHeight(0.5f)) {
                    Div(attrs = {
                        attr("data-case", "short")
                        style { property("height", "32px") }
                    }) {}
                }
                DomposableHtmlModifiers(DomposableModifier.maxViewportHeight(0.5f)) {
                    Div(attrs = {
                        attr("data-case", "long")
                        style { property("height", "2000px") }
                    }) {}
                }
            }
        try {
            fun height(name: String): Double =
                (
                    root
                        .querySelector(
                            "[data-case='$name']",
                        )!!
                        .parentElement as HTMLElement
                ).getBoundingClientRect().height
            val viewport = window.innerHeight.toDouble()
            assertTrue(abs(height("fixed") - (viewport / 2 - 24)) < 1, "Fixed viewport height must reserve chrome")
            assertTrue(abs(height("minimum") - viewport / 4) < 1, "Minimum viewport height must fill a short document")
            assertTrue(abs(height("short") - 32) < 1, "A maximum must not expand short content")
            assertTrue(abs(height("long") - viewport / 2) < 1, "Long content must remain bounded")
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
