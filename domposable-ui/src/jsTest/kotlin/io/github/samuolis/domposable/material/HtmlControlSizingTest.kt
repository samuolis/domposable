package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableLink
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableSegmentedButtons
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.testId
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlControlSizingTest {
    @Test
    fun buttonAndItsTouchTargetFillBoundedWidth() =
        assertFullWidth { modifier ->
            DomposableButton(text = "Action", onClick = {}, modifier = modifier)
        }

    @Test
    fun styledAnchorFillsBoundedWidth() =
        assertFullWidth { modifier ->
            DomposableLink(
                label = "Destination",
                href = "/destination",
                modifier = modifier,
                buttonAppearance = DomposableButtonAppearance(),
            )
        }

    @Test
    fun plainAnchorFillsBoundedWidthWhenRequested() =
        assertFullWidth { modifier ->
            DomposableLink(label = "Destination", href = "/destination", modifier = modifier)
        }

    @Test
    fun equalWidthSegmentsFillBoundedWidthWhenRequested() =
        assertFullWidth(selector = ".d-segments") { modifier ->
            DomposableSegmentedButtons(
                labels = listOf("One", "Two"),
                selectedIndex = 0,
                onSelect = {},
                label = "Mode",
                modifier = modifier,
                equalWidth = true,
            )
        }

    @Test
    fun narrowEqualWidthSegmentsKeepFullLabelsOnOneLineAndScrollHorizontally() {
        val root = document.createElement("div") as HTMLElement
        root.style.width = "320px"
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableSegmentedButtons(
                        labels = listOf("Fiksuotas sezonas", "Paskutiniai 12 mėnesių", "Visas laikotarpis"),
                        selectedIndex = 0,
                        onSelect = {},
                        label = "Įskaitomi rezultatai",
                        modifier = DomposableModifier.fillMaxWidth(),
                        equalWidth = true,
                    )
                }
            }
        try {
            val group = root.querySelector(".d-segments") as HTMLElement
            assertEquals("radiogroup", group.getAttribute("role"))
            assertEquals(3, group.querySelectorAll("input[type=radio]").length)
            assertTrue(group.scrollWidth > group.clientWidth)
            assertEquals("auto", window.getComputedStyle(group).overflowX)
            val segments = group.querySelectorAll(".d-segment")
            val first = (segments.item(0) as HTMLElement).getBoundingClientRect()
            for (index in 0 until 3) {
                val segment = segments.item(index) as HTMLElement
                val surface = segment.querySelector(".d-segment-surface") as HTMLElement
                val label = segment.querySelector(".d-segment-label") as HTMLElement
                val bounds = segment.getBoundingClientRect()
                val surfaceBounds = surface.getBoundingClientRect()
                val labelBounds = label.getBoundingClientRect()
                assertEquals("nowrap", window.getComputedStyle(surface).whiteSpace)
                assertTrue(abs(bounds.width - first.width) <= 1.1)
                assertTrue(labelBounds.left >= surfaceBounds.left && labelBounds.right <= surfaceBounds.right)
                if (index > 0) {
                    val previous = (segments.item(index - 1) as HTMLElement).getBoundingClientRect()
                    assertTrue(bounds.left >= previous.right - 1.1)
                }
            }
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun fullWidthSegmentRadiosOwnTheirPointerTarget() {
        for (enabled in listOf(true, false)) {
            var selected = -1
            withHost(content = {
                DomposableSegmentedButtons(
                    labels = listOf("One", "Two"),
                    selectedIndex = 0,
                    onSelect = { selected = it },
                    label = "Mode",
                    modifier = DomposableModifier.fillMaxWidth(),
                    enabled = enabled,
                    equalWidth = true,
                )
            }) { root ->
                val input = root.querySelectorAll(".d-segment input").item(1) as HTMLElement
                val bounds = input.getBoundingClientRect()
                val label = requireNotNull(input.parentElement).getBoundingClientRect()
                assertTrue(abs(bounds.width - label.width) < 1.0)
                assertTrue(abs(bounds.height - label.height) < 1.0)
                assertEquals(
                    input,
                    document.elementFromPoint(bounds.left + bounds.width / 2, bounds.top + bounds.height / 2),
                )
                input.click()
                assertEquals(if (enabled) 1 else -1, selected)
            }
        }
    }

    private fun assertFullWidth(
        selector: String = "[data-testid=control]",
        content: @Composable (DomposableModifier) -> Unit,
    ) {
        val root = document.createElement("div") as HTMLElement
        root.style.width = "320px"
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    content(DomposableModifier.fillMaxWidth().testId("control"))
                }
            }
        try {
            val control = root.querySelector(selector) as HTMLElement
            assertTrue(abs(control.getBoundingClientRect().width - root.getBoundingClientRect().width) < 1.0)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
