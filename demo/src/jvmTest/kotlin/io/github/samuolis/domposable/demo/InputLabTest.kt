package io.github.samuolis.domposable.demo

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import org.junit.Rule
import org.junit.Test

class InputLabTest {
    @get:Rule val compose = createComposeRule()

    @Test fun formValidationDisabledControlsAndFeedbackFollowUserActions() {
        compose.setContent { DomposableMaterialTheme { InputLab() } }
        compose.onNodeWithText("Preview").assertIsNotEnabled()
        compose.onNodeWithText("Allow local preview").performClick().assertIsOn()
        compose.onNodeWithText("Preview").performClick()
        compose.onNodeWithText("Enter your display name").assertExists()
        compose.onNodeWithTag("input-name").performTextInput("Žąsis 李")
        compose.onNodeWithText("Preview").performClick()
        compose.onNodeWithText("Preview ready for Žąsis 李").assertExists()
        compose.onNodeWithText("Editing enabled").performClick()
        compose.onNodeWithTag("input-name").assertIsNotEnabled()
        compose.onNodeWithTag("input-secret").assertIsNotEnabled()
        compose.onNodeWithText("Preview").assertIsNotEnabled()
        compose.onNodeWithText("Editing enabled").performClick()
        compose.onNodeWithTag("input-name").assertIsEnabled()
    }

    @Test fun helpModalDismissesThroughItsVisibleAction() {
        compose.setContent { DomposableMaterialTheme { InputLab() } }
        compose.onNodeWithText("Open help").performClick()
        compose.onNodeWithText("Input help").assertExists()
        compose.onNodeWithText("Close help").performClick()
        compose.onNodeWithText("Input help").assertDoesNotExist()
    }
}
