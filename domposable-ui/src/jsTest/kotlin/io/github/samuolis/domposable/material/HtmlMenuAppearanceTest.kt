package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableDropdownMenuItem
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlMenuAppearanceTest {
    @Test
    fun itemAppearanceControlsPaddingHeightAndIconSpacing() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableDropdownMenuItem(
                        label = "Configured action",
                        onClick = {},
                        leadingIcon = { Span { Text("+") } },
                        trailingContent = { Span { Text(">") } },
                        appearance =
                            DomposableItemAppearance(
                                contentPadding =
                                    DomposableInsets(
                                        top = 5.dp,
                                        end = 11.dp,
                                        bottom = 9.dp,
                                        start = 13.dp,
                                    ),
                                minHeight = 72.dp,
                                contentSpacing = 7.dp,
                            ),
                    )
                }
            }
        try {
            val item = root.querySelector("[role=menuitem]") as HTMLElement
            val style = window.getComputedStyle(item)
            assertEquals(
                "5px 11px 9px 13px",
                "${style.paddingTop} ${style.paddingRight} ${style.paddingBottom} ${style.paddingLeft}",
            )
            assertEquals("7px", style.getPropertyValue("column-gap"))
            assertEquals(72.0, item.getBoundingClientRect().height)
            for (selector in listOf(".d-dropdown-menu-leading", ".d-dropdown-menu-trailing")) {
                val icon = root.querySelector(selector) as HTMLElement
                val iconStyle = window.getComputedStyle(icon)
                assertEquals("0", iconStyle.flexShrink)
                assertEquals("center", iconStyle.alignItems)
            }
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
