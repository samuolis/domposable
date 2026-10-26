package io.github.samuolis.domposable.material

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableComponentStyles
import io.github.samuolis.domposable.foundation.DomposableInteractionAppearance
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableSwitchAppearance
import io.github.samuolis.domposable.foundation.DomposableSwitchColors
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableButtonProps
import io.github.samuolis.domposable.ui.DomposableIconButton
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableSwitch
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import androidx.compose.ui.unit.dp as nativeDp

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeButtonAppearanceTest {
    @get:Rule val compose = createComposeRule()

    @Test
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
    fun explicitCompactHeightKeepsLayoutAndExpandedTouchTarget() {
        var compactClicks = 0
        compose.setContent {
            DomposableMaterialTheme {
                Column(Modifier.width(200.nativeDp)) {
                    DomposableButton(
                        DomposableButtonProps(
                            onClick = { compactClicks++ },
                            appearance = DomposableButtonAppearance(minHeight = 32.dp),
                        ),
                        DomposableModifier.fillMaxWidth().testId("compact"),
                    ) { Box(Modifier.size(8.nativeDp)) }
                    Spacer(Modifier.height(32.nativeDp))
                    DomposableButton(
                        DomposableButtonProps(onClick = {}, style = DomposableButtonStyle.Filled),
                        DomposableModifier.testId("default"),
                    ) { DomposableText("Default") }
                }
            }
        }

        val compact = compose.onNodeWithTag("compact")
        compact.assertHeightIsEqualTo(32.nativeDp).assertWidthIsEqualTo(200.nativeDp)
        val capture = compact.captureToImage()
        val capturedCompact = capture.toPixelMap()
        assertEquals(32.nativeDp, with(compose.density) { capturedCompact.height.toDp() })
        val target = File("../build/screenshots/android/compact-native-button.png")
        target.parentFile!!.mkdirs()
        target.outputStream().use { capture.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
        compose.onNodeWithTag("default").assertHeightIsEqualTo(40.nativeDp)
        compact.performTouchInput {
            click(center + Offset(0f, 19.nativeDp.toPx()))
        }
        compose.runOnIdle { assertEquals(1, compactClicks) }
    }

    @Test
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
    fun explicitCompactHeightRespectsExactParentHeight() {
        compose.setContent {
            DomposableMaterialTheme {
                Box(Modifier.height(32.nativeDp)) {
                    DomposableButton(
                        DomposableButtonProps(
                            onClick = {},
                            appearance = DomposableButtonAppearance(minHeight = 32.dp),
                        ),
                        DomposableModifier.testId("exact-height"),
                    ) { Box(Modifier.size(8.nativeDp)) }
                }
            }
        }

        compose.onNodeWithTag("exact-height").assertHeightIsEqualTo(32.nativeDp)
    }

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
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
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
                                indicatorTrackColor = DomposableColor(0xFFFF0000),
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
        val loadingPixels = compose.onNodeWithTag("save").captureToImage().toPixelMap()
        assertTrue(
            (0 until loadingPixels.height).any { y ->
                (0 until loadingPixels.width).any { x ->
                    loadingPixels[x, y].red > .8f && loadingPixels[x, y].green < .2f && loadingPixels[x, y].blue < .2f
                }
            },
            "The configured loading indicator track must be rendered",
        )
        compose.runOnIdle {
            assertEquals(1, clicks)
            loading.value = false
        }
        compose.onNodeWithTag("save").assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(2, clicks) }
    }

    @Test
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
    fun typedInteractionUsesHoveredAndPressedColorsAndKeepsFocusRingVisible() {
        lateinit var inputMode: InputModeManager
        compose.setContent {
            inputMode = LocalInputModeManager.current
            InteractionControls()
        }
        val button = compose.onNodeWithTag("interaction-button")
        button.performMouseInput { moveTo(center) }
        val hovered = button.captureToImage().toPixelMap()
        assertEquals(Color(0xFFAA0000), hovered[hovered.width / 2, hovered.height / 2])
        button.performTouchInput { down(center) }
        val pressed = button.captureToImage().toPixelMap()
        assertEquals(Color(0xFF00AA00), pressed[pressed.width / 2, pressed.height / 2])
        button.performTouchInput { up() }
        compose.runOnIdle { inputMode.requestInputMode(InputMode.Keyboard) }
        button.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        button.assertIsFocused()
        val focused = compose.onNodeWithTag("interaction-host").captureToImage().toPixelMap()
        assertTrue(
            (0 until focused.height).any { y ->
                (0 until focused.width).any { x -> focused[x, y] == Color(0xFFFFFF00) }
            },
            "The configured focus ring must be painted outside the button, without clipping",
        )
        val icon = compose.onNodeWithTag("interaction-icon")
        icon.performMouseInput { moveTo(center) }
        val hoveredIcon = icon.captureToImage().toPixelMap()
        assertEquals(Color(0xFFAA0000), hoveredIcon[hoveredIcon.width / 2, hoveredIcon.height / 2])
        icon.performTouchInput { down(center) }
        val pressedIcon = icon.captureToImage().toPixelMap()
        assertEquals(Color(0xFF00AA00), pressedIcon[pressedIcon.width / 2, pressedIcon.height / 2])
        icon.performTouchInput { up() }
    }

    @Composable
    private fun InteractionControls() {
        DomposableMaterialTheme {
            Column(Modifier.testTag("interaction-host").padding(16.nativeDp)) {
                DomposableButton(
                    DomposableButtonProps(
                        onClick = {},
                        appearance =
                            DomposableButtonAppearance(
                                container = DomposableColor(0xFF123456),
                                interaction =
                                    DomposableInteractionAppearance(
                                        hoveredContainer = DomposableColor(0xFFAA0000),
                                        pressedContainer = DomposableColor(0xFF00AA00),
                                        focusRingColor = DomposableColor(0xFFFFFF00),
                                        focusRingWidth = 4.dp,
                                        focusRingOffset = 6.dp,
                                    ),
                            ),
                    ),
                    DomposableModifier.testId("interaction-button"),
                ) { }
                DomposableIconButton(
                    onClick = {},
                    label = "Close",
                    modifier = DomposableModifier.testId("interaction-icon"),
                    appearance =
                        DomposableButtonAppearance(
                            container = DomposableColor(0xFF123456),
                            interaction =
                                DomposableInteractionAppearance(
                                    hoveredContainer = DomposableColor(0xFFAA0000),
                                    pressedContainer = DomposableColor(0xFF00AA00),
                                ),
                        ),
                ) { Box(Modifier.size(20.nativeDp)) }
            }
        }
    }

    @Test
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
    fun disabledOpacityAppliesToTheWholeNativeButton() {
        var surface = Color.Unspecified
        compose.setContent {
            DomposableMaterialTheme {
                val surfaceColor = MaterialTheme.colorScheme.surface
                SideEffect { surface = surfaceColor }
                Box(Modifier.background(surfaceColor)) {
                    DomposableButton(
                        DomposableButtonProps(
                            onClick = {},
                            enabled = false,
                            appearance =
                                DomposableButtonAppearance(
                                    disabledContainer = DomposableColor(0xFFFF0000),
                                    disabledOpacity = .38f,
                                ),
                        ),
                        DomposableModifier.testId("faded-disabled"),
                    ) { }
                }
            }
        }
        val image = compose.onNodeWithTag("faded-disabled").captureToImage().toPixelMap()
        assertEquals(
            Color(0xFFFF0000).copy(alpha = .38f).compositeOver(surface).toArgb(),
            image[
                image.width / 2, image.height /
                    2,
            ].toArgb(),
        )
    }

    @Test
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
    fun switchPressRetainsMaterialThumbGeometry() {
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    Switch(
                        false,
                        {},
                        Modifier.testTag("material-switch"),
                        colors = SwitchDefaults.colors(uncheckedThumbColor = Color.Red),
                    )
                    DomposableSwitch(
                        false,
                        {},
                        "Switch",
                        showLabel = false,
                        modifier = DomposableModifier.testId("shared-switch"),
                        appearance =
                            DomposableSwitchAppearance(
                                colors = DomposableSwitchColors(uncheckedThumb = DomposableColor(0xFFFF0000)),
                            ),
                    )
                }
            }
        }

        fun pressedThumbWidth(tag: String): Int {
            val control = compose.onNodeWithTag(tag)
            control.performTouchInput { down(center) }
            compose.waitForIdle()
            val pixels = control.captureToImage().toPixelMap()
            val width =
                (0 until pixels.width).count { x ->
                    val pixel = pixels[x, pixels.height / 2]
                    pixel.red > 0.9f && pixel.green < 0.1f && pixel.blue < 0.1f
                }
            control.performTouchInput { up() }
            return width
        }
        val expected = pressedThumbWidth("material-switch")
        assertEquals(expected, pressedThumbWidth("shared-switch"))
    }
}
