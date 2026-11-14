package io.github.samuolis.domposable.ui

import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DomposablePillNavigationTest {
    @Test
    fun semanticNavigationKeepsBadgeAccessibleNameAndClick() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        var selected: String? = null
        val composition =
            renderComposable(root) {
                DomposablePillNavigationBar(
                    items =
                        listOf(
                            DomposablePillNavigationItem(
                                key = "inbox",
                                label = "Inbox",
                                icon = { DomposableText("Icon") },
                                badge = { DomposableText("3") },
                                contentDescription = "Inbox, 3 unread",
                                href = "/inbox",
                            ),
                            DomposablePillNavigationItem(
                                key = "blocked",
                                label = "Blocked",
                                icon = {},
                                enabled = false,
                            ),
                        ),
                    selectedKey = "inbox",
                    onSelect = { selected = it },
                    label = "Main navigation",
                )
            }
        try {
            val nav = assertNotNull(root.querySelector("nav"))
            assertEquals("Main navigation", nav.getAttribute("aria-label"))
            val link = assertNotNull(nav.querySelector("a")) as HTMLElement
            assertEquals("Inbox, 3 unread", link.getAttribute("aria-label"))
            assertEquals("page", link.getAttribute("aria-current"))
            assertNotNull(link.querySelector("[aria-hidden=true]"))
            link.click()
            assertEquals("inbox", selected)
            assertNotNull(nav.querySelector("button[disabled]"))
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
