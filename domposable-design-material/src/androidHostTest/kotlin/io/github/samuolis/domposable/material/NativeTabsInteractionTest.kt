package io.github.samuolis.domposable.material

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.LayoutDirection
import io.github.samuolis.domposable.ui.DomposableForm
import io.github.samuolis.domposable.ui.DomposableTabItem
import io.github.samuolis.domposable.ui.DomposableTabs
import io.github.samuolis.domposable.ui.DomposableTabsActivation
import io.github.samuolis.domposable.ui.DomposableTabsAppearance
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
class NativeTabsInteractionTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun keyedTabsPreservePerItemSemanticsAndDisabledState() {
        val selected = mutableStateListOf<Int>()
        compose.setContent {
            DomposableMaterialTheme {
                DomposableTabs(
                    items =
                        listOf(
                            DomposableTabItem("active", "Active", "Active tab", testTag = "active-tab"),
                            DomposableTabItem("locked", "Locked", enabled = false, testTag = "locked-tab"),
                        ),
                    selectedIndex = 0,
                    onSelect = selected::add,
                    label = "Competition sections",
                )
            }
        }

        compose
            .onNodeWithTag("active-tab")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf("Active tab")))
            .assertHasClickAction()
            .performClick()
        compose.runOnIdle { assertEquals(listOf(0), selected) }
        compose.onNodeWithTag("locked-tab").assertIsNotEnabled()
    }

    @Test
    fun manualActivationMovesFocusAcrossEnabledTabsWithoutSelecting() {
        val selected = mutableStateListOf<Int>()
        lateinit var inputMode: InputModeManager
        compose.setContent {
            DomposableMaterialTheme {
                inputMode = LocalInputModeManager.current
                DomposableTabs(
                    items =
                        listOf(
                            DomposableTabItem("first", "First", testTag = "first-tab"),
                            DomposableTabItem("disabled", "Disabled", enabled = false, testTag = "disabled-tab"),
                            DomposableTabItem("last", "Last", testTag = "last-tab"),
                        ),
                    selectedIndex = 0,
                    onSelect = selected::add,
                    tabsAppearance = DomposableTabsAppearance(activation = DomposableTabsActivation.Manual),
                )
            }
        }

        compose.runOnIdle { inputMode.requestInputMode(InputMode.Keyboard) }
        compose
            .onNodeWithTag("first-tab")
            .performSemanticsAction(SemanticsActions.RequestFocus)
        compose.onNodeWithTag("first-tab").assertIsFocused()
        compose.onNodeWithTag("first-tab").performKeyInput { pressKey(Key.DirectionRight) }
        compose.onNodeWithTag("last-tab").assertIsFocused()
        compose.runOnIdle { assertEquals(emptyList(), selected) }

        compose.onNodeWithTag("last-tab").performKeyInput { pressKey(Key.DirectionLeft) }
        compose.onNodeWithTag("first-tab").assertIsFocused()
        compose.runOnIdle { assertEquals(emptyList(), selected) }
    }

    @Test
    fun disabledFormDisablesTabActionsAndKeyboardFocus() {
        val selected = mutableStateListOf<Int>()
        compose.setContent {
            DomposableMaterialTheme {
                DomposableForm(onSubmit = {}, enabled = false) {
                    DomposableTabs(
                        items =
                            listOf(
                                DomposableTabItem("first", "First", testTag = "form-first-tab"),
                                DomposableTabItem("last", "Last", testTag = "form-last-tab"),
                            ),
                        selectedIndex = 0,
                        onSelect = selected::add,
                    )
                }
            }
        }

        compose.onNodeWithTag("form-first-tab").assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(emptyList(), selected) }
    }

    @Test
    fun automaticActivationSelectsTheFocusedTab() {
        val selected = mutableStateListOf<Int>()
        lateinit var inputMode: InputModeManager
        compose.setContent {
            DomposableMaterialTheme {
                inputMode = LocalInputModeManager.current
                DomposableTabs(
                    items =
                        listOf(
                            DomposableTabItem("first", "First", testTag = "automatic-first-tab"),
                            DomposableTabItem("last", "Last", testTag = "automatic-last-tab"),
                        ),
                    selectedIndex = 0,
                    onSelect = selected::add,
                )
            }
        }

        compose.runOnIdle { inputMode.requestInputMode(InputMode.Keyboard) }
        compose
            .onNodeWithTag("automatic-first-tab")
            .performSemanticsAction(SemanticsActions.RequestFocus)
        compose.onNodeWithTag("automatic-first-tab").assertIsFocused()
        compose.onNodeWithTag("automatic-first-tab").performKeyInput { pressKey(Key.DirectionRight) }
        compose.onNodeWithTag("automatic-last-tab").assertIsFocused()
        compose.runOnIdle { assertEquals(listOf(1), selected) }
    }

    @Test
    fun rtlArrowKeysFollowLogicalTabOrder() {
        val selected = mutableStateListOf<Int>()
        lateinit var inputMode: InputModeManager
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                DomposableMaterialTheme {
                    inputMode = LocalInputModeManager.current
                    DomposableTabs(
                        items =
                            listOf(
                                DomposableTabItem("first", "First", testTag = "rtl-first-tab"),
                                DomposableTabItem("last", "Last", testTag = "rtl-last-tab"),
                            ),
                        selectedIndex = 0,
                        onSelect = selected::add,
                        tabsAppearance = DomposableTabsAppearance(activation = DomposableTabsActivation.Manual),
                    )
                }
            }
        }

        compose.runOnIdle { inputMode.requestInputMode(InputMode.Keyboard) }
        compose
            .onNodeWithTag("rtl-first-tab")
            .performSemanticsAction(SemanticsActions.RequestFocus)
        compose.onNodeWithTag("rtl-first-tab").assertIsFocused()
        compose.onNodeWithTag("rtl-first-tab").performKeyInput { pressKey(Key.DirectionLeft) }
        compose.onNodeWithTag("rtl-last-tab").assertIsFocused()
        compose.runOnIdle { assertEquals(emptyList(), selected) }
    }
}
