package io.github.samuolis.domposable.material

import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h900dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeNestedColumnWidthTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun fillWidthCardUsesNearestBoundWhileIntrinsicColumnStaysContentSized() {
        compose.setContent { NestedColumnWidthFixture(showFill = true) }

        val actualWidthsDp =
            nestedColumnWidthExpectations.associate { expectation ->
                expectation.testId to
                    compose
                        .onNodeWithTag(expectation.testId)
                        .getUnclippedBoundsInRoot()
                        .let { it.right.value - it.left.value }
            }
        val intrinsicWidthsDp =
            intrinsicColumnWidthTestIds.associateWith { testId ->
                compose
                    .onNodeWithTag(testId)
                    .getUnclippedBoundsInRoot()
                    .let { it.right.value - it.left.value }
            }

        compose.runOnIdle {
            nestedColumnWidthExpectations.forEach { expectation ->
                assertEquals(
                    expectation.expectedWidthDp.toFloat(),
                    actualWidthsDp.getValue(expectation.testId),
                    1f,
                    expectation.label,
                )
            }
            intrinsicColumnWidthTestIds.forEach { testId ->
                assertTrue(
                    intrinsicWidthsDp.getValue(testId) > 0f && intrinsicWidthsDp.getValue(testId) < 200f,
                    "$testId should stay intrinsic",
                )
            }
        }
    }

    @Test
    fun weightedNestedRowsStayInsideTheirSiblingBounds() {
        compose.setContent { WeightedRowOverlapFixture() }

        fun bounds(testId: String) = compose.onNodeWithTag(testId).getUnclippedBoundsInRoot()

        val row = bounds("weighted-row")
        val leading = bounds("leading-slot")
        val roster = bounds("roster-row")
        val alex = bounds("alex-column")
        val taylor = bounds("taylor-column")
        val rating = bounds("rating-slot")

        compose.runOnIdle {
            assertEquals(358f, row.right.value - row.left.value, 1f)
            assertEquals(row.left.value, leading.left.value, 1f)
            assertTrue(leading.right.value <= roster.left.value + 1f, "roster starts after the leading slot")
            assertTrue(roster.right.value <= rating.left.value + 1f, "roster ends before the rating slot")
            assertTrue(alex.right.value <= taylor.left.value + 1f, "weighted member columns do not overlap")
            assertTrue(taylor.right.value <= roster.right.value + 1f, "members stay inside the roster row")
            assertTrue(rating.right.value - rating.left.value < 150f, "unweighted card keeps its intrinsic width")
            assertEquals(row.right.value, rating.right.value, 1f)
        }
    }
}
