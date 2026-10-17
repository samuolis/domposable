package io.github.samuolis.domposable.material

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.samuolis.domposable.ui.DomposableSegmentedButtons
import io.github.samuolis.domposable.ui.DomposableSegmentedButtonsProps
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
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
}
