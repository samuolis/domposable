package io.github.samuolis.domposable.material

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableItemColors
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableScrollField
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
        compose.setContent {
            DomposableMaterialTheme {
                colors = appearance.nativeScrollFieldColors()
            }
        }
        compose.runOnIdle {
            val resolved = requireNotNull(colors)
            assertEquals(0xFF112233.toInt(), resolved.containerColor.toArgb())
            assertEquals(0xFF223344.toInt(), resolved.contentColor.toArgb())
            assertEquals(0xFF334455.toInt(), resolved.selectedContentColor.toArgb())
            assertEquals(0xFF445566.toInt(), resolved.disabledContainerColor.toArgb())
            assertEquals(0xFF556677.toInt(), resolved.disabledSelectedContentColor.toArgb())
        }
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
