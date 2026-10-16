package io.github.samuolis.domposable.material

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import io.github.samuolis.domposable.ui.DomposableTimePicker
import io.github.samuolis.domposable.ui.DomposableTimePickerMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeTimeScrollTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun scrollPickerChangesHourMinuteAndPeriod() {
        val hour = mutableIntStateOf(10)
        val minute = mutableIntStateOf(30)
        compose.setContent {
            DomposableMaterialTheme {
                DomposableTimePicker(
                    hour = hour.intValue,
                    minute = minute.intValue,
                    onTimeChange = { nextHour, nextMinute ->
                        hour.intValue = nextHour
                        minute.intValue = nextMinute
                    },
                    is24Hour = false,
                    mode = DomposableTimePickerMode.Scroll,
                )
            }
        }

        compose.onNodeWithContentDescription("Hour").performTouchInput { swipeUp() }
        compose.waitForIdle()
        val scrolledHour = hour.intValue
        assertNotEquals(10, scrolledHour)

        compose.onNodeWithContentDescription("Minute").performTouchInput { swipeUp() }
        compose.waitForIdle()
        assertNotEquals(30, minute.intValue)

        compose.onNodeWithText("PM").performClick()
        compose.waitForIdle()
        assertTrue(hour.intValue >= 12)
    }
}
