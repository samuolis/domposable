package io.github.samuolis.domposable.material

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableItemColors
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableScrollField
import io.github.samuolis.domposable.ui.asDomposableModifier
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeMaterialInputExtensionsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun scrollFieldAppearanceColorsReachNativeColorScheme() {
        val appearance =
            DomposableItemAppearance(
                colors =
                    DomposableItemColors(
                        container = DomposableColor(0xFF112233),
                        content = DomposableColor(0xFF223344),
                        selectedContent = DomposableColor(0xFF334455),
                        disabledContainer = DomposableColor(0xFF445566),
                        disabledContent = DomposableColor(0xFF556677),
                    ),
            )
        var colors: androidx.compose.material3.ScrollFieldColors? = null
        var disabledColors: androidx.compose.material3.ScrollFieldColors? = null
        compose.setContent {
            DomposableMaterialTheme {
                colors = appearance.nativeScrollFieldColors()
                disabledColors = appearance.nativeScrollFieldColors(enabled = false)
            }
        }
        compose.runOnIdle {
            val resolved = requireNotNull(colors)
            assertEquals(0xFF112233.toInt(), resolved.containerColor.toArgb())
            assertEquals(0xFF223344.toInt(), resolved.unselectedContentColor.toArgb())
            assertEquals(0xFF334455.toInt(), resolved.selectedContentColor.toArgb())
            val disabled = requireNotNull(disabledColors)
            assertEquals(0xFF445566.toInt(), disabled.containerColor.toArgb())
            assertEquals(0xFF556677.toInt(), disabled.unselectedContentColor.toArgb())
            assertEquals(0xFF556677.toInt(), disabled.selectedContentColor.toArgb())
        }
    }

    @Test
    fun scrollFieldRetainsAccessibleAdjustmentKeyboardAndDisabledState() {
        val selected = mutableStateOf(0)
        val enabled = mutableStateOf(true)
        val callbacks = mutableListOf<Int>()
        val focus = FocusRequester()
        compose.setContent {
            DomposableMaterialTheme {
                DomposableScrollField(
                    itemCount = 5,
                    selectedIndex = selected.value,
                    onSelectedIndexChange = {
                        callbacks += it
                        selected.value = it
                    },
                    label = "Select month",
                    indexDescription = { "Month $it" },
                    enabled = enabled.value,
                    modifier = Modifier.focusRequester(focus).asDomposableModifier().testId("month-field"),
                )
            }
        }
        val field = compose.onNodeWithTag("month-field")
        field.assertContentDescriptionEquals("Select month")
        val initialPage = field.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current
        field.performSemanticsAction(SemanticsActions.SetProgress) { it(initialPage + 1) }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(listOf(1), callbacks) }
        compose.runOnIdle { focus.requestFocus() }
        field.performKeyInput { pressKey(Key.DirectionDown) }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(listOf(1, 2), callbacks) }
        assertEquals("Month 2", field.fetchSemanticsNode().config[SemanticsProperties.StateDescription])

        compose.runOnIdle { enabled.value = false }
        field.assertIsNotEnabled()
        field.performSemanticsAction(SemanticsActions.SetProgress) { assertFalse(it(initialPage + 3)) }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(listOf(1, 2), callbacks) }
    }

    @Test
    fun externalScrollFieldSelectionUpdateDoesNotEchoCallback() {
        val selectedIndex = mutableStateOf(0)
        val callbacks = mutableListOf<Int>()
        compose.setContent {
            DomposableMaterialTheme {
                DomposableScrollField(
                    itemCount = 5,
                    selectedIndex = selectedIndex.value,
                    onSelectedIndexChange = callbacks::add,
                    label = "Select month",
                    modifier = DomposableModifier.testId("month-field"),
                )
            }
        }

        compose.runOnIdle { selectedIndex.value = 2 }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(emptyList(), callbacks) }
    }
}
