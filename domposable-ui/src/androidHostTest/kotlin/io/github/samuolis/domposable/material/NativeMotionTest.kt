package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.ui.DomposableFloatingActionButton
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposablePullToRefresh
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w360dp-h640dp-mdpi")
class NativeMotionTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun extendedFabKeepsIntermediateWidthsWhenCollapsing() {
        var expanded by mutableStateOf(true)
        compose.setContent {
            DomposableMaterialTheme {
                DomposableFloatingActionButton(
                    {},
                    "Open timetable",
                    extended = expanded,
                    modifier = DomposableModifier.testId("fab"),
                ) { DomposableText("+") }
            }
        }
        val fab = compose.onNodeWithTag("fab")
        val expandedBounds = fab.getUnclippedBoundsInRoot()
        val expandedWidth = (expandedBounds.right - expandedBounds.left).value
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { expanded = false }
        val widths =
            List(20) {
                compose.mainClock.advanceTimeByFrame()
                compose.waitForIdle()
                val bounds = fab.getUnclippedBoundsInRoot()
                (bounds.right - bounds.left).value
            }
        assertTrue(
            widths.any { it > 56f && it < expandedWidth },
            "FAB must animate through intermediate widths: $expandedWidth -> $widths",
        )
        compose.mainClock.advanceTimeBy(1000)
        val collapsedBounds = fab.getUnclippedBoundsInRoot()
        assertEquals(56f, (collapsedBounds.right - collapsedBounds.left).value, .5f)
    }

    @Test
    fun customRefreshIndicatorFollowsTheDragAndRetractsOnRelease() {
        compose.setContent {
            DomposableMaterialTheme {
                Box(Modifier.size(240.dp, 400.dp)) {
                    DomposablePullToRefresh(
                        false,
                        {},
                        modifier = DomposableModifier.testId("pull"),
                        indicatorContent = {
                            Box(Modifier.size(40.dp).testTag("indicator"))
                        },
                    ) {
                        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            Box(Modifier.height(1000.dp))
                        }
                    }
                }
            }
        }
        val indicator = compose.onNodeWithTag("indicator")
        val restingTop = indicator.getUnclippedBoundsInRoot().top.value
        val pull = compose.onNodeWithTag("pull")
        pull.performTouchInput { down(center) }
        compose.mainClock.advanceTimeByFrame()
        pull.performTouchInput { moveBy(Offset(0f, 80f), delayMillis = 100) }
        val partialTop = indicator.getUnclippedBoundsInRoot().top.value
        pull.performTouchInput { moveBy(Offset(0f, 40f), delayMillis = 100) }
        val furtherTop = indicator.getUnclippedBoundsInRoot().top.value
        assertTrue(partialTop > restingTop, "Indicator must move down with the pull")
        assertTrue(furtherTop > partialTop, "Indicator must keep following the finger")
        pull.performTouchInput { up() }
        compose.mainClock.advanceTimeBy(1000)
        assertTrue(
            indicator.getUnclippedBoundsInRoot().bottom.value <= .5f,
            "An uncommitted pull must retract above the content",
        )
    }
}
