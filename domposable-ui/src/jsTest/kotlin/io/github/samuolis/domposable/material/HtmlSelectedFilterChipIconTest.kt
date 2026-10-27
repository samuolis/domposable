package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableChipVariant
import io.github.samuolis.domposable.ui.DomposableChip
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.testId
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlSelectedFilterChipIconTest {
    @Test
    fun selectedFilterChipOnlyShowsAnIconWhenOneIsProvided() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableChip(
                        label = "Selected without icon",
                        onClick = {},
                        modifier = DomposableModifier.testId("selected-filter-without-icon"),
                        selected = true,
                        variant = DomposableChipVariant.Filter,
                    )
                    DomposableChip(
                        label = "Selected with icon",
                        onClick = {},
                        modifier = DomposableModifier.testId("selected-filter-with-icon"),
                        selected = true,
                        variant = DomposableChipVariant.Filter,
                        leadingIcon = { DomposableText("✓") },
                    )
                }
            }
        try {
            val withoutIcon = root.querySelector("[data-testid=selected-filter-without-icon]") as HTMLElement
            val withIcon = root.querySelector("[data-testid=selected-filter-with-icon]") as HTMLElement
            assertEquals(
                "none",
                window.getComputedStyle(withoutIcon.querySelector(".d-chip-label") as HTMLElement, "::before").content,
            )
            assertEquals("✓", withIcon.querySelector(".d-chip-icon")?.textContent)
            assertEquals(
                "none",
                window.getComputedStyle(withIcon.querySelector(".d-chip-label") as HTMLElement, "::before").content,
            )
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
