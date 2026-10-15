package io.github.samuolis.domposable.material

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.down
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import io.github.samuolis.domposable.ui.DomposableLabel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeLabelInteractionTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun nonPersistentLabelShowsOnPressWithoutConsumingChildClick() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            val clicks = remember { mutableIntStateOf(0) }
            DomposableMaterialTheme {
                DomposableLabel(
                    label = { Text("Temporary label", Modifier.testTag("temporary-label")) },
                ) {
                    Button(
                        onClick = { clicks.intValue++ },
                        modifier = Modifier.testTag("label-anchor"),
                    ) {
                        Text("Press me")
                    }
                    Text("Clicks: ${clicks.intValue}", Modifier.testTag("click-count"))
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithTag("label-anchor").performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(300)
        compose.onNodeWithTag("temporary-label").assertExists()
        compose.onNodeWithTag("label-anchor").performTouchInput { up() }
        compose.mainClock.advanceTimeBy(100)
        compose.waitForIdle()
        compose.onNodeWithTag("click-count").assertTextEquals("Clicks: 1")
    }
}
