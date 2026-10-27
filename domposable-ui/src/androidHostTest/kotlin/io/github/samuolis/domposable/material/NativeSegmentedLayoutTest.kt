package io.github.samuolis.domposable.material

import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableItemColors
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableSegmentedButtons
import io.github.samuolis.domposable.ui.DomposableSegmentedButtonsProps
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w390dp-h500dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeSegmentedLayoutTest {
    @get:Rule val compose = createComposeRule()

    private fun widths(equal: Boolean): List<Float> {
        compose.setContent {
            DomposableMaterialTheme {
                DomposableSegmentedButtons(
                    DomposableSegmentedButtonsProps(
                        labels = listOf("A", "Longer option", "B"),
                        selectedIndex = 0,
                        onSelect = {},
                        label = "Choices",
                        equalWidth = equal,
                        optionTestTags = listOf("first", "middle", "last"),
                    ),
                )
            }
        }
        return listOf("first", "middle", "last").map {
            compose
                .onNodeWithTag(it)
                .fetchSemanticsNode()
                .boundsInRoot.width
        }
    }

    @Test
    fun intrinsicSegmentsKeepTheirDifferentContentWidths() {
        val widths = widths(false)
        assertTrue(widths[1] > widths[0])
        assertTrue(widths[1] > widths[2])
    }

    @Test
    fun equalSegmentsReserveTheSameWidthForEveryOption() {
        val widths = widths(true)
        assertEquals(widths[0], widths[1], 1f)
        assertEquals(widths[0], widths[2], 1f)
    }

    @Test
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
    fun intrinsicSegmentsKeepMultiwordLabelsOnOneLineWhenSpaceAllows() {
        compose.setContent {
            DomposableMaterialTheme {
                DomposableSegmentedButtons(
                    DomposableSegmentedButtonsProps(
                        labels = listOf("Standard", "Express", "Same day"),
                        selectedIndex = 0,
                        onSelect = {},
                        label = "Delivery speed",
                        equalWidth = false,
                        optionTestTags = listOf("first", "middle", "last"),
                        appearance =
                            DomposableItemAppearance(
                                colors =
                                    DomposableItemColors(
                                        border = DomposableColor(0xFF0000FF),
                                        selectedBorder = DomposableColor(0xFFFF0000),
                                    ),
                                selectedBorderWidth = 3.dp,
                            ),
                    ),
                )
            }
        }
        val layouts = mutableListOf<TextLayoutResult>()
        compose
            .onNodeWithText("Same day", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertEquals(1, layouts.single().lineCount)
        val selected = compose.onNodeWithTag("first").captureToImage().toPixelMap()
        val seam = selected[selected.width - 1, selected.height / 2]
        assertTrue(seam.red > seam.blue, "The selected border must paint over its neighbour")
    }
}
