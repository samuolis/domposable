package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableComponentStyles
import io.github.samuolis.domposable.foundation.DomposableShape
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
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
    fun geometryOverridesRetainElevatedAndDisabledTonalMaterialColors() {
        var expectedContent = Color.Unspecified
        var actualContent = Color.Unspecified
        compose.setContent {
            DomposableMaterialTheme {
                val elevatedContent = ButtonDefaults.elevatedButtonColors().contentColor
                SideEffect { expectedContent = elevatedContent }
                Column {
                    DomposableButton(
                        DomposableButtonProps(
                            onClick = {},
                            style = DomposableButtonStyle.Elevated,
                            appearance = DomposableButtonAppearance(contentSpacing = 1.dp),
                        ),
                    ) {
                        val color = LocalContentColor.current
                        SideEffect { actualContent = color }
                    }
                    FilledTonalButton({}, Modifier.testTag("material-tonal"), enabled = false) {
                        Box(
                            Modifier.size(
                                androidx.compose.ui.unit
                                    .Dp(8f),
                            ),
                        )
                    }
                    DomposableButton(
                        DomposableButtonProps(
                            onClick = {},
                            style = DomposableButtonStyle.FilledTonal,
                            enabled = false,
                            appearance = DomposableButtonAppearance(contentSpacing = 1.dp),
                        ),
                        DomposableModifier.testId("custom-tonal"),
                    ) {
                        Box(
                            Modifier.size(
                                androidx.compose.ui.unit
                                    .Dp(8f),
                            ),
                        )
                    }
                }
            }
        }
        compose.runOnIdle { assertEquals(expectedContent, actualContent) }
        val material = compose.onNodeWithTag("material-tonal").captureToImage().toPixelMap()
        val custom = compose.onNodeWithTag("custom-tonal").captureToImage().toPixelMap()
        assertEquals(material[material.width / 2, material.height / 2], custom[custom.width / 2, custom.height / 2])
    }

    @Test
    fun themedButtonPreventsActivationDuringLoadingAndRecovers() {
        val loading = mutableStateOf(false)
        var clicks = 0
        compose.setContent {
            DomposableMaterialTheme(
                styles =
                    DomposableComponentStyles(
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
