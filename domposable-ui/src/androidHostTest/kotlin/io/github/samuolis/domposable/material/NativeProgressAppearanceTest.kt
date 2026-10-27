package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableProgressAppearance
import io.github.samuolis.domposable.foundation.DomposableProgressVariant
import io.github.samuolis.domposable.ui.DomposableLoadingIndicator
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
                    DomposableProgressVariant.entries.forEach { variant ->
                        DomposableProgress(
                            modifier = DomposableModifier.testId("without-stop-$variant").width(DomposableLength(200f)),
                            value = 0.25f,
                            circular = false,
                            appearance = progressAppearance(showStopIndicator = false),
                            variant = variant,
                        )
                        Spacer(Modifier.height(48.dp))
                        DomposableProgress(
                            modifier = DomposableModifier.testId("with-stop-$variant").width(DomposableLength(200f)),
                            value = 0.25f,
                            circular = false,
                            appearance = progressAppearance(showStopIndicator = true),
                            variant = variant,
                        )
                    }
                }
            }
        }

        DomposableProgressVariant.entries.forEach { variant ->
            val withoutStop = compose.onNodeWithTag("without-stop-$variant").captureToImage().toPixelMap()
            val withStop = compose.onNodeWithTag("with-stop-$variant").captureToImage().toPixelMap()
            assertFalse(withoutStop.hasRedPixelInLastQuarter())
            assertTrue(withStop.hasRedPixelInLastQuarter())
        }
    }

    @Test
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
    fun wavyStrokeScalesWithDisplayDensity() {
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    listOf(1f, 2f).forEach { density ->
                        CompositionLocalProvider(LocalDensity provides Density(density)) {
                            DomposableProgress(
                                modifier = DomposableModifier.testId("wavy-$density"),
                                value = 0.6f,
                                variant = DomposableProgressVariant.Wavy,
                                appearance =
                                    DomposableProgressAppearance(
                                        color = DomposableColor(0xFFFF0000),
                                        track = DomposableColor(0xFFFFFFFF),
                                        strokeWidth = DomposableLength(4f),
                                        waveSpeed = DomposableLength(0f),
                                    ),
                            )
                        }
                    }
                }
            }
        }

        fun redPixels(tag: String): Int {
            val pixels = compose.onNodeWithTag(tag).captureToImage().toPixelMap()
            return (0 until pixels.width).sumOf { x ->
                (0 until pixels.height).count { y ->
                    val pixel = pixels[x, y]
                    pixel.red > 0.8f && pixel.green < 0.2f && pixel.blue < 0.2f
                }
            }
        }
        val ratio = redPixels("wavy-2.0").toFloat() / redPixels("wavy-1.0")
        assertTrue(ratio in 3.5f..4.5f, "Doubling density should quadruple painted area: $ratio")
    }

    @Test
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
    fun containedLoadingUsesTheMaterialContainerForeground() {
        val expected = mutableMapOf<Boolean, Int>()
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    listOf(false, true).forEach { contained ->
                        val colors = androidx.compose.material3.MaterialTheme.colorScheme
                        expected[contained] = (if (contained) colors.onPrimaryContainer else colors.primary).toArgb()
                        DomposableLoadingIndicator(
                            progress = 0.6f,
                            contained = contained,
                            modifier = DomposableModifier.testId("loading-$contained"),
                        )
                    }
                }
            }
        }
        listOf(false, true).forEach { contained ->
            val pixels = compose.onNodeWithTag("loading-$contained").captureToImage().toPixelMap()
            assertEquals(expected[contained], pixels[pixels.width / 2, pixels.height / 2].toArgb())
        }
    }

    private fun progressAppearance(showStopIndicator: Boolean) =
        DomposableProgressAppearance(
            color = DomposableColor(0xFFFF0000),
            track = DomposableColor(0xFFFFFFFF),
            strokeWidth = DomposableLength(12f),
            showStopIndicator = showStopIndicator,
            waveSpeed = DomposableLength(0f),
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
