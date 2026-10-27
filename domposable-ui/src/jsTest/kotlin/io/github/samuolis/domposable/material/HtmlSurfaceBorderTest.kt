package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableSurface
import io.github.samuolis.domposable.ui.size
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlSurfaceBorderTest {
    @Test
    fun overlayBorderDoesNotInsetContentAndKeepsShadow() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableSurface(
                        modifier = DomposableModifier.size(DomposableLength(100f)),
                        appearance =
                            DomposableSurfaceAppearance(
                                border = DomposableColor(0xFF123456),
                                borderWidth = DomposableLength(4f),
                                shadowElevation = DomposableLength(3f),
                            ),
                    ) {
                        Div(attrs = {
                            attr("data-testid", "bordered-content")
                            style {
                                property("width", "100%")
                                property("height", "32px")
                            }
                        }) { }
                    }
                    DomposableSurface(
                        modifier = DomposableModifier.size(DomposableLength(100f)),
                    ) {
                        Div(attrs = {
                            attr("data-testid", "plain-content")
                            style {
                                property("width", "100%")
                                property("height", "32px")
                            }
                        }) { }
                    }
                }
            }

        try {
            val borderedContent = root.querySelector("[data-testid=bordered-content]") as HTMLElement
            val plainContent = root.querySelector("[data-testid=plain-content]") as HTMLElement
            val bordered = borderedContent.parentElement as HTMLElement
            val overlay = bordered.lastElementChild as HTMLElement
            assertEquals(100.0, borderedContent.getBoundingClientRect().width)
            assertEquals(plainContent.getBoundingClientRect().width, borderedContent.getBoundingClientRect().width)
            assertEquals(32.0, borderedContent.getBoundingClientRect().height)
            assertEquals("4px solid rgb(18, 52, 86)", overlay.style.border)
            assertEquals("absolute", overlay.style.position)
            assertEquals("none", overlay.style.getPropertyValue("pointer-events"))
            assertEquals("", bordered.style.border)
            assertTrue(window.getComputedStyle(bordered).boxShadow.isNotEmpty())
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
