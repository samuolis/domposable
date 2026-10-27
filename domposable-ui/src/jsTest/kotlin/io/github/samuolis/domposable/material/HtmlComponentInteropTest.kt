package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableColors
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.ui.DomposableFloatingToolbar
import io.github.samuolis.domposable.ui.DomposableFloatingToolbarVariant
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTabItem
import io.github.samuolis.domposable.ui.DomposableTabs
import io.github.samuolis.domposable.ui.testId
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlComponentInteropTest {
    @Test
    fun customTabLabelPreservesAccessibleNameAndItemModifier() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableTabs(
                        items =
                            listOf(
                                DomposableTabItem(
                                    key = "groups",
                                    label = "Groups",
                                    contentDescription = "Tournament groups",
                                    labelContent = { Text("Groups 4") },
                                    modifier = DomposableModifier.testId("custom-tab"),
                                ),
                            ),
                        selectedIndex = 0,
                        onSelect = {},
                    )
                }
            }
        try {
            val tab = root.querySelector("[data-testid=custom-tab]") as HTMLElement
            assertEquals("Tournament groups", tab.getAttribute("aria-label"))
            assertEquals("Groups 4", tab.textContent)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun vibrantToolbarUsesThePrimaryContainerAndStandardPadding() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme(
                    tokens =
                        DomposableTokens(
                            colors =
                                DomposableColors(
                                    primaryContainer = DomposableColor(0xFFFF0000),
                                    surfaceContainerHigh = DomposableColor(0xFF00FF00),
                                ),
                        ),
                ) {
                    DomposableFloatingToolbar(
                        expanded = true,
                        variant = DomposableFloatingToolbarVariant.Vibrant,
                    ) { Text("Actions") }
                }
            }
        try {
            val toolbar = root.querySelector(".d-floating-toolbar") as HTMLElement
            val style = window.getComputedStyle(toolbar)
            assertEquals("rgb(255, 0, 0)", style.backgroundColor)
            assertEquals("8px", style.paddingTop)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
