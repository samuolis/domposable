package io.github.samuolis.domposable.demo

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import org.junit.Rule
import org.junit.Test

class ModifierExtensionTest {
    @get:Rule val compose = createComposeRule()

    @Test fun customDrawingAndPlatformComponentKeepTheirContentState() {
        compose.setContent { DomposableMaterialTheme { ModifierExtensionExample() } }
        compose.onNodeWithTag("app-extension-draft").performTextInput("A retained app draft")
        compose.onNodeWithText("Increment custom counter").performClick()
        compose.onNodeWithText("Custom counter: 1").assertExists()
        compose.onNodeWithText("Change frame color").performClick()
        compose.onNodeWithText("Custom counter: 1").assertExists()
        compose.onNodeWithText("A retained app draft").assertExists()
    }
}
