package io.github.samuolis.domposable.material

import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableArrangement
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableFlowRow
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableTheme
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.width
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeLayoutArrangementTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun spaceBetweenDistributesChildrenAcrossTheAvailableWidth() {
        compose.setContent {
            DomposableTheme(components = rememberDomposableMaterialComponents()) {
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
}
