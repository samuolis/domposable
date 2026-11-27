package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.AnnotatedString
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableComponentStyles
import io.github.samuolis.domposable.foundation.DomposableInputPurpose
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableStateColors
import io.github.samuolis.domposable.foundation.DomposableTextFieldAppearance
import io.github.samuolis.domposable.foundation.DomposableTextFieldColors
import io.github.samuolis.domposable.foundation.DomposableTextFieldVariant
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableIcon
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.DomposableTextFieldDecorations
import io.github.samuolis.domposable.ui.DomposableTextFieldProps
import io.github.samuolis.domposable.ui.DomposableVectorIcon
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeTextFieldAppearanceTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun decorativeIconKeepsOppositeActionSizeAndClick() {
        var clicks = 0
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    listOf(false, true).forEach { decorative ->
                        DomposableTextFieldDecorations(leading = decorative) {
                            DomposableTextField(
                                value = "Alice",
                                onValueChange = {},
                                label = "Search",
                                leadingIcon = { Text("Search") },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { clicks++ },
                                        modifier = Modifier.testTag("clear-$decorative"),
                                    ) {
                                        Text("Clear")
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
        val normal = compose.onNodeWithTag("clear-false").fetchSemanticsNode().boundsInRoot
        val decorated = compose.onNodeWithTag("clear-true")
        val bounds = decorated.fetchSemanticsNode().boundsInRoot
        assertEquals(normal.width, bounds.width)
        assertEquals(normal.height, bounds.height)
        decorated.performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
    }

    @Test
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
    fun decorativeTrailingIconStaysInsetAndActivatesTheField() {
        var clicks = 0
        compose.setContent {
            DomposableMaterialTheme {
                DomposableTextFieldDecorations(trailing = true) {
                    DomposableTextField(
                        value = "Tennis",
                        onValueChange = {},
                        label = "Sport",
                        id = "sport",
                        readOnly = true,
                        onClick = { clicks++ },
                        trailingIcon = {
                            DomposableIcon(
                                vector = DomposableVectorIcon("M7.41 8.59 12 13.17 16.59 8.59 18 10 12 16 6 10z"),
                                description = null,
                                modifier = DomposableModifier.testId("arrow"),
                            )
                        },
                    )
                }
            }
        }
        val field = compose.onNodeWithTag("sport")
        val bounds = field.fetchSemanticsNode().boundsInRoot
        val iconBounds = compose.onNodeWithTag("arrow", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        val pixels = field.captureToImage().toPixelMap()
        val density = compose.density.density
        val background = pixels[pixels.width - (40 * density).toInt(), pixels.height / 2]
        val rightmost =
            (pixels.width - (3 * density).toInt() downTo pixels.width - (40 * density).toInt())
                .firstOrNull { x ->
                    ((pixels.height / 2 - (10 * density).toInt())..(pixels.height / 2 + (10 * density).toInt()))
                        .any { y -> pixels[x, y] != background }
                }
        assertTrue(rightmost != null, "Trailing icon was not painted")
        val insetDp = (pixels.width - 1 - rightmost) / density
        assertTrue(insetDp in 8f..24f, "Painted trailing icon inset was $insetDp dp")
        val borderX = pixels.width * 3 / 4
        val top = (0 until pixels.height / 2).first { pixels[borderX, it] != background }
        val bottom = (pixels.height - 1 downTo pixels.height / 2).first { pixels[borderX, it] != background }
        val outlineCenter = bounds.top + (top + bottom) / 2f
        val verticalOffsetDp = kotlin.math.abs(iconBounds.center.y - outlineCenter) / density
        assertTrue(verticalOffsetDp < 1.5f, "Icon center differed from outline center by $verticalOffsetDp dp")
        field.performTouchInput { click(iconBounds.center - bounds.topLeft) }
        compose.runOnIdle { assertEquals(1, clicks) }
    }

    @Test
    fun copiedMultilineFieldAcceptsNewlines() {
        val value = mutableStateOf("")
        compose.setContent {
            DomposableMaterialTheme {
                DomposableTextField(
                    DomposableTextFieldProps(value = value.value, onValueChange = { value.value = it }, label = "Notes")
                        .copy(multiline = true),
                )
            }
        }
        compose.onNodeWithText("Notes").performTextInput("first\nsecond")
        compose.runOnIdle { assertEquals("first\nsecond", value.value) }
    }

    @Test
    fun passwordEditsStayMaskedAndOutOfSavedState() {
        val value = mutableStateOf("")
        val visible = mutableStateOf(false)
        val registry = SaveableStateRegistry(null) { true }
        compose.setContent {
            CompositionLocalProvider(LocalSaveableStateRegistry provides registry) {
                DomposableMaterialTheme {
                    DomposableTextField(
                        DomposableTextFieldProps(
                            value = value.value,
                            onValueChange = { value.value = it },
                            label = "Private phrase",
                            id = "secret",
                            purpose = DomposableInputPurpose.Password,
                            passwordVisible = visible.value,
                        ),
                    )
                }
            }
        }
        compose.onNodeWithTag("secret").performTextInput("Private 123")
        compose.runOnIdle {
            assertEquals("Private 123", value.value)
            assertFalse(registry.performSave().toString().contains("Private 123"))
        }
        compose.onNodeWithTag("secret").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("•".repeat(11))),
        )
        compose.runOnIdle { visible.value = true }
        compose.onNodeWithTag("secret").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("Private 123")),
        )
    }

    @Test
    fun themedReadonlyFieldKeepsLabelErrorAndNoTextEntryAction() {
        compose.setContent {
            DomposableMaterialTheme(
                styles =
                    DomposableComponentStyles(
                        textField =
                            DomposableTextFieldAppearance(
                                colors =
                                    DomposableTextFieldColors(
                                        container = DomposableStateColors(unfocused = DomposableColor(0xFFF5F1EB)),
                                        text = DomposableStateColors(unfocused = DomposableColor(0xFF332D27)),
                                        label = DomposableStateColors(unfocused = DomposableColor(0xFF665B4F)),
                                        outline =
                                            DomposableStateColors(
                                                unfocused = DomposableColor(0xFF8B7865),
                                                focused = DomposableColor(0xFF5C4A3B),
                                                error = DomposableColor(0xFFBA1A1A),
                                            ),
                                    ),
                                shape = DomposableShape.Rounded(8.dp),
                                floatingLabel = true,
                                variant = DomposableTextFieldVariant.Filled,
                            ),
                    ),
            ) {
                DomposableTextField(
                    DomposableTextFieldProps(
                        value = "locked",
                        onValueChange = {},
                        label = "Account number",
                        readOnly = true,
                        error = "This value is locked",
                        id = "account-number",
                    ),
                )
            }
        }

        compose.onNodeWithTag("account-number").assertIsDisplayed()
        compose.onNodeWithTag("account-number").assert(SemanticsMatcher.keyNotDefined(SemanticsActions.SetText))
        compose.onNodeWithText("Account number").assertIsDisplayed()
        compose.onNodeWithText("This value is locked").assertIsDisplayed()
    }

    @Test
    fun accessibleFieldActivationUsesTheLatestCallbackAndReportsFocus() {
        val version = mutableIntStateOf(1)
        val clicks = mutableListOf<Int>()
        val focus = mutableListOf<Boolean>()
        compose.setContent {
            val current = version.intValue
            DomposableMaterialTheme {
                DomposableTextField(
                    DomposableTextFieldProps(
                        value = "",
                        onValueChange = {},
                        label = "Picker",
                        id = "picker",
                        readOnly = true,
                        onClick = { clicks += current },
                        onFocusChange = { focus += it },
                    ),
                )
            }
        }
        compose.runOnIdle { version.intValue = 2 }
        compose.onNodeWithTag("picker").performClick()
        compose.runOnIdle {
            assertEquals(listOf(2), clicks)
            assertEquals(true, focus.last())
        }
        compose.onNodeWithTag("picker").performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.runOnIdle { assertEquals(listOf(2, 2), clicks) }
    }
}
