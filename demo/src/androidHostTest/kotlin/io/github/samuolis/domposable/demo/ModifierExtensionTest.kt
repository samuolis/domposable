package io.github.samuolis.domposable.demo

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
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
