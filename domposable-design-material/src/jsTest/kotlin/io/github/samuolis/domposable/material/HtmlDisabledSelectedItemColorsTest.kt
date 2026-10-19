package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableChipVariant
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableItemColors
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.ui.DomposableChip
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableSegmentedButtons
import io.github.samuolis.domposable.ui.testId
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlDisabledSelectedItemColorsTest {
    @Test
    fun disabledSegmentsKeepIndependentSelectedAndUnselectedColors() {
        withHost(content = {
            DomposableSegmentedButtons(
                labels = listOf("One", "Two"),
                selectedIndex = 1,
                onSelect = {},
                label = "Mode",
                enabled = false,
                appearance =
                    DomposableItemAppearance(
                        colors =
                            DomposableItemColors(
                                disabledContainer = DomposableColor(0xFF112233),
                                disabledContent = DomposableColor(0xFF223344),
                                disabledBorder = DomposableColor(0xFF334455),
                                disabledSelectedContainer = DomposableColor(0xFFAA0000),
                                disabledSelectedContent = DomposableColor(0xFF00AA00),
                                disabledSelectedBorder = DomposableColor(0xFF0000AA),
                            ),
                    ),
            )
        }) { root ->
            val surfaces = root.querySelectorAll(".d-segment-surface")
            val unselected = window.getComputedStyle(surfaces.item(0) as HTMLElement)
            val selected = window.getComputedStyle(surfaces.item(1) as HTMLElement)
            assertEquals("rgb(17, 34, 51)", unselected.backgroundColor)
            assertEquals("rgb(34, 51, 68)", unselected.color)
            assertEquals("rgb(51, 68, 85)", unselected.borderTopColor)
            assertEquals("rgb(170, 0, 0)", selected.backgroundColor)
            assertEquals("rgb(0, 170, 0)", selected.color)
            assertEquals("rgb(0, 0, 170)", selected.borderTopColor)
        }
    }

    @Test
    fun disabledSelectedChipUsesItsSpecificColorsAndFallsBackToDisabledColors() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableChip(
                        label = "Selected custom",
                        onClick = {},
                        modifier = DomposableModifier.testId("disabled-selected-custom"),
                        selected = true,
                        enabled = false,
                        variant = DomposableChipVariant.Filter,
                        appearance =
                            DomposableItemAppearance(
                                minimumInteractiveSize = DomposableLength(0f),
                                colors =
                                    DomposableItemColors(
                                        disabledContainer = DomposableColor(0xFF112233),
                                        disabledContent = DomposableColor(0xFF223344),
                                        disabledBorder = DomposableColor(0xFF334455),
                                        disabledSelectedContainer = DomposableColor(0xFFAA0000),
                                        disabledSelectedContent = DomposableColor(0xFF00AA00),
                                        disabledSelectedBorder = DomposableColor(0xFF0000AA),
                                    ),
                            ),
                    )
                    DomposableChip(
                        label = "Selected fallback",
                        onClick = {},
                        modifier = DomposableModifier.testId("disabled-selected-fallback"),
                        selected = true,
                        enabled = false,
                        variant = DomposableChipVariant.Filter,
                        appearance =
                            DomposableItemAppearance(
                                minimumInteractiveSize = DomposableLength(0f),
                                colors =
                                    DomposableItemColors(
                                        disabledContainer = DomposableColor(0xFF112233),
                                        disabledContent = DomposableColor(0xFF223344),
                                        disabledBorder = DomposableColor(0xFF334455),
                                    ),
                            ),
                    )
                }
            }
        try {
            val custom = root.querySelector("[data-testid=disabled-selected-custom]") as HTMLElement
            val fallback = root.querySelector("[data-testid=disabled-selected-fallback]") as HTMLElement
            assertEquals(true, custom.hasAttribute("disabled"))
            assertEquals("true", custom.getAttribute("data-selected"))
            assertEquals("rgb(170, 0, 0)", window.getComputedStyle(custom).backgroundColor)
            assertEquals("rgb(0, 170, 0)", window.getComputedStyle(custom).color)
            assertEquals("rgb(0, 0, 170)", window.getComputedStyle(custom).borderTopColor)
            assertEquals("rgb(17, 34, 51)", window.getComputedStyle(fallback).backgroundColor)
            assertEquals("rgb(34, 51, 68)", window.getComputedStyle(fallback).color)
            assertEquals("rgb(51, 68, 85)", window.getComputedStyle(fallback).borderTopColor)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
