package io.github.samuolis.domposable.material

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableItemColors
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
class NativeDisabledSelectedItemColorsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun disabledSelectedItemsResolveSpecificColorsAndFallBackToDisabledColors() {
        compose.setContent {
            DomposableMaterialTheme {
                val interactionSource = remember { MutableInteractionSource() }
                val custom =
                    DomposableItemAppearance(
                        colors =
                            DomposableItemColors(
                                disabledContainer = DomposableColor(0xFF112233),
                                disabledContent = DomposableColor(0xFF223344),
                                disabledBorder = DomposableColor(0xFF334455),
                                disabledSelectedContainer = DomposableColor(0xFFAA0000),
                                disabledSelectedContent = DomposableColor(0xFF00AA00),
                                disabledSelectedBorder = DomposableColor(0xFF0000AA),
                            ),
                    ).resolveInteractionColors(interactionSource, selected = true, enabled = false)
                val fallback =
                    DomposableItemAppearance(
                        colors =
                            DomposableItemColors(
                                disabledContainer = DomposableColor(0xFF112233),
                                disabledContent = DomposableColor(0xFF223344),
                                disabledBorder = DomposableColor(0xFF334455),
                            ),
                    ).resolveInteractionColors(interactionSource, selected = true, enabled = false)
                val unselected =
                    DomposableItemAppearance(
                        colors =
                            DomposableItemColors(
                                disabledContainer = DomposableColor(0xFF112233),
                                disabledContent = DomposableColor(0xFF223344),
                                disabledBorder = DomposableColor(0xFF334455),
                                disabledSelectedContainer = DomposableColor(0xFFAA0000),
                                disabledSelectedContent = DomposableColor(0xFF00AA00),
                                disabledSelectedBorder = DomposableColor(0xFF0000AA),
                            ),
                    ).resolveInteractionColors(interactionSource, selected = false, enabled = false)
                SideEffect {
                    assertEquals(Color(0xFFAA0000), custom.selectedContainer)
                    assertEquals(Color(0xFF00AA00), custom.selectedContent)
                    assertEquals(Color(0xFF0000AA), custom.selectedBorder)
                    assertEquals(Color(0xFF112233), fallback.selectedContainer)
                    assertEquals(Color(0xFF223344), fallback.selectedContent)
                    assertEquals(Color(0xFF334455), fallback.selectedBorder)
                    assertEquals(Color(0xFF112233), unselected.container)
                    assertEquals(Color(0xFF223344), unselected.content)
                    assertEquals(Color(0xFF334455), unselected.border)
                }
            }
        }
        compose.waitForIdle()
    }
}
