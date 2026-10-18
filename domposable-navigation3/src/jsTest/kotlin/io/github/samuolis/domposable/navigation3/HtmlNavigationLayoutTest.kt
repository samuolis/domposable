package io.github.samuolis.domposable.navigation3

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlNavigationLayoutTest {
    @Test
    fun displayFillsFlexSlotAndLeavesVerticalScrollingToItsEntry() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                displayHost {
                    DomposableNavDisplay(
                        entries =
                            listOf(
                                NavEntry(key = "screen", contentKey = "screen") {
                                    ScreenWithOwnedScroll()
                                },
                            ),
                        modifier = DomposableModifier.fillMaxWidth(),
                        onBack = {},
                    )
                }
            }
        try {
            val host = root.querySelector("[data-host]") as HTMLElement
            val allocatedSlot = host.children.item(1) as HTMLElement
            val section = root.querySelector("section[data-entry-id='screen']") as HTMLElement
            val display = section.parentElement as HTMLElement
            val screen = root.querySelector("[data-screen-root]") as HTMLElement
            val scrollOwner = root.querySelector("[data-screen-scroll]") as HTMLElement

            assertHeight(500.0, allocatedSlot, "The display must receive the space below the 100px header")
            assertHeight(500.0, display, "The display must fill its allocated flex slot")
            assertHeight(500.0, section, "The entry must fill the display height")
            assertHeight(500.0, screen, "The entry screen must receive the bounded height")
            assertHeight(500.0, scrollOwner, "The screen's own scroll viewport must fill the entry")
            assertTrue(scrollOwner.scrollHeight > scrollOwner.clientHeight, "The screen must own vertical scrolling")
            assertEquals("visible", window.getComputedStyle(section).overflowY)
            assertEquals("visible", window.getComputedStyle(display).overflowY)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun visibleEntriesShareTheAvailableWidthEqually() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                displayHost(width = 500, height = 400, headerHeight = 80) {
                    DomposableNavDisplay(
                        entries =
                            listOf(
                                NavEntry(key = "one", contentKey = "one") { Div(attrs = {}) {} },
                                NavEntry(key = "two", contentKey = "two") { Div(attrs = {}) {} },
                            ),
                        modifier = DomposableModifier.fillMaxWidth(),
                        visibleEntries = 2,
                        onBack = {},
                    )
                }
            }
        try {
            val entries = root.querySelectorAll("section[data-entry-id]")
            assertEquals(2, entries.length)
            val first = entries.item(0) as HTMLElement
            val second = entries.item(1) as HTMLElement
            assertHeight(320.0, first, "Visible entries must fill the host's remaining height")
            assertHeight(320.0, second, "Visible entries must fill the host's remaining height")
            assertWidth(250.0, first, "Visible entries must split the available width evenly")
            assertWidth(250.0, second, "Visible entries must split the available width evenly")
            assertTrue(abs(first.getBoundingClientRect().left - second.getBoundingClientRect().left) > 1)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun explicitModifierHeightWinsOverTheDefaultDisplayHeight() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                displayHost {
                    DomposableNavDisplay(
                        entries =
                            listOf(
                                NavEntry(key = "screen", contentKey = "screen") { Div(attrs = {}) {} },
                            ),
                        modifier = DomposableModifier.fillMaxWidth().height(300.dp),
                        onBack = {},
                    )
                }
            }
        try {
            val host = root.querySelector("[data-host]") as HTMLElement
            val allocatedSlot = host.children.item(1) as HTMLElement
            val section = root.querySelector("section[data-entry-id='screen']") as HTMLElement
            val display = section.parentElement as HTMLElement

            assertHeight(300.0, allocatedSlot, "An explicit 300px height must remain in effect")
            assertHeight(300.0, display, "The display must honor its explicit modifier height")
            assertHeight(300.0, section, "The entry must honor its explicit modifier height")
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}

@Composable
private fun ScreenWithOwnedScroll() {
    Div(attrs = {
        attr("data-screen-root", "")
        style {
            property("display", "flex")
            property("flex", "1")
            property("flex-direction", "column")
            property("min-height", "0")
        }
    }) {
        Div(attrs = {
            attr("data-screen-scroll", "")
            style {
                property("flex", "1")
                property("min-height", "0")
                property("overflow-y", "auto")
            }
        }) {
            Div(attrs = { style { property("height", "1200px") } }) {}
        }
    }
}

@Composable
private fun displayHost(
    width: Int = 600,
    height: Int = 600,
    headerHeight: Int = 100,
    content: @Composable () -> Unit,
) {
    Div(attrs = {
        attr("data-host", "")
        style {
            property("width", "${width}px")
            property("height", "${height}px")
            property("display", "flex")
            property("flex-direction", "column")
            property("overflow", "hidden")
        }
    }) {
        Div(attrs = {
            style {
                property("height", "${headerHeight}px")
                property("flex", "none")
            }
        }) {}
        content()
    }
}

private fun assertHeight(
    expected: Double,
    element: HTMLElement,
    message: String,
) {
    val actual = element.getBoundingClientRect().height
    assertTrue(abs(actual - expected) < 1, "$message: expected $expected but got $actual")
}

private fun assertWidth(
    expected: Double,
    element: HTMLElement,
    message: String,
) {
    val actual = element.getBoundingClientRect().width
    assertTrue(abs(actual - expected) < 1, "$message: expected $expected but got $actual")
}
