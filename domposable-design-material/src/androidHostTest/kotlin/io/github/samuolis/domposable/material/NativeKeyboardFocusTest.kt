package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.ui.DomposableCheckbox
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.testId
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
class NativeKeyboardFocusTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun keyboardFocusPaintsTheThemeRingOutsideTheSelectionHitTarget() {
        lateinit var manager: InputModeManager
        var primary = Color.Unspecified
        compose.setContent {
            DomposableMaterialTheme {
                manager = LocalInputModeManager.current
                primary = MaterialTheme.colorScheme.primary
                Box(Modifier.size(80.dp).testTag("focus-frame")) {
                    Box(Modifier.padding(16.dp)) {
                        DomposableCheckbox(
                            false,
                            {},
                            "Choice",
                            modifier = DomposableModifier.testId("focus-choice"),
                            showLabel = false,
                        )
                    }
                }
            }
        }
        compose.runOnIdle { manager.requestInputMode(InputMode.Keyboard) }
        compose.onNodeWithTag("focus-choice").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        val pixels = compose.onNodeWithTag("focus-frame").captureToImage().toPixelMap()
        val coordinate = pixels.width * 11 / 80
        assertEquals(primary, pixels[coordinate, coordinate])
    }
}
