package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableOffset
import io.github.samuolis.domposable.ui.DomposableTabItem
import io.github.samuolis.domposable.ui.DomposableTabs
import io.github.samuolis.domposable.ui.DomposableTabsActivation
import io.github.samuolis.domposable.ui.DomposableTabsAppearance
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.KeyboardEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HtmlTabsTest {
    @Test
    fun keyedTabsExposeLocalizedSemanticsAndKeyboardSkipsDisabledItems() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val selected = mutableListOf<Int>()
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableTabs(
                        items =
                            listOf(
                                DomposableTabItem("overview", "Overview", "Overview section", testTag = "tab-overview"),
                                DomposableTabItem("hidden", "Hidden", enabled = false),
                                DomposableTabItem("activity", "Activity", "Recent activity", testTag = "tab-activity"),
                            ),
                        selectedIndex = 0,
                        onSelect = selected::add,
                        label = "Competition sections",
                    )
                }
            }
        try {
            val list = root.querySelector("[role=tablist]") as HTMLElement
            assertEquals("Competition sections", list.getAttribute("aria-label"))
            val overview = root.querySelector("[data-testid=tab-overview]") as HTMLElement
            val disabled = root.querySelectorAll("[role=tab]").item(1) as HTMLElement
            val activity = root.querySelector("[data-testid=tab-activity]") as HTMLElement
            assertEquals("Overview section", overview.getAttribute("aria-label"))
            assertEquals(true, disabled.hasAttribute("disabled"))
            assertEquals("0", overview.getAttribute("tabindex"))

            overview.focus()
            overview.dispatchEvent(key("ArrowRight"))
            assertEquals(activity, document.activeElement)
            assertEquals("2", activity.getAttribute("data-tab-index"))
            assertEquals(listOf(2), selected)

            activity.dispatchEvent(key("Home"))
            assertEquals(overview, document.activeElement)
            assertEquals(listOf(2, 0), selected)

            overview.dispatchEvent(key("End"))
            assertEquals(activity, document.activeElement)
            assertEquals(listOf(2, 0, 2), selected)
            assertNotNull(activity.getAttribute("data-testid"))
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun configuredTabsPreserveManualActivationAndPortableGeometry() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val selected = mutableListOf<Int>()
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableTabs(
                        items =
                            listOf(
                                DomposableTabItem("first", "Duplicate", badgeContent = { Text("7") }),
                                DomposableTabItem("disabled", "Locked", enabled = false),
                                DomposableTabItem("last", "Duplicate"),
                            ),
                        selectedIndex = 0,
                        onSelect = selected::add,
                        scrollable = true,
                        tabsAppearance =
                            DomposableTabsAppearance(
                                minHeight = DomposableLength(48f),
                                contentPadding =
                                    DomposableInsets(
                                        top = DomposableLength(10f),
                                        end = DomposableLength(16f),
                                        bottom = DomposableLength(10f),
                                        start = DomposableLength(16f),
                                    ),
                                indicatorThickness = DomposableLength(2f),
                                scrollableEdgePadding = DomposableLength(0f),
                                disabledOpacity = 0.55f,
                                activation = DomposableTabsActivation.Manual,
                                badgeClearance = DomposableLength(16f),
                                badgeOffset = DomposableOffset(8f, -4f),
                            ),
                    )
                }
            }
        try {
            val list = root.querySelector("[role=tablist]") as HTMLElement
            val first = root.querySelectorAll("[role=tab]").item(0) as HTMLElement
            val disabled = root.querySelectorAll("[role=tab]").item(1) as HTMLElement
            val badge = root.querySelector(".d-tab-badge") as HTMLElement
            first.focus()
            first.dispatchEvent(key("ArrowRight"))
            assertEquals("2", (document.activeElement as HTMLElement).getAttribute("data-tab-index"))
            assertEquals(emptyList(), selected)
            assertEquals("2px", list.style.getPropertyValue("--d-tabs-indicator-height"))
            assertEquals("0px", list.style.getPropertyValue("padding-inline"))
            assertEquals("0.55", disabled.style.opacity)
            assertEquals(true, badge.style.transform.contains("8px"))
            assertEquals(true, badge.style.transform.contains("-4px"))
            list.setAttribute("dir", "rtl")
            (document.activeElement as HTMLElement).dispatchEvent(key("ArrowLeft"))
            assertEquals(first, document.activeElement)
            assertEquals(emptyList(), selected)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    private fun key(name: String): KeyboardEvent {
        val createKeyboardEvent = js("(key) => new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true })")
        return createKeyboardEvent(name) as KeyboardEvent
    }
}
