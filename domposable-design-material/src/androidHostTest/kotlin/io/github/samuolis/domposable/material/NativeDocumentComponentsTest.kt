package io.github.samuolis.domposable.material

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import io.github.samuolis.domposable.foundation.DomposableImeAction
import io.github.samuolis.domposable.ui.DomposableDisclosure
import io.github.samuolis.domposable.ui.DomposableForm
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.DomposableTextFieldProps
import io.github.samuolis.domposable.ui.DomposableTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeDocumentComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun textFieldKeepsReadonlyErrorFocusAndSlotBehavior() {
        val value = mutableStateOf("fixed")
        compose.setContent {
            DomposableTheme(components = rememberDomposableMaterialComponents()) {
                DomposableTextField(
                    DomposableTextFieldProps(
                        value = value.value,
                        onValueChange = { value.value = it },
                        label = "Access code",
                        readOnly = true,
                        error = "Code is locked",
                        isError = true,
                        autofocus = true,
                        id = "locked-code",
                        leadingIcon = { androidx.compose.material3.Text("Leading slot") },
                        prefix = { androidx.compose.material3.Text("ID:") },
                        trailingIcon = { androidx.compose.material3.Text("Trailing slot") },
                    ),
                )
            }
        }

        compose.waitForIdle()
        compose.onNodeWithTag("locked-code").assertIsDisplayed().assertIsFocused()
        compose.onNodeWithText("Code is locked").assertIsDisplayed()
        compose.onNodeWithText("Leading slot").assertIsDisplayed()
        compose.onNodeWithText("ID:").assertIsDisplayed()
        compose.onNodeWithText("Trailing slot").assertIsDisplayed()
        compose.onNodeWithTag("locked-code").assert(SemanticsMatcher.keyNotDefined(SemanticsActions.SetText))
        compose.onNodeWithTag("locked-code").assertTextContains("fixed")
        compose.runOnIdle { kotlin.test.assertEquals("fixed", value.value) }
    }

    @Test
    fun disabledFormDisablesInputAndDisclosureWithoutSubmitting() {
        val submissions = mutableIntStateOf(0)
        val toggles = mutableIntStateOf(0)
        compose.setContent {
            DomposableTheme(components = rememberDomposableMaterialComponents()) {
                DomposableForm(onSubmit = { submissions.intValue++ }, enabled = false, label = "Disabled form") {
                    DomposableTextField(
                        DomposableTextFieldProps(
                            value = "not editable",
                            onValueChange = {},
                            label = "Disabled input",
                            id = "disabled-input",
                            imeAction = DomposableImeAction.Done,
                        ),
                    )
                    DomposableDisclosure(
                        title = "Disabled disclosure",
                        expanded = false,
                        onToggle = { toggles.intValue++ },
                        content = {},
                    )
                }
            }
        }

        compose.onNodeWithTag("disabled-input").assertIsNotEnabled()
        compose.onNodeWithText("Disabled disclosure").performClick()
        compose.runOnIdle {
            kotlin.test.assertEquals(0, submissions.intValue)
            kotlin.test.assertEquals(0, toggles.intValue)
        }
    }

    @Test
    fun enabledFormTextFieldDoneActionSubmitsOnce() {
        val submissions = mutableIntStateOf(0)
        compose.setContent {
            DomposableTheme(components = rememberDomposableMaterialComponents()) {
                DomposableForm(onSubmit = { submissions.intValue++ }) {
                    DomposableTextField(
                        DomposableTextFieldProps(
                            value = "ready",
                            onValueChange = {},
                            label = "Ready",
                            id = "ready-input",
                            imeAction = DomposableImeAction.Done,
                        ),
                    )
                }
            }
        }

        compose.onNodeWithTag("ready-input").performClick().performImeAction()
        compose.runOnIdle { kotlin.test.assertEquals(1, submissions.intValue) }
    }

    @Test
    fun disclosureLabelExcludesDecorativeArrowWhileStateAndContentUpdate() {
        val expanded = mutableStateOf(false)
        compose.setContent {
            DomposableTheme(components = rememberDomposableMaterialComponents()) {
                DomposableDisclosure("Shipping details", expanded.value, { expanded.value = !expanded.value }) {
                    androidx.compose.material3.Text("Shipping content")
                }
            }
        }
        val disclosure = compose.onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        disclosure
            .assertTextEquals("Shipping details")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Collapsed"))
        disclosure.performClick()
        disclosure
            .assertTextEquals("Shipping details")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Expanded"))
        compose.onNodeWithText("Shipping content").assertIsDisplayed()
    }

    @Test fun hostedProviderRetainsMaterialThemeAndUpdatesSubmitCallback() {
        val submittedVersion = mutableIntStateOf(0)
        val onSubmit = mutableStateOf<() -> Unit>({ submittedVersion.intValue = 1 })
        val colorScheme =
            androidx.compose.material3.lightColorScheme(
                primary = androidx.compose.ui.graphics.Color.Magenta,
            )
        compose.setContent {
            androidx.compose.material3.MaterialTheme(colorScheme = colorScheme) {
                ProvideDomposableMaterialComponents(
                    io.github.samuolis.domposable.foundation
                        .DomposableTokens(),
                ) {
                    kotlin.test.assertEquals(
                        colorScheme.primary,
                        androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    )
                    DomposableForm(onSubmit = onSubmit.value) {
                        val submit =
                            io.github.samuolis.domposable.ui
                                .rememberDomposableFormSubmit()
                        io.github.samuolis.domposable.ui
                            .DomposableButton("Submit current callback", submit)
                    }
                }
            }
        }
        compose.runOnIdle { onSubmit.value = { submittedVersion.intValue = 2 } }
        compose.onNodeWithText("Submit current callback").performClick()
        compose.runOnIdle { kotlin.test.assertEquals(2, submittedVersion.intValue) }
    }
}
