package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableIconButton
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlIconButtonAlignmentTest {
    @Test
    fun iconContentIsCenteredInAnExpandedHitTarget() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableIconButton(
                        onClick = {},
                        label = "Centered action",
                        appearance =
                            DomposableButtonAppearance(
                                minHeight = 48.dp,
                                minWidth = 48.dp,
                                contentPadding = DomposableInsets(0.dp),
                            ),
                    ) {
                        Span(attrs = {
                            attr("data-testid", "icon-content")
                            style {
                                property("display", "block")
                                property("width", "24px")
                                property("height", "24px")
                            }
                        }) {}
                    }
                }
            }

        try {
            val button = root.querySelector("button[aria-label='Centered action']") as HTMLElement
            val content = root.querySelector("[data-testid=icon-content]") as HTMLElement
            val buttonBounds = button.getBoundingClientRect()
            val contentBounds = content.getBoundingClientRect()

            assertEquals(48.0, buttonBounds.width, 1.0)
            assertEquals(48.0, buttonBounds.height, 1.0)
            assertEquals(buttonBounds.left + buttonBounds.width / 2, contentBounds.left + contentBounds.width / 2, 0.5)
            assertEquals(buttonBounds.top + buttonBounds.height / 2, contentBounds.top + contentBounds.height / 2, 0.5)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
