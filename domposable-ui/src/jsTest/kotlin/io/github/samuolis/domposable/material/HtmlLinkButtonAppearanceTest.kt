package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTypographyRole
import io.github.samuolis.domposable.ui.DomposableLink
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.testId
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.MouseEventInit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlLinkButtonAppearanceTest {
    @Test
    fun plainLinkKeepsInlineGeometryAndModifiedAnchorBehavior() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        var callbackCount = 0
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableLink(
                        label = "Plain link",
                        href = "/next",
                        modifier = DomposableModifier.testId("plain-link"),
                        onClick = { callbackCount++ },
                    )
                }
            }
        try {
            val link = root.querySelector("[data-testid=plain-link]") as HTMLElement
            assertEquals("A", link.tagName)
            assertEquals("/next", link.getAttribute("href"))
            assertEquals("0px", window.getComputedStyle(link).paddingTop)
            assertEquals("0px", window.getComputedStyle(link).minHeight)
            assertEquals("16px", link.style.fontSize)
            assertEquals("24px", link.style.lineHeight)

            val ordinaryClick = MouseEvent("click", MouseEventInit(bubbles = true, cancelable = true))
            assertTrue(!link.dispatchEvent(ordinaryClick))
            assertTrue(ordinaryClick.defaultPrevented)
            assertEquals(1, callbackCount)

            val modifiedClick =
                MouseEvent("click", MouseEventInit(bubbles = true, cancelable = true, ctrlKey = true))
            // Observe the renderer before stopping the fixture from navigating the Karma runner.
            var browserDefaultAllowed = false
            val preventFixtureNavigation: (org.w3c.dom.events.Event) -> Unit = { event ->
                browserDefaultAllowed = !event.defaultPrevented
                event.preventDefault()
            }
            root.addEventListener("click", preventFixtureNavigation)
            link.dispatchEvent(modifiedClick)
            root.removeEventListener("click", preventFixtureNavigation)
            assertTrue(browserDefaultAllowed)
            assertEquals(1, callbackCount)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun explicitTypographyRoleUsesSharedCatalogAppearance() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableLink(
                        label = "Role link",
                        href = "/role",
                        modifier = DomposableModifier.testId("role-link"),
                        appearance = DomposableTextAppearance(role = DomposableTypographyRole.BodyMedium),
                    )
                }
            }
        try {
            val link = root.querySelector("[data-testid=role-link]") as HTMLElement
            assertTrue(link.style.fontSize.startsWith("var(--d-type-body-medium-size"))
            assertTrue(link.style.lineHeight.startsWith("var(--d-type-body-medium-line-height"))
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun buttonAppearanceStylesTheAnchorAndOrdinaryClickUsesCallback() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        var callbackCount = 0
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableLink(
                        label = "Continue",
                        href = "/next",
                        modifier = DomposableModifier.testId("action-link"),
                        onClick = { callbackCount++ },
                        buttonAppearance =
                            DomposableButtonAppearance(
                                container = DomposableColor(0xFF123456),
                                minHeight = DomposableLength(56f),
                                minWidth = DomposableLength(120f),
                            ),
                        buttonStyle = DomposableButtonStyle.Outlined,
                        content = { Span { Text("Custom action content") } },
                    )
                }
            }
        try {
            val link = root.querySelector("[data-testid=action-link]") as HTMLElement
            assertEquals("A", link.tagName)
            assertEquals("/next", link.getAttribute("href"))
            assertTrue("d-button" in link.className)
            assertTrue("d-button-outlined" in link.className)
            assertEquals("Custom action content", link.textContent)
            assertTrue(!link.getAttribute("aria-label").isNullOrBlank())
            assertEquals("56px", link.style.minHeight)
            assertEquals("120px", link.style.minWidth)
            assertEquals("rgb(18, 52, 86)", window.getComputedStyle(link).backgroundColor)

            val event = js("new MouseEvent('click', { bubbles: true, cancelable: true })") as org.w3c.dom.events.Event
            assertTrue(!link.dispatchEvent(event))
            assertTrue(event.defaultPrevented)
            assertEquals(1, callbackCount)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
