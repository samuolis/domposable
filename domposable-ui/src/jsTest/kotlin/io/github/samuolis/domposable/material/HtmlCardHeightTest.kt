package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableCard
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.fillMaxSize
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class HtmlCardHeightTest {
    @Test
    fun filledCardPassesItsBoundedHeightThroughTheContentWrapper() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    Div(attrs = {
                        style {
                            property("width", "300px")
                            property("height", "240px")
                        }
                    }) {
                        DomposableCard(
                            modifier = DomposableModifier.fillMaxSize(),
                            contentPadding = DomposableInsets(16.dp),
                        ) {
                            Div(attrs = {
                                classes("filled-child")
                                style { property("height", "100%") }
                            }) {}
                        }
                    }
                }
            }
        try {
            val card = root.querySelector(".d-card") as HTMLElement
            val content = root.querySelector(".d-card-content") as HTMLElement
            val child = root.querySelector(".filled-child") as HTMLElement
            assertTrue(abs(card.getBoundingClientRect().height - 240.0) < 0.5)
            assertTrue(abs(content.getBoundingClientRect().height - 208.0) < 0.5)
            assertTrue(abs(child.getBoundingClientRect().height - 208.0) < 0.5)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
