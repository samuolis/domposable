package io.github.samuolis.domposable.material

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableButtonProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
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
class NativeButtonAppearanceTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun themedButtonPreventsActivationDuringLoadingAndRecovers() {
        val loading = mutableStateOf(false)
        var clicks = 0
        compose.setContent {
            DomposableMaterialTheme(
                tokens =
                    DomposableTokens(
                        button =
                            DomposableButtonAppearance(
                                container = DomposableColor(0xFF77503B),
                                shape = DomposableShape.Circle,
                                minHeight = 48.dp,
                            ),
                    ),
            ) {
                DomposableButton(
                    DomposableButtonProps(onClick = { clicks++ }, loading = loading.value, loadingLabel = "Saving"),
                    modifier = DomposableModifier.testId("save"),
                ) { DomposableText("Save") }
            }
        }
        compose.onNodeWithTag("save").assertIsEnabled().performClick()
        compose.runOnIdle { loading.value = true }
        compose.onNodeWithTag("save").assertIsNotEnabled().performTouchInput { click() }
        compose.runOnIdle {
            assertEquals(1, clicks)
            loading.value = false
        }
        compose.onNodeWithTag("save").assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(2, clicks) }
    }
}
