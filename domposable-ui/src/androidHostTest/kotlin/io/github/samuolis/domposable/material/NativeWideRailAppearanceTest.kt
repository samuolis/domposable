package io.github.samuolis.domposable.material

import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableWideNavigationRailItem
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeWideRailAppearanceTest {
    @get:Rule val compose = createComposeRule()

    @Test
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
    fun wideRailItemsApplyThemeColorsAndMinimumBounds() {
        compose.setContent {
            DomposableMaterialTheme {
                DomposableWideNavigationRailItem(
                    "Destination",
                    true,
                    {},
                    railExpanded = false,
                    icon = { DomposableText("+") },
                    modifier = DomposableModifier.testId("wide-item"),
                    appearance =
                        DomposableItemAppearance(
                            minHeight = DomposableLength(100f),
                            colors =
                                io.github.samuolis.domposable.foundation.DomposableItemColors(
                                    selectedContainer = DomposableColor(0xFFFF0000),
                                    selectedContent = DomposableColor(0xFF00FF00),
                                ),
                        ),
                )
            }
        }
        val item = compose.onNodeWithTag("wide-item")
        item.assertHeightIsAtLeast(100.dp)
        val pixels = item.captureToImage().toPixelMap()
        val colors =
            (0 until pixels.width)
                .flatMap { x ->
                    (0 until pixels.height).map { y -> pixels[x, y].toArgb() }
                }.toSet()
        assertTrue(0xFFFF0000.toInt() in colors)
        assertTrue(0xFF00FF00.toInt() in colors)
    }
}
