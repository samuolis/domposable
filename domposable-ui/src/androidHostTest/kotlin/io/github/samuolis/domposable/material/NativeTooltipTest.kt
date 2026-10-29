package io.github.samuolis.domposable.material

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTooltip
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NativeTooltipTest {
    @get:Rule val compose = createComposeRule()

    @Test fun tapOpensAndKeyboardDismissalAllowsReopening() {
        compose.setContent {
            var open by remember { mutableStateOf(false) }
            DomposableMaterialTheme {
                DomposableTooltip(
                    text = "Saved estimate. Source: Elo.",
                    visible = open,
                    onDismiss = { open = false },
                    surfaceModifier = DomposableModifier.testId("help-surface"),
                ) {
                    DomposableButton("Win probability", { open = true })
                }
            }
        }
        compose.onNodeWithTag("help-surface").assertDoesNotExist()
        compose.onNodeWithText("Win probability").performClick()
        compose.onNodeWithTag("help-surface").assertIsDisplayed()
        compose.onNode(isPopup()).performKeyInput { pressKey(Key.Escape) }
        compose.waitUntil(5000) { compose.onAllNodesWithTag("help-surface").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("help-surface").assertDoesNotExist()
        compose.onNodeWithText("Win probability").performClick()
        compose.onNodeWithTag("help-surface").assertIsDisplayed()
        compose.onNode(isPopup()).performKeyInput { pressKey(Key.Back) }
        compose.waitUntil(5000) { compose.onAllNodesWithTag("help-surface").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithText("Win probability").performClick()
        compose.onNodeWithTag("help-surface").assertIsDisplayed()
    }
}
