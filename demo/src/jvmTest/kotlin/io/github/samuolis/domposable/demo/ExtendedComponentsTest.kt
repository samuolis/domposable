package io.github.samuolis.domposable.demo

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import org.junit.Rule
import org.junit.Test

class ExtendedComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun selectionCallbacksUpdateTheFeatureAndSnackbarActionsResetIt() {
        compose.setContent { DomposableMaterialTheme { ExtendedComponentCatalog() } }
        compose.onNodeWithContentDescription("Delivery").performClick()
        compose.onNodeWithText("Same day (unavailable)").assertIsNotEnabled()
        compose.onNodeWithText("Express").performClick()
        compose.onNodeWithText("Office").performClick().assertIsSelected()
        compose.onNodeWithText("Deliver EXPRESS to Office").assertExists()
        compose.onNodeWithContentDescription("Save preferences").assert(
            SemanticsMatcher.keyNotDefined(SemanticsProperties.Text),
        )
        compose.onNodeWithContentDescription("About preferences").assert(
            SemanticsMatcher.keyNotDefined(SemanticsProperties.Text),
        )
        compose.onNodeWithContentDescription("Save preferences").performClick()
        compose.onNodeWithText("Preferences saved").assertExists()
        compose.onNodeWithText("Reset").performClick()
        compose.onNodeWithText("Deliver STANDARD to Home").assertExists()
        compose.onNodeWithText("Dismiss").performClick()
        compose.onNodeWithText("Preferences reset").assertDoesNotExist()
    }
}
