package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.dp
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class HtmlAspectRatioBoundsTest {
    @Test
    fun aspectRatioConstrainsLaterBorderedModifierWrappers() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        val widths = listOf(110, 300)
        val composition =
            renderComposable(root) {
                widths.forEach { width ->
                    DomposableHtmlModifiers(
                        modifier =
                            DomposableModifier
                                .width(width.dp)
                                .aspectRatio(0.7f)
                                .testId("aspect-ratio-$width")
                                .then(TestAspectRatioBorder),
                        operationRenderer = { element ->
                            if (element == TestAspectRatioBorder) {
                                (
                                    {
                                        attr("data-aspect-ratio-border", width.toString())
                                        style {
                                            property("box-sizing", "border-box")
                                            property("border", "2px solid transparent")
                                        }
                                    }
                                )
                            } else {
                                null
                            }
                        },
                    ) {
                        DomposableHtmlModifiers(
                            DomposableModifier.fillMaxWidth().aspectRatio(0.7f),
                        ) {
                            Div(attrs = { style { property("width", "100%") } }) {}
                        }
                    }
                }
            }

        try {
            widths.forEach { width ->
                val bordered = root.querySelector("[data-aspect-ratio-border='$width']") as HTMLElement
                val aspectRatioBox = bordered.closest("[data-domposable-modifier='AspectRatio']") as HTMLElement
                val outerBounds = aspectRatioBox.getBoundingClientRect()
                val borderedBounds = bordered.getBoundingClientRect()
                val expectedHeight = width / 0.7

                assertTrue(
                    abs(outerBounds.height - expectedHeight) < 1,
                    "Expected ${width}px aspect-ratio wrapper height $expectedHeight, got ${outerBounds.height}",
                )
                assertTrue(
                    abs(borderedBounds.width - outerBounds.width) < 1 &&
                        abs(borderedBounds.height - outerBounds.height) < 1,
                    "Bordered modifier wrapper should fill its aspect-ratio parent at $width px",
                )
            }
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}

private data object TestAspectRatioBorder : DomposableModifierElement
