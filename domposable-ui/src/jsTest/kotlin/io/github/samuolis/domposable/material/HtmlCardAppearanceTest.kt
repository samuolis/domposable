package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableCardVariant
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableCard
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.width
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlCardAppearanceTest {
    @Test
    fun clickableCardUsesSpaceKeyupAndDoesNotClaimNestedSvgClicks() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        var cardClicks = 0
        var nestedClicks = 0
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableCard(onClick = { cardClicks++ }) {
                        Button(attrs = { onClick { nestedClicks++ } }) {
                            Span { Text("Nested action") }
                        }
                    }
                }
            }

        try {
            val card = root.querySelector(".d-card[role=button]") as HTMLElement
            val spaceDown = keyboardEvent("keydown", " ")
            card.dispatchEvent(spaceDown)
            assertTrue(spaceDown.defaultPrevented)
            assertEquals(0, cardClicks)
            card.dispatchEvent(keyboardEvent("keyup", " "))
            assertEquals(1, cardClicks)
            card.dispatchEvent(keyboardEvent("keydown", "Enter"))
            assertEquals(2, cardClicks)

            val nestedButton = root.querySelector("button") as HTMLElement
            val nestedSpan = nestedButton.querySelector("span") as HTMLElement
            val svg = document.createElementNS("http://www.w3.org/2000/svg", "svg")
            val path = document.createElementNS("http://www.w3.org/2000/svg", "path")
            svg.appendChild(path)
            nestedSpan.appendChild(svg)
            path.dispatchEvent(mouseClick())
            assertEquals(1, nestedClicks)
            assertEquals(2, cardClicks)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun explicitPaddingAndBorderDoNotChangeCardOuterWidth() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableCard(
                        modifier = DomposableModifier.width(DomposableLength(320f)),
                        variant = DomposableCardVariant.Outlined,
                        appearance = DomposableSurfaceAppearance(borderWidth = DomposableLength(3f)),
                        contentPadding =
                            DomposableInsets(
                                top = DomposableLength(8f),
                                end = DomposableLength(12f),
                                bottom = DomposableLength(8f),
                                start = DomposableLength(16f),
                            ),
                    ) {
                        DomposableBox(DomposableModifier.fillMaxWidth().testId("card-inner")) { }
                    }
                }
            }

        try {
            val card = root.querySelector(".d-card") as HTMLElement
            val inner = root.querySelector("[data-testid=card-inner]") as HTMLElement
            assertEquals("16px 12px", "${card.style.paddingLeft} ${card.style.paddingRight}")
            assertEquals("3px", card.style.getPropertyValue("--d-card-border-width"))
            assertTrue(card.style.getPropertyValue("--d-card-border-color").isNotEmpty())
            assertEquals(320.0, card.getBoundingClientRect().width)
            assertEquals(292.0, inner.getBoundingClientRect().width)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}

private fun keyboardEvent(
    type: String,
    key: String,
): Event {
    val createEvent = js("(type, key) => new KeyboardEvent(type, { key, bubbles: true, cancelable: true })")
    return createEvent(type, key) as Event
}

private fun mouseClick(): Event = js("new MouseEvent('click', { bubbles: true, cancelable: true })") as Event
