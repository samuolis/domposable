package io.github.samuolis.domposable.material

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
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
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.DomposableTextFieldProps
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
class NativeTextFieldAppearanceTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun passwordSemanticsStayMaskedWithoutChangingTheEditedValue() {
        val value = mutableStateOf("")
        val visible = mutableStateOf(false)
        compose.setContent {
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
        compose.onNodeWithTag("secret").performTextInput("Private 123")
        compose.runOnIdle { assertEquals("Private 123", value.value) }
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
