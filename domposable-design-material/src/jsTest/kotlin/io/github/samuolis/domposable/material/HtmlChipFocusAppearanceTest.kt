package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.ui.DomposableChip
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.testId
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlChipFocusAppearanceTest {
    @Test
    fun keyboardFocusRingFollowsTheChipSurfaceWithinItsLargerHitTarget() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    InteractiveChip()
                }
            }
        try {
            val chip = root.querySelector("[data-testid=interactive-chip]") as org.w3c.dom.HTMLElement
            val visual = chip.querySelector(".d-chip-surface") as org.w3c.dom.HTMLElement
            assertTrue(chip.classList.contains("d-item-hittarget"))
            assertEquals("button", chip.tagName.lowercase())
            chip.focus()
            assertTrue(chip.matches(":focus-visible"), "Programmatic initial focus should use keyboard focus styling")

            val target = kotlinx.browser.window.getComputedStyle(chip)
            val targetBounds = chip.getBoundingClientRect()
            val visualStyle = kotlinx.browser.window.getComputedStyle(visual)
            val visualBounds = visual.getBoundingClientRect()
            val colorProbe = document.createElement("span") as org.w3c.dom.HTMLElement
            colorProbe.style.color = "var(--d-primary)"
            chip.parentNode!!.appendChild(colorProbe)
            val primary =
                kotlinx.browser.window
                    .getComputedStyle(colorProbe)
                    .color
            assertTrue(primary.isNotEmpty())
            assertEquals("none", target.outlineStyle)
            assertTrue(targetBounds.height >= 48.0)
            assertEquals("solid", visualStyle.outlineStyle)
            assertEquals("3px", visualStyle.outlineWidth)
            assertEquals(primary, visualStyle.outlineColor)
            assertEquals("8px", visualStyle.borderRadius)
            assertTrue(visualBounds.width < targetBounds.width || visualBounds.height < targetBounds.height)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Composable
    private fun InteractiveChip() {
        DomposableChip(
            label = "Filters",
            onClick = {},
            modifier = DomposableModifier.testId("interactive-chip"),
            appearance = DomposableItemAppearance(minimumInteractiveSize = DomposableLength(48f)),
        )
    }
}
