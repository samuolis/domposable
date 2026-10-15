package io.github.samuolis.domposable.material

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableStateColors
import io.github.samuolis.domposable.foundation.DomposableTextFieldAppearance
import io.github.samuolis.domposable.foundation.DomposableTextFieldColors
import io.github.samuolis.domposable.foundation.DomposableTextFieldVariant
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.DomposableTextFieldProps
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeTextFieldAppearanceTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun themedReadonlyFieldKeepsLabelErrorAndNoTextEntryAction() {
        compose.setContent {
            DomposableMaterialTheme(
                tokens =
                    DomposableTokens(
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
}
