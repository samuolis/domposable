package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableProgressAppearance
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableProgress
import io.github.samuolis.domposable.ui.asDomposableModifier
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.width
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
class NativeProgressAppearanceTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun linearProgressHonorsExplicitStrokeWidth() {
        var measuredHeight = 0
        var expectedHeight = 0
        compose.setContent {
            expectedHeight = with(LocalDensity.current) { 12.dp.roundToPx() }
            DomposableMaterialTheme {
                DomposableProgress(
                    modifier = Modifier.onSizeChanged { measuredHeight = it.height }.asDomposableModifier(),
                    value = 0.5f,
                    circular = false,
                    appearance = DomposableProgressAppearance(strokeWidth = DomposableLength(12f)),
                )
            }
        }

        compose.runOnIdle { assertEquals(expectedHeight, measuredHeight) }
    }

    @Test
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
    fun determinateLinearProgressCanHideStopIndicator() {
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    DomposableProgress(
                        modifier = DomposableModifier.testId("without-stop").width(DomposableLength(200f)),
                        value = 0.25f,
                        circular = false,
                        appearance = progressAppearance(showStopIndicator = false),
                    )
                    Spacer(Modifier.height(48.dp))
                    DomposableProgress(
                        modifier = DomposableModifier.testId("with-stop").width(DomposableLength(200f)),
                        value = 0.25f,
                        circular = false,
                        appearance = progressAppearance(showStopIndicator = true),
                    )
                }
            }
        }

        val withoutStop = compose.onNodeWithTag("without-stop").captureToImage().toPixelMap()
        val withStop = compose.onNodeWithTag("with-stop").captureToImage().toPixelMap()

        assertFalse(withoutStop.hasRedPixelInLastQuarter())
        assertTrue(withStop.hasRedPixelInLastQuarter())
    }

    private fun progressAppearance(showStopIndicator: Boolean) =
        DomposableProgressAppearance(
            color = DomposableColor(0xFFFF0000),
            track = DomposableColor(0xFFFFFFFF),
            strokeWidth = DomposableLength(12f),
            showStopIndicator = showStopIndicator,
        )

    private fun PixelMap.hasRedPixelInLastQuarter(): Boolean {
        for (x in width * 3 / 4 until width) {
            for (y in height / 4 until height * 3 / 4) {
                val pixel = this[x, y]
                if (pixel.red > 0.8f && pixel.green < 0.2f && pixel.blue < 0.2f) {
                    return true
                }
            }
        }
        return false
    }
}
