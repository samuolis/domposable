package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.dp
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class HtmlChildLayoutBoundsTest {
    @Test
    fun weightedItemsKeepFillBoundsThroughTheirDecorationWrappers() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                for (fixed in listOf(false, true)) {
                    Div(attrs = {
                        style {
                            property("width", "400px")
                            property("height", "400px")
                            property("display", "flex")
                            property("flex-direction", "column")
                            property("align-items", "flex-start")
                        }
                    }) {
                        val size = if (fixed) DomposableModifier.width(120.dp) else DomposableModifier
                        DomposableHtmlModifiers(
                            size.then(DomposableModifierOperation.Weight(1f, true)).fillMaxSize().padding(16.dp),
                        ) {
                            Div(attrs = {
                                attr("data-layout-case", fixed.toString())
                                style {
                                    property("height", "100%")
                                    property("width", "100%")
                                }
                            }) {}
                        }
                    }
                }
            }
        try {
            for (fixed in listOf(false, true)) {
                val element = root.querySelector("[data-layout-case='$fixed']") as HTMLElement
                val bounds = element.getBoundingClientRect()
                val width = if (fixed) 88.0 else 368.0
                assertTrue(abs(bounds.width - width) < 1, "Expected content width $width but got ${bounds.width}")
                assertTrue(
                    abs(bounds.height - 368.0) < 1,
                    "A weighted child must keep its 400px allocation minus padding",
                )
            }
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
