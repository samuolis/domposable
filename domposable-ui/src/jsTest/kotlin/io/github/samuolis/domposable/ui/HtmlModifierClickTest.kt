package io.github.samuolis.domposable.ui

import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HtmlModifierClickTest {
    @Test
    fun nestedInteractiveTargetsOwnClicksWithoutStoppingBubbling() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        var parentClicks = 0
        var nestedClicks = 0
        var bubbledClicks = 0
        val composition =
            renderComposable(root) {
                DomposableHtmlModifiers(
                    DomposableModifier.clickable(onClick = { parentClicks++ }, onClickLabel = "Parent action"),
                ) {
                    Span { Text("Parent copy") }
                    Button(attrs = { onClick { nestedClicks++ } }) {
                        Span { Text("Nested control") }
                    }
                }
            }
        root.addEventListener("click", { bubbledClicks++ })

        try {
            val wrapper = root.querySelector("[aria-label='Parent action']") as HTMLElement
            val nestedButton = root.querySelector("button") as HTMLElement
            val nestedSpan = nestedButton.querySelector("span") as HTMLElement
            val svg = document.createElementNS("http://www.w3.org/2000/svg", "svg")
            val path = document.createElementNS("http://www.w3.org/2000/svg", "path")
            svg.appendChild(path)
            nestedSpan.appendChild(svg)

            nestedSpan.dispatchEvent(mouseClick())
            path.dispatchEvent(mouseClick())
            assertEquals(0, parentClicks)
            assertEquals(2, nestedClicks)
            assertEquals(2, bubbledClicks, "Click events must keep bubbling to outside handlers")

            (wrapper.querySelector("span") as HTMLElement).dispatchEvent(mouseClick())
            assertEquals(1, parentClicks)
            assertEquals(3, bubbledClicks)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun clickModifiersUseEnterDownAndSpaceUpAndRespectLinkAndDisabledRoles() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        var legacyClicks = 0
        var buttonClicks = 0
        var linkClicks = 0
        var disabledClicks = 0
        val composition =
            renderComposable(root) {
                DomposableHtmlModifiers(DomposableModifier.clickable("Legacy action") { legacyClicks++ }) {
                    Span { Text("Legacy") }
                }
                DomposableHtmlModifiers(
                    DomposableModifier.clickable(onClick = { buttonClicks++ }, onClickLabel = "Button action"),
                ) { Span { Text("Button") } }
                DomposableHtmlModifiers(
                    DomposableModifier.clickable(
                        onClick = { linkClicks++ },
                        role = DomposableClickRole.Link,
                        onClickLabel = "Link action",
                    ),
                ) { Span { Text("Link") } }
                DomposableHtmlModifiers(
                    DomposableModifier.clickable(
                        onClick = { disabledClicks++ },
                        enabled = false,
                        onClickLabel = "Disabled action",
                    ),
                ) { Span { Text("Disabled") } }
            }

        try {
            val legacy = root.querySelector("[aria-label='Legacy action']") as HTMLElement
            val button = root.querySelector("[aria-label='Button action']") as HTMLElement
            val link = root.querySelector("[aria-label='Link action']") as HTMLElement
            val disabled = root.querySelector("[aria-label='Disabled action']") as HTMLElement
            assertEquals("0", legacy.getAttribute("tabindex"))
            assertEquals("0", button.getAttribute("tabindex"))
            assertEquals("link", link.getAttribute("role"))
            assertEquals("0", link.getAttribute("tabindex"))
            assertNull(disabled.getAttribute("tabindex"))
            assertEquals("true", disabled.getAttribute("aria-disabled"))

            verifyButtonKeyboardActivation(legacy) { legacyClicks }
            verifyButtonKeyboardActivation(button) { buttonClicks }

            val linkSpaceDown = keyboardEvent("keydown", " ")
            link.dispatchEvent(linkSpaceDown)
            link.dispatchEvent(keyboardEvent("keyup", " "))
            assertFalse(linkSpaceDown.defaultPrevented)
            assertEquals(0, linkClicks, "Link modifiers do not activate with Space")
            link.dispatchEvent(keyboardEvent("keydown", "Enter"))
            assertEquals(1, linkClicks)

            disabled.click()
            disabled.dispatchEvent(keyboardEvent("keydown", "Enter"))
            disabled.dispatchEvent(keyboardEvent("keyup", " "))
            assertEquals(0, disabledClicks)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    private fun verifyButtonKeyboardActivation(
        control: HTMLElement,
        clickCount: () -> Int,
    ) {
        val spaceDown = keyboardEvent("keydown", " ")
        control.dispatchEvent(spaceDown)
        assertTrue(spaceDown.defaultPrevented)
        assertEquals(0, clickCount())
        control.dispatchEvent(keyboardEvent("keyup", " "))
        assertEquals(1, clickCount())
        control.dispatchEvent(keyboardEvent("keydown", "Enter"))
        assertEquals(2, clickCount())
        control.dispatchEvent(keyboardEvent("keyup", "Enter"))
        assertEquals(2, clickCount())
    }

    private fun mouseClick(): Event = js("new MouseEvent('click', { bubbles: true, cancelable: true })") as Event

    private fun keyboardEvent(
        type: String,
        key: String,
    ): Event {
        val createEvent = js("(type, key) => new KeyboardEvent(type, { key, bubbles: true, cancelable: true })")
        return createEvent(type, key) as Event
    }
}
