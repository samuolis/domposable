package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableFontWeight
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.DomposableTypography
import io.github.samuolis.domposable.foundation.DomposableTypographyRole
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.htmlAttributes
import io.github.samuolis.domposable.ui.testId
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    fun inheritedTextUsesHostTypographyAndKeepsExplicitAppearanceOverrides() {
        val root = document.createElement("div") as HTMLElement
        val styles = document.createElement("style")
        styles.textContent =
            ".host-typography { font-size: var(--host-size); line-height: var(--host-line); font-weight: var(--host-weight); }"
        document.head!!.appendChild(styles)
        root.style.setProperty("--host-size", "13px")
        root.style.setProperty("--host-line", "19px")
        root.style.setProperty("--host-weight", "300")
        document.body!!.appendChild(root)
        val modifier = DomposableModifier.htmlAttributes { classes("host-typography") }
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableText(
                        "Inherited",
                        modifier = modifier.testId("inherited"),
                        style = DomposableTextStyle.Inherit,
                    )
                    DomposableText(
                        "Inherited heading",
                        modifier = modifier.testId("inherited-heading"),
                        style = DomposableTextStyle.Inherit,
                        headingLevel = 3,
                    )
                    DomposableText(
                        "Overridden",
                        modifier = modifier.testId("overridden"),
                        style = DomposableTextStyle.Inherit,
                        appearance =
                            DomposableTextAppearance(
                                fontSize = 22f,
                                lineHeight = 27f,
                                fontWeight = DomposableFontWeight.Bold,
                            ),
                    )
                }
            }
        try {
            val inherited = root.querySelector("[data-testid=inherited]") as HTMLElement
            val heading = root.querySelector("[data-testid=inherited-heading]") as HTMLElement
            val overridden = root.querySelector("[data-testid=overridden]") as HTMLElement
            assertEquals("H3", heading.tagName)
            assertTrue(heading.classList.contains("d-text"))
            assertTrue(!heading.classList.contains("d-title"))
            assertComputedTypography(inherited, "13px", "19px", "300")
            assertComputedTypography(heading, "13px", "19px", "300")
            root.style.setProperty("--host-size", "16px")
            root.style.setProperty("--host-line", "24px")
            root.style.setProperty("--host-weight", "500")
            assertComputedTypography(inherited, "16px", "24px", "500")
            assertComputedTypography(heading, "16px", "24px", "500")
            assertComputedTypography(overridden, "22px", "27px", "700")
        } finally {
            composition.dispose()
            styles.parentNode?.removeChild(styles)
            root.parentNode?.removeChild(root)
        }
    }

    private fun assertComputedTypography(
        element: HTMLElement,
        fontSize: String,
        lineHeight: String,
        fontWeight: String,
    ) {
        val style = window.getComputedStyle(element)
        assertEquals(fontSize, style.fontSize)
        assertEquals(lineHeight, style.lineHeight)
        assertEquals(fontWeight, style.fontWeight)
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
