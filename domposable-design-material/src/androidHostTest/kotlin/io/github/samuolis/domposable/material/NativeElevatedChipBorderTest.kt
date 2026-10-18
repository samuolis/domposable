package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableChipVariant
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableItemColors
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.ui.DomposableChip
import io.github.samuolis.domposable.ui.DomposableModifier
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
@Config(sdk = [36], qualifiers = "w390dp-h844dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeElevatedChipBorderTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun elevatedFilterAssistAndSuggestionMatchMaterialDefaultBorderPixels() {
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    ChipBorderPair(DomposableChipVariant.Filter, "filter")
                    ChipBorderPair(DomposableChipVariant.Assist, "assist")
                    ChipBorderPair(DomposableChipVariant.Suggestion, "suggestion")
                }
            }
        }

        listOf("filter", "assist", "suggestion").forEach { variant ->
            val domposable = compose.onNodeWithTag("$variant-portable").captureToImage().toPixelMap()
            val material = compose.onNodeWithTag("$variant-material").captureToImage().toPixelMap()
            assertEquals(material[0, material.height / 2].toArgb(), domposable[0, domposable.height / 2].toArgb())
        }
    }

    @Test
    fun elevatedFilterKeepsAnExplicitAppearanceBorder() {
        compose.setContent {
            DomposableMaterialTheme {
                DomposableChip(
                    label = "Explicit border",
                    onClick = {},
                    modifier = DomposableModifier.width(DomposableLength(128f)).testId("explicit-border"),
                    appearance =
                        DomposableItemAppearance(
                            colors = DomposableItemColors(border = DomposableColor(0xFFFF0000)),
                        ),
                    elevated = true,
                )
            }
        }

        val pixels = compose.onNodeWithTag("explicit-border").captureToImage().toPixelMap()
        assertEquals(0xFFFF0000.toInt(), pixels[0, pixels.height / 2].toArgb())
    }
}

@Composable
private fun ChipBorderPair(
    variant: DomposableChipVariant,
    name: String,
) {
    Row {
        DomposableChip(
            label = "Border",
            onClick = {},
            modifier = DomposableModifier.width(DomposableLength(128f)).testId("$name-portable"),
            variant = variant,
            elevated = true,
        )
        when (variant) {
            DomposableChipVariant.Filter -> {
                ElevatedFilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text("Border") },
                    modifier = Modifier.width(128.dp).testTag("$name-material"),
                )
            }

            DomposableChipVariant.Assist -> {
                ElevatedAssistChip(
                    onClick = {},
                    label = { Text("Border") },
                    modifier = Modifier.width(128.dp).testTag("$name-material"),
                )
            }

            DomposableChipVariant.Suggestion -> {
                ElevatedSuggestionChip(
                    onClick = {},
                    label = { Text("Border") },
                    modifier = Modifier.width(128.dp).testTag("$name-material"),
                )
            }

            DomposableChipVariant.Input -> {
                error("Input chips are not part of elevated border comparison")
            }
        }
    }
}
