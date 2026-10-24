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
}
