package io.github.samuolis.domposable.demo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WorkspaceTest {
    @get:Rule val compose = createComposeRule()
    @Test fun sharedFeatureUsesNativeInputAndNavigation() {
        compose.setContent { DemoApp() }
        compose.onNodeWithText("Task workspace").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("task-welcome").performScrollTo().performClick()
        compose.onNodeWithTag("task-notes").performTextInput("Android feature notes")
        compose.onNodeWithTag("task-notes").assertTextContains("Android feature notes")
        compose.onNodeWithText("Back to tasks").performClick()
        compose.onNodeWithText("Task workspace").performScrollTo().assertIsDisplayed()
    }
}
