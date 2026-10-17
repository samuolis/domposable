package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableFontWeight
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.DomposableTypography
import io.github.samuolis.domposable.foundation.DomposableTypographyRole
import io.github.samuolis.domposable.ui.DomposableText
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlTypographyParityTest {
    @Test
    fun headingsUseTheirTextRoleInsteadOfBrowserHeadingDefaults() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableText("Title", style = DomposableTextStyle.Title, headingLevel = 1)
                    DomposableText("Body heading", headingLevel = 4)
                }
            }
        try {
            val title = window.getComputedStyle(root.querySelector("h1") as HTMLElement)
            assertEquals("24px", title.fontSize)
            assertEquals("28px", title.lineHeight)
            assertEquals("400", title.fontWeight)
            val body = window.getComputedStyle(root.querySelector("h4") as HTMLElement)
            assertEquals("16px", body.fontSize)
            assertEquals("24px", body.lineHeight)
            assertEquals("0px", body.marginTop)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun partialRoleOverridesRetainTheMaterialLineHeightAndSize() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme(
                    typography =
                        DomposableTypography(
                            roles =
                                mapOf(
                                    DomposableTypographyRole.TitleLarge to
                                        DomposableTextAppearance(fontWeight = DomposableFontWeight.Bold),
                                ),
                        ),
                ) {
                    DomposableText("Title", style = DomposableTextStyle.Title)
                }
            }
        try {
            val title = window.getComputedStyle(root.querySelector("h2") as HTMLElement)
            assertEquals("24px", title.fontSize)
            assertEquals("28px", title.lineHeight)
            assertEquals("700", title.fontWeight)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
