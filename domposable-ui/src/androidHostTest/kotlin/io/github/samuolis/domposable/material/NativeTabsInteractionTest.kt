package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.LayoutDirection
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableItemColors
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.ui.DomposableForm
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTabItem
import io.github.samuolis.domposable.ui.DomposableTabs
import io.github.samuolis.domposable.ui.DomposableTabsActivation
import io.github.samuolis.domposable.ui.DomposableTabsAppearance
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.width
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
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
    @Config(qualifiers = "w360dp-h800dp-mdpi")
    @Suppress("LongMethod") // Compare both width modes under fixed and scrollable Material rows.
    fun primaryIndicatorUsesTheRequestedLabelOrTabWidthForFixedAndScrollableRows() {
        val cases =
            listOf(
                Triple("fixed-label", false, true),
                Triple("fixed-tab", false, false),
                Triple("scrollable-label", true, true),
                Triple("scrollable-tab", true, false),
            )
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    cases.forEach { (id, scrollable, matchContent) ->
                        val label = "Selected"
                        DomposableTabs(
                            items =
                                listOf(
                                    DomposableTabItem("before", "Before"),
                                    DomposableTabItem(
                                        "selected",
                                        label,
                                        testTag = "$id-selected",
                                        labelContent = { Text(label, Modifier.testTag("$id-label")) },
                                    ),
                                    DomposableTabItem("after", "After"),
                                ),
                            selectedIndex = 1,
                            onSelect = {},
                            modifier = DomposableModifier.width(DomposableLength(300f)).testId("$id-row"),
                            appearance =
                                DomposableItemAppearance(
                                    borderWidth = DomposableLength(0f),
                                    selectedBorderWidth = DomposableLength(0f),
                                    colors = DomposableItemColors(selectedBorder = DomposableColor(0xFFFF0000)),
                                ),
                            scrollable = scrollable,
                            tabsAppearance =
                                DomposableTabsAppearance(
                                    indicatorThickness = DomposableLength(3f),
                                    indicatorMatchContent = matchContent,
                                    scrollableEdgePadding = DomposableLength(0f),
                                ),
                        )
                    }
                }
            }
        }

        cases.forEach { (id, _, matchContent) ->
            val selectedWidth =
                compose
                    .onNodeWithTag(
                        if (matchContent) "$id-label" else "$id-selected",
                        useUnmergedTree = true,
                    ).getUnclippedBoundsInRoot()
                    .let { it.right - it.left }
            val pixels = compose.onNodeWithTag("$id-row").captureToImage().toPixelMap()
            val indicatorWidth = pixels.longestHorizontalRun(Color(0xFFFF0000)).toFloat()

            assertEquals(selectedWidth.value, indicatorWidth, 2f, "$id indicator width")
        }
    }

    @Test
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
    fun disabledSelectedTabKeepsItsLegacySelectedContainerWhenNoOverrideIsSet() {
        compose.setContent {
            DomposableMaterialTheme {
                DomposableTabs(
                    items =
                        listOf(
                            DomposableTabItem("locked", "Locked", enabled = false, testTag = "locked-selected-tab"),
                            DomposableTabItem("active", "Active"),
                        ),
                    selectedIndex = 0,
                    onSelect = {},
                    appearance =
                        DomposableItemAppearance(
                            colors =
                                DomposableItemColors(
                                    selectedContainer = DomposableColor(0xFFCC2211),
                                    disabledContainer = DomposableColor(0xFF1122CC),
                                ),
                            disabledOpacity = 1f,
                        ),
                )
            }
        }

        val pixels = compose.onNodeWithTag("locked-selected-tab").captureToImage().toPixelMap()
        assertEquals(Color(0xFFCC2211), pixels[1, pixels.height / 2])
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

private fun androidx.compose.ui.graphics.PixelMap.longestHorizontalRun(color: Color): Int {
    val expected = color.toArgb()
    var longest = 0
    for (y in 0 until height) {
        var current = 0
        for (x in 0 until width) {
            if (this[x, y].toArgb() == expected) {
                current++
                longest = maxOf(longest, current)
            } else {
                current = 0
            }
        }
    }
    return longest
}
