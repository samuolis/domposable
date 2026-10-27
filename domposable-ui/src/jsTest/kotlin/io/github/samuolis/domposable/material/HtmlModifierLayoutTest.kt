package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableImage
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.background
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxSize
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.htmlAttributes
import io.github.samuolis.domposable.ui.maxHeight
import io.github.samuolis.domposable.ui.minHeight
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.rememberDomposableScrollState
import io.github.samuolis.domposable.ui.rotate
import io.github.samuolis.domposable.ui.scale
import io.github.samuolis.domposable.ui.shadow
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.verticalScroll
import io.github.samuolis.domposable.ui.width
import io.github.samuolis.domposable.ui.wrapContentSize
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlModifierLayoutTest {
    @Test
    fun fillMaxSizeImageUsesTheBoundedBoxHeightThroughWidthWrapper() {
        val cases =
            listOf(
                Triple("bounded", DomposableModifier.fillMaxSize(), 240),
                Triple("padded", DomposableModifier.fillMaxWidth().padding(DomposableLength(10f)).fillMaxHeight(), 220),
                Triple("fixed", DomposableModifier.height(DomposableLength(100f)).fillMaxHeight(), 100),
                Triple(
                    "minimum",
                    DomposableModifier.fillMaxWidth().minHeight(DomposableLength(100f)).fillMaxHeight(),
                    240,
                ),
                Triple(
                    "maximum",
                    DomposableModifier.fillMaxWidth().maxHeight(DomposableLength(100f)).fillMaxHeight(),
                    100,
                ),
            )
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    cases.forEach { (name, modifier, _) ->
                        DomposableBox(DomposableModifier.height(DomposableLength(240f))) {
                            DomposableImage(INTRINSIC_IMAGE, null, modifier.testId("$name-image"))
                        }
                    }
                }
            }
        try {
            cases.forEach { (name, _, expectedHeight) ->
                val image = root.querySelector("[data-testid=$name-image]") as HTMLElement
                assertEquals(expectedHeight, image.offsetHeight, "$name image height")
            }
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

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

    @Test
    fun verticalScrollKeepsShortAndTallContentAtTheViewportStart() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition = renderComposable(root) { ScrollConstraintFixture() }

        try {
            assertVerticalScrollLayout(root, "short-scroll-content", "Scroll", 0, tall = false)
            assertVerticalScrollLayout(root, "tall-scroll-content", "Scroll", 1, tall = true)
            assertVerticalScrollLayout(root, "stateful-short-scroll-content", "StatefulScroll", 0, tall = false)
            assertVerticalScrollLayout(root, "stateful-tall-scroll-content", "StatefulScroll", 1, tall = true)
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

@Composable
private fun ScrollConstraintFixture() {
    DomposableMaterialTheme {
        StatelessScrollExamples()
        StatefulScrollExamples()
    }
}

@Composable
private fun StatelessScrollExamples() {
    DomposableBox(
        modifier =
            DomposableModifier
                .height(DomposableLength(48f))
                .verticalScroll()
                .wrapContentSize()
                .background(DomposableColor(0xFFECEFF8))
                .testId("short-scroll-content"),
    ) { DomposableText("Short content") }
    DomposableColumn(
        modifier =
            DomposableModifier
                .height(DomposableLength(48f))
                .verticalScroll()
                .wrapContentSize()
                .background(DomposableColor(0xFFECEFF8))
                .testId("tall-scroll-content"),
    ) {
        repeat(8) { index ->
            DomposableText(
                "Tall row $index",
                modifier = DomposableModifier.height(DomposableLength(24f)),
            )
        }
    }
}

@Composable
private fun StatefulScrollExamples() {
    val shortScrollState = rememberDomposableScrollState()
    val tallScrollState = rememberDomposableScrollState()
    DomposableBox(
        modifier =
            DomposableModifier
                .height(DomposableLength(48f))
                .verticalScroll(shortScrollState)
                .wrapContentSize()
                .background(DomposableColor(0xFFECEFF8))
                .testId("stateful-short-scroll-content"),
    ) { DomposableText("Short stateful content") }
    DomposableColumn(
        modifier =
            DomposableModifier
                .maxHeight(DomposableLength(48f))
                .verticalScroll(tallScrollState)
                .wrapContentSize()
                .background(DomposableColor(0xFFECEFF8))
                .testId("stateful-tall-scroll-content"),
    ) {
        repeat(8) { index ->
            DomposableText(
                "Stateful tall row $index",
                modifier = DomposableModifier.height(DomposableLength(24f)),
            )
        }
    }
}

private fun assertVerticalScrollLayout(
    root: Element,
    testId: String,
    operation: String,
    index: Int,
    tall: Boolean,
) {
    val content = root.querySelector("[data-testid=$testId]") as HTMLElement
    val scrollport = root.querySelectorAll("[data-domposable-modifier=$operation]").item(index) as HTMLElement
    val background = content.parentElement as HTMLElement
    val offset = background.getBoundingClientRect().top - scrollport.getBoundingClientRect().top
    val maxScrollTop = scrollport.scrollHeight - scrollport.clientHeight
    scrollport.scrollTop = maxScrollTop.toDouble()
    val reachedScrollEnd = kotlin.math.abs(scrollport.scrollTop - maxScrollTop) < 1
    scrollport.scrollTop = 0.0
    val hasExpectedScrollRange = !tall || (maxScrollTop > 0 && reachedScrollEnd)

    assertEquals(48, scrollport.clientHeight)
    assertTrue(
        offset >= -1 && hasExpectedScrollRange,
        "$testId offset=$offset, scrollHeight=${scrollport.scrollHeight}",
    )
}

private const val INTRINSIC_IMAGE =
    "data:image/svg+xml," +
        "%3Csvg xmlns='http://www.w3.org/2000/svg' width='700' height='700'%3E%3C/svg%3E"
