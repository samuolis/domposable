package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableFontWeight
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTypographyRole
import io.github.samuolis.domposable.ui.DomposableText
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlTextAppearanceTest {
    @Test
    fun customTextHostSharesThemeMetricsWithStandardTextAndRespondsToThemeChanges() {
        val appearance = DomposableTextAppearance(role = DomposableTypographyRole.BodyMedium)
        withHost(content = {
            DomposableText("Standard", appearance = appearance)
            Span(attrs = { domposableTextAppearance(appearance) }) {
                Text("Custom ")
                Button { Text("More") }
            }
            Span(attrs = {
                domposableTextAppearance(appearance.copy(fontSize = 11f, fontWeight = DomposableFontWeight.Bold))
            }) { Text("Override") }
        }) { root ->
            val standard = root.querySelector(".d-text") as HTMLElement
            val custom = root.querySelectorAll("span").item(1) as HTMLElement
            val override = root.querySelectorAll("span").item(2) as HTMLElement
            val theme = root.firstElementChild as HTMLElement

            fun assertMatching() {
                val expected = window.getComputedStyle(standard)
                val actual = window.getComputedStyle(custom)
                assertEquals(expected.fontSize, actual.fontSize)
                assertEquals(expected.lineHeight, actual.lineHeight)
                assertEquals(expected.fontWeight, actual.fontWeight)
            }
            assertMatching()
            theme.style.setProperty("--d-type-body-medium-size", "28px")
            theme.style.setProperty("--d-type-body-medium-line-height", "34px")
            assertMatching()
            assertEquals("28px", window.getComputedStyle(custom).fontSize)
            assertEquals("34px", window.getComputedStyle(custom).lineHeight)
            assertEquals("11px", window.getComputedStyle(override).fontSize)
            assertEquals("700", window.getComputedStyle(override).fontWeight)
        }
    }
}
