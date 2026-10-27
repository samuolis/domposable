package io.github.samuolis.domposable.material

import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableArrangement
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableFlowRow
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTheme
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.weight
import io.github.samuolis.domposable.ui.width
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeLayoutArrangementTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun spaceBetweenDistributesChildrenAcrossTheAvailableWidth() {
        compose.setContent {
            DomposableTheme {
                DomposableRow(
                    modifier = DomposableModifier.width(300.dp).height(40.dp).testId("row"),
                    gap = 12.dp,
                    arrangement = DomposableArrangement.SpaceBetween,
                ) {
                    repeat(3) { index ->
                        DomposableBox(DomposableModifier.width(40.dp).height(20.dp).testId("child-$index")) {}
                    }
                }
                DomposableFlowRow(
                    modifier = DomposableModifier.width(130.dp),
                    gap = 10.dp,
                    arrangement = DomposableArrangement.Start,
                    alignment = DomposableAlignment.End,
                    rowGap = 30.dp,
                ) {
                    DomposableBox(DomposableModifier.width(60.dp).height(40.dp).testId("flow-tall")) {}
                    DomposableBox(DomposableModifier.width(60.dp).height(20.dp).testId("flow-short")) {}
                    DomposableBox(DomposableModifier.width(60.dp).height(10.dp).testId("flow-next-line")) {}
                }
            }
        }

        val row = compose.onNodeWithTag("row").getUnclippedBoundsInRoot()
        val first = compose.onNodeWithTag("child-0").getUnclippedBoundsInRoot()
        val second = compose.onNodeWithTag("child-1").getUnclippedBoundsInRoot()
        val third = compose.onNodeWithTag("child-2").getUnclippedBoundsInRoot()
        val tall = compose.onNodeWithTag("flow-tall").getUnclippedBoundsInRoot()
        val short = compose.onNodeWithTag("flow-short").getUnclippedBoundsInRoot()
        val nextLine = compose.onNodeWithTag("flow-next-line").getUnclippedBoundsInRoot()

        assertEquals(row.left.value, first.left.value, 1f)
        assertEquals(row.right.value, third.right.value, 1f)
        assertEquals(90f, second.left.value - first.right.value, 1f)
        assertEquals(90f, third.left.value - second.right.value, 1f)
        assertEquals(70f, short.left.value - tall.left.value, 1f)
        assertEquals(20f, short.top.value - tall.top.value, 1f)
        assertEquals(70f, nextLine.top.value - tall.top.value, 1f)
    }

    @Test
    @Suppress("LongMethod")
    fun weightedRowsInStartAlignedPaddedBoxesUseOnlyFillWeightsToClaimAvailableWidth() {
        compose.setContent {
            DomposableTheme {
                DomposableColumn(modifier = DomposableModifier.width(400.dp).testId("parent-column")) {
                    DomposableBox(
                        DomposableModifier.padding(16.dp).testId("fill-box"),
                    ) {
                        DomposableRow(modifier = DomposableModifier.testId("fill-row")) {
                            DomposableText(
                                "Player name",
                                modifier = DomposableModifier.weight(1f).padding(4.dp).testId("fill-label"),
                            )
                            DomposableText("League points", modifier = DomposableModifier.testId("fill-metric"))
                        }
                    }
                    DomposableBox(
                        DomposableModifier.padding(16.dp).testId("nonfill-box"),
                    ) {
                        DomposableRow(modifier = DomposableModifier.testId("nonfill-row")) {
                            DomposableText(
                                "Player name",
                                modifier =
                                    DomposableModifier
                                        .weight(1f, fill = false)
                                        .padding(4.dp)
                                        .testId("nonfill-label"),
                            )
                            DomposableText("League points", modifier = DomposableModifier.testId("nonfill-metric"))
                        }
                    }
                    DomposableBox(DomposableModifier.padding(16.dp)) {
                        DomposableBox(DomposableModifier.width(200.dp).testId("fixed-box")) {
                            DomposableRow(modifier = DomposableModifier.testId("fixed-row")) {
                                DomposableText("Player name", modifier = DomposableModifier.weight(1f))
                                DomposableText("League points")
                            }
                        }
                    }
                }
            }
        }

        val parentColumn = compose.onNodeWithTag("parent-column").getUnclippedBoundsInRoot()
        val fillBox = compose.onNodeWithTag("fill-box").getUnclippedBoundsInRoot()
        val fillRow = compose.onNodeWithTag("fill-row").getUnclippedBoundsInRoot()
        val fillMetric = compose.onNodeWithTag("fill-metric").getUnclippedBoundsInRoot()
        val nonfillBox = compose.onNodeWithTag("nonfill-box").getUnclippedBoundsInRoot()
        val nonfillRow = compose.onNodeWithTag("nonfill-row").getUnclippedBoundsInRoot()
        val nonfillMetric = compose.onNodeWithTag("nonfill-metric").getUnclippedBoundsInRoot()
        val fixedBox = compose.onNodeWithTag("fixed-box").getUnclippedBoundsInRoot()
        val fixedRow = compose.onNodeWithTag("fixed-row").getUnclippedBoundsInRoot()

        val parentWidth = parentColumn.right.value - parentColumn.left.value
        assertEquals(parentColumn.left.value + 16f, fillBox.left.value, 1f)
        assertEquals(parentColumn.right.value - 16f, fillBox.right.value, 1f)
        assertEquals(fillBox.left.value, fillRow.left.value, 1f)
        assertEquals(fillBox.right.value, fillRow.right.value, 1f)
        assertEquals(fillRow.right.value, fillMetric.right.value, 1f)
        assertEquals(nonfillBox.left.value, nonfillRow.left.value, 1f)
        assertEquals(nonfillRow.right.value, nonfillMetric.right.value, 1f)
        val nonfillWidth = nonfillRow.right.value - nonfillRow.left.value
        val fixedBoxWidth = fixedBox.right.value - fixedBox.left.value
        assertTrue(nonfillWidth < parentWidth - 32f)
        assertTrue(fixedBoxWidth < parentWidth - 32f)
        assertEquals(fixedBox.left.value, fixedRow.left.value, 1f)
        assertEquals(fixedBox.right.value, fixedRow.right.value, 1f)
    }
}
