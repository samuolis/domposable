package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableTextAlignment
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableLink
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.weight
import io.github.samuolis.domposable.ui.width
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlButtonContentAlignmentTest {
    @Test
    fun plainTextButtonCentersWrappedDefaultLabel() {
        verifyPlainLabelAlignment(
            DomposableTokens(),
            "A long button label that wraps across multiple lines",
            "center",
            wraps = true,
        )
    }

    @Test
    fun plainTextButtonUsesConfiguredContentAlignment() {
        val tokens = DomposableTokens()
        val startAligned =
            tokens.copy(
                components =
                    tokens.components.copy(
                        button =
                            tokens.components.button.copy(
                                contentAlignment = DomposableAlignment.Start,
                            ),
                    ),
            )
        verifyPlainLabelAlignment(
            startAligned,
            "Start aligned label",
            "start",
        )
    }

    @Test
    fun explicitTextAlignmentOverridesButtonAlignment() {
        val tokens = DomposableTokens()
        val explicitTextAlignment =
            tokens.copy(
                components =
                    tokens.components.copy(
                        button =
                            tokens.components.button.copy(
                                contentAlignment = DomposableAlignment.Start,
                            ),
                        text =
                            tokens.components.text.copy(
                                alignment = DomposableTextAlignment.End,
                            ),
                    ),
            )
        verifyPlainLabelAlignment(
            explicitTextAlignment,
            "Explicit text alignment",
            "end",
        )
    }

    private fun verifyPlainLabelAlignment(
        tokens: DomposableTokens,
        text: String,
        expected: String,
        wraps: Boolean = false,
    ) {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme(tokens = tokens) {
                    DomposableButton(text, {}, DomposableModifier.width(180.dp))
                }
            }
        try {
            val label = root.querySelector(".d-button-copy > .d-text") as HTMLElement
            assertEquals(
                expected,
                kotlinx.browser.window
                    .getComputedStyle(label)
                    .textAlign,
            )
            if (wraps) assertTrue(label.getBoundingClientRect().height > 20)
        } finally {
            composition.dispose()
            root.remove()
        }
    }

    @Test
    fun buttonsAndLinksAlignContentLogicallyInBothDirections() {
        for (direction in listOf("ltr", "rtl")) {
            for (alignment in DomposableAlignment.entries) {
                verifyAlignment(direction, alignment)
            }
        }
    }

    @Test
    fun weightedCustomContentUsesTheWholeButtonWidth() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableButton(
                        onClick = {},
                        modifier = DomposableModifier.width(320.dp),
                        appearance =
                            DomposableButtonAppearance(
                                borderWidth = 0.dp,
                                contentPadding = DomposableInsets(start = 16.dp, end = 16.dp),
                            ),
                    ) {
                        DomposableRow(modifier = DomposableModifier.fillMaxWidth(), gap = 12.dp) {
                            DomposableText("Weighted label", modifier = DomposableModifier.weight(1f))
                            DomposableText("›")
                        }
                    }
                }
            }
        try {
            val button = root.querySelector("button") as HTMLElement
            val labels = root.querySelectorAll(".d-text")
            val label = labels.item(0) as HTMLElement
            val trailing = labels.item(1) as HTMLElement
            val bounds = button.getBoundingClientRect()
            assertTrue(abs(label.getBoundingClientRect().left - (bounds.left + 16)) < 1)
            assertTrue(abs(trailing.getBoundingClientRect().right - (bounds.right - 16)) < 1)
            assertTrue(
                kotlinx.browser.window
                    .getComputedStyle(label)
                    .textAlign == "start",
            )
        } finally {
            composition.dispose()
            root.remove()
        }
    }

    private fun verifyAlignment(
        direction: String,
        alignment: DomposableAlignment,
    ) {
        val root = document.createElement("div") as HTMLElement
        root.dir = direction
        document.body!!.appendChild(root)
        val appearance =
            DomposableButtonAppearance(
                contentAlignment = alignment,
                contentPadding = DomposableInsets(start = 16.dp, end = 16.dp),
                borderWidth = 0.dp,
            )
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableButton({}, DomposableModifier.width(240.dp), appearance = appearance) {
                        Span { Text("Button") }
                    }
                    DomposableLink(
                        "Link",
                        "/next",
                        DomposableModifier.width(240.dp),
                        buttonAppearance = appearance,
                    ) {
                        org.jetbrains.compose.web.dom
                            .Span { Text("Link") }
                    }
                }
            }
        try {
            for (selector in listOf("button", "a")) {
                assertAlignment(root, selector, direction, alignment)
            }
        } finally {
            composition.dispose()
            root.remove()
        }
    }

    private fun assertAlignment(
        root: HTMLElement,
        selector: String,
        direction: String,
        alignment: DomposableAlignment,
    ) {
        val control = root.querySelector(selector) as HTMLElement
        val copy = (control.querySelector(".d-button-copy > span") ?: control.firstElementChild) as HTMLElement
        val bounds = control.getBoundingClientRect()
        val content = copy.getBoundingClientRect()
        val expected =
            when (alignment) {
                DomposableAlignment.Start -> {
                    if (direction ==
                        "ltr"
                    ) {
                        bounds.left + 16
                    } else {
                        bounds.right - 16 - content.width
                    }
                }

                DomposableAlignment.Center -> {
                    bounds.left + (bounds.width - content.width) / 2
                }

                DomposableAlignment.End -> {
                    if (direction ==
                        "ltr"
                    ) {
                        bounds.right - 16 - content.width
                    } else {
                        bounds.left + 16
                    }
                }
            }
        assertTrue(
            abs(expected - content.left) < 1,
            "$selector $direction $alignment: expected $expected, actual ${content.left}",
        )
    }
}
