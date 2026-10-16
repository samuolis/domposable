package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.background
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.htmlAttributes
import io.github.samuolis.domposable.ui.maxHeight
import io.github.samuolis.domposable.ui.minHeight
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.rotate
import io.github.samuolis.domposable.ui.scale
import io.github.samuolis.domposable.ui.shadow
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.width
import io.github.samuolis.domposable.ui.wrapContentSize
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlModifierLayoutTest {
    @Test
    fun fixedHeightFlowsThroughGraphicsWrappersToTheBackgroundAndContent() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                PortableGraphicsFixture()
            }

        try {
            val content = root.querySelector("[data-testid=portable-graphics]") as HTMLElement
            val innerBox = root.querySelector("[data-testid=inner-box]") as HTMLElement
            val padding = root.querySelector("[data-domposable-modifier=Padding]") as HTMLElement
            val width = padding.firstElementChild as HTMLElement
            val background = root.querySelector("[data-domposable-modifier=ShapeBackground]") as HTMLElement
            val shadow = root.querySelector("[data-domposable-modifier=Shadow]") as HTMLElement

            assertEquals("48px", window.getComputedStyle(content).height)
            assertEquals("48px", window.getComputedStyle(background).height)
            assertEquals("48px", window.getComputedStyle(shadow).height)
            assertEquals(48, background.offsetHeight)
            assertEquals(48, shadow.offsetHeight)
            assertEquals(160, background.offsetWidth)
            assertEquals("Height", width.firstElementChild?.getAttribute("data-domposable-modifier"))
            assertEquals(
                "Shadow",
                width.firstElementChild?.firstElementChild?.getAttribute("data-domposable-modifier"),
            )
            val outerBounds = content.getBoundingClientRect()
            val innerBounds = innerBox.getBoundingClientRect()
            val horizontalCenterDelta =
                (innerBounds.left + innerBounds.right) / 2 - (outerBounds.left + outerBounds.right) / 2
            val verticalCenterDelta =
                (innerBounds.top + innerBounds.bottom) / 2 - (outerBounds.top + outerBounds.bottom) / 2
            assertTrue(kotlin.math.abs(horizontalCenterDelta) < 1)
            assertTrue(kotlin.math.abs(verticalCenterDelta) < 1)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun minimumHeightFlowsToGraphicsAndWrapContentStopsThePropagation() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                ConstraintFixture()
            }

        try {
            val minHeightBackground =
                root.querySelector("[data-testid=minimum-height]")!!.parentElement as HTMLElement
            val wrapHeight = root.querySelector("[data-domposable-modifier=Height]") as HTMLElement
            val wrapBackground =
                root.querySelector("[data-testid=wrap-content]")!!.parentElement as HTMLElement
            val innerPaddingBackground =
                root.querySelector("[data-testid=inner-padding]")!!.parentElement as HTMLElement
            val innerPadding = innerPaddingBackground.parentElement as HTMLElement
            val fillHeight = root.querySelector("[data-testid=fill-height]")!!.parentElement as HTMLElement
            val maxHeightBackground =
                root.querySelector("[data-testid=max-height]")!!.parentElement as HTMLElement

            assertEquals(48, minHeightBackground.offsetHeight)
            assertEquals(48, innerPadding.offsetHeight)
            assertEquals(32, innerPaddingBackground.offsetHeight)
            assertEquals(48, wrapHeight.offsetHeight)
            assertTrue(wrapBackground.offsetHeight < wrapHeight.offsetHeight)
            assertEquals(48, fillHeight.offsetHeight)
            assertTrue(maxHeightBackground.offsetHeight in 1..48)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}

@Composable
private fun PortableGraphicsFixture() {
    DomposableMaterialTheme {
        DomposableBox(
            modifier =
                DomposableModifier
                    .padding(DomposableLength(12f))
                    .size(DomposableLength(160f), DomposableLength(48f))
                    .shadow(DomposableLength(4f), DomposableShape.Rounded(DomposableLength(8f)))
                    .rotate(5f)
                    .scale(0.9f)
                    .background(DomposableColor(0xFFECEFF8), DomposableShape.Rounded(DomposableLength(8f)))
                    .testId("portable-graphics"),
            alignment = DomposableAlignment.Center,
        ) {
            DomposableBox(
                modifier =
                    DomposableModifier
                        .size(DomposableLength(20f))
                        .background(DomposableColor(0xFF4059AD))
                        .testId("inner-box"),
            ) { DomposableText("Shared transforms") }
        }
    }
}

@Composable
private fun ConstraintFixture() {
    DomposableMaterialTheme {
        DomposableBox(
            modifier =
                DomposableModifier
                    .minHeight(DomposableLength(48f))
                    .background(DomposableColor(0xFFECEFF8))
                    .testId("minimum-height"),
        ) { DomposableText("Minimum height") }
        DomposableBox(
            modifier =
                DomposableModifier
                    .height(DomposableLength(48f))
                    .padding(DomposableLength(8f))
                    .background(DomposableColor(0xFFECEFF8))
                    .testId("inner-padding"),
        ) { DomposableText("Inner padding") }
        DomposableBox(
            modifier =
                DomposableModifier
                    .height(DomposableLength(48f))
                    .wrapContentSize()
                    .background(DomposableColor(0xFFECEFF8))
                    .testId("wrap-content"),
        ) { DomposableText("Wrap content") }
        DomposableBox(
            modifier =
                DomposableModifier
                    .htmlAttributes { style { property("height", "48px") } }
                    .testId("fill-parent"),
        ) {
            DomposableBox(
                modifier =
                    DomposableModifier
                        .fillMaxHeight()
                        .background(DomposableColor(0xFFECEFF8))
                        .testId("fill-height"),
            ) { DomposableText("Fill height") }
        }
        DomposableBox(
            modifier =
                DomposableModifier
                    .width(DomposableLength(80f))
                    .maxHeight(DomposableLength(48f))
                    .background(DomposableColor(0xFFECEFF8))
                    .testId("max-height"),
        ) { DomposableText("Long content ".repeat(20)) }
    }
}
