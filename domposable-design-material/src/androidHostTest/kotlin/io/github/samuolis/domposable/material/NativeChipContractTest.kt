package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import io.github.samuolis.domposable.foundation.DomposableChipVariant
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableItemColors
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextOverflow
import io.github.samuolis.domposable.ui.DomposableChip
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.width
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeChipContractTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun chipTextAppearanceLimitsAndEllipsizesNativeLabels() {
        val unselectedLabel = "A very long unselected chip label that should be truncated"
        val selectedLabel = "A very long selected chip label that should be truncated"
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    DomposableChip(
                        label = unselectedLabel,
                        onClick = {},
                        modifier = DomposableModifier.width(DomposableLength(112f)),
                        appearance =
                            DomposableItemAppearance(
                                textAppearance =
                                    DomposableTextAppearance(
                                        maxLines = 1,
                                        overflow = DomposableTextOverflow.Ellipsis,
                                    ),
                            ),
                    )
                    DomposableChip(
                        label = selectedLabel,
                        onClick = {},
                        modifier = DomposableModifier.width(DomposableLength(112f)),
                        selected = true,
                        appearance =
                            DomposableItemAppearance(
                                textAppearance = DomposableTextAppearance(maxLines = 3),
                                selectedTextAppearance =
                                    DomposableTextAppearance(
                                        maxLines = 1,
                                        overflow = DomposableTextOverflow.Ellipsis,
                                    ),
                            ),
                    )
                }
            }
        }

        listOf(unselectedLabel, selectedLabel).forEach { label ->
            val layouts = mutableListOf<TextLayoutResult>()
            compose
                .onNodeWithText(label, useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
                    assertTrue(action(layouts))
                }
            val layout = layouts.single()
            assertEquals(1, layout.lineCount)
            assertTrue(layout.isLineEllipsized(0))
        }
    }

    @Test
    fun chipIconsUseConfiguredColorsAndAssistRendersTrailingIcon() {
        val content = DomposableColor(0xFF13579B)
        val selectedContent = DomposableColor(0xFF9B5713)
        val appearance =
            DomposableItemAppearance(
                colors = DomposableItemColors(content = content, selectedContent = selectedContent),
            )
        val observed = mutableMapOf<String, Color>()

        compose.setContent {
            DomposableMaterialTheme { ChipColorCases(appearance, observed) }
        }

        compose.runOnIdle {
            assertEquals(
                mapOf(
                    "filter" to Color(selectedContent.argb),
                    "filter-trailing" to Color(selectedContent.argb),
                    "assist-leading" to Color(content.argb),
                    "assist-trailing" to Color(content.argb),
                    "elevated-assist-leading" to Color(content.argb),
                    "elevated-assist-trailing" to Color(content.argb),
                    "suggestion" to Color(content.argb),
                    "input" to Color(selectedContent.argb),
                    "input-trailing" to Color(selectedContent.argb),
                ),
                observed,
            )
        }
    }

    @Test
    fun disabledChipIconsUseTheConfiguredDisabledContentColor() {
        val disabled = DomposableColor(0xFF793E82)
        val observed = mutableMapOf<String, Color>()
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    DomposableChipVariant.entries.forEach { variant ->
                        DomposableChip(
                            label = variant.name,
                            onClick = {},
                            variant = variant,
                            selected = true,
                            enabled = false,
                            appearance =
                                DomposableItemAppearance(
                                    colors = DomposableItemColors(disabledContent = disabled),
                                ),
                            leadingIcon = { CaptureContentColor(variant.name, observed) },
                            trailingIcon =
                                if (variant == DomposableChipVariant.Suggestion) {
                                    null
                                } else {
                                    ({ CaptureContentColor("${variant.name}-trailing", observed) })
                                },
                        )
                    }
                }
            }
        }
        compose.runOnIdle {
            val expectedKeys =
                DomposableChipVariant.entries.flatMap { variant ->
                    if (variant == DomposableChipVariant.Suggestion) {
                        listOf(variant.name)
                    } else {
                        listOf(variant.name, "${variant.name}-trailing")
                    }
                }
            assertEquals(expectedKeys.associateWith { Color(disabled.argb) }, observed)
        }
    }
}

@Composable
private fun ChipColorCases(
    appearance: DomposableItemAppearance,
    observed: MutableMap<String, Color>,
) {
    Column {
        DomposableChip(
            label = "Filter",
            onClick = {},
            selected = true,
            appearance = appearance,
            variant = DomposableChipVariant.Filter,
            leadingIcon = { CaptureContentColor("filter", observed) },
            trailingIcon = { CaptureContentColor("filter-trailing", observed) },
        )
        DomposableChip(
            label = "Assist",
            onClick = {},
            appearance = appearance,
            variant = DomposableChipVariant.Assist,
            leadingIcon = { CaptureContentColor("assist-leading", observed) },
            trailingIcon = { CaptureContentColor("assist-trailing", observed) },
        )
        DomposableChip(
            label = "Elevated assist",
            onClick = {},
            appearance = appearance,
            variant = DomposableChipVariant.Assist,
            elevated = true,
            leadingIcon = { CaptureContentColor("elevated-assist-leading", observed) },
            trailingIcon = { CaptureContentColor("elevated-assist-trailing", observed) },
        )
        DomposableChip(
            label = "Suggestion",
            onClick = {},
            appearance = appearance,
            variant = DomposableChipVariant.Suggestion,
            leadingIcon = { CaptureContentColor("suggestion", observed) },
        )
        DomposableChip(
            label = "Input",
            onClick = {},
            selected = true,
            appearance = appearance,
            variant = DomposableChipVariant.Input,
            leadingIcon = { CaptureContentColor("input", observed) },
            trailingIcon = { CaptureContentColor("input-trailing", observed) },
        )
    }
}

@Composable
private fun CaptureContentColor(
    key: String,
    observed: MutableMap<String, Color>,
) {
    val contentColor = LocalContentColor.current
    SideEffect { observed[key] = contentColor }
}
