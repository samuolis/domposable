package io.github.samuolis.domposable.material

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import io.github.samuolis.domposable.ui.DomposableDatePicker
import io.github.samuolis.domposable.ui.DomposableDatePickerMode
import io.github.samuolis.domposable.ui.DomposableDatePickerMonthHeader
import io.github.samuolis.domposable.ui.DomposableForm
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
class NativePickerFormTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun monthHeaderCanNavigateWithoutChangingSelectedDate() {
        var displayed = -1L
        var selected: Long? = 0L
        compose.setContent {
            DomposableMaterialTheme {
                DomposableDatePickerMonthHeader(header = { month, showMonth ->
                    displayed = month
                    Button(onClick = { showMonth(2_678_400_000L) }) { Text("February") }
                    Button(onClick = { showMonth(31_536_000_000L) }) { Text("Outside range") }
                }) {
                    DomposableDatePicker(selected, { selected = it }, yearRange = 1970..1970)
                }
            }
        }
        compose.onNodeWithText("February").performClick()
        compose.onNodeWithText("Outside range").performClick()
        compose.runOnIdle {
            assertEquals(2_678_400_000L, displayed)
            assertEquals(0L, selected)
        }
    }

    @Test
    fun disabledFormExposesADisabledPickerAndBlocksChanges() {
        var requests = 0
        compose.setContent {
            DomposableMaterialTheme {
                DomposableForm(onSubmit = {}, enabled = false) {
                    DomposableDatePicker(0L, { requests++ }, mode = DomposableDatePickerMode.Input)
                }
            }
        }
        compose.onNodeWithContentDescription("Date picker").assertIsNotEnabled().performTouchInput { click() }
        compose.runOnIdle { assertEquals(0, requests) }
    }
}
