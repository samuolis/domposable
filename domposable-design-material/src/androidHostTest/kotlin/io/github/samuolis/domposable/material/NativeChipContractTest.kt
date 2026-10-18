package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableChipVariant
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableItemColors
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextOverflow
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
import kotlin.math.abs
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

    @Test
    fun nativeChipGeometryMatchesMaterialDefaultsAcrossVariants() {
        val cases = chipGeometryCases()
        var currentCase by mutableStateOf(cases.first())
        compose.setContent { DomposableMaterialTheme { NativeChipGeometryPair(currentCase) } }

        cases.forEach { case ->
            compose.runOnIdle { currentCase = case }
            assertGeometryCase(case)
        }
    }

    private fun chipGeometryCases(): List<ChipGeometryCase> {
        val variants =
            listOf(
                DomposableChipVariant.Assist to listOf(false, true),
                DomposableChipVariant.Filter to listOf(false, true),
                DomposableChipVariant.Suggestion to listOf(false, true),
                DomposableChipVariant.Input to listOf(false),
            )
        val standardCases =
            variants.flatMap { (variant, elevations) ->
                elevations.flatMap { elevated ->
                    listOf(
                        ChipGeometryCase("${variant.name}-$elevated-plain", variant, elevated),
                        ChipGeometryCase(
                            "${variant.name}-$elevated-icons",
                            variant,
                            elevated,
                            leading = true,
                            trailing = variant != DomposableChipVariant.Suggestion,
                        ),
                    )
                }
            }
        return standardCases +
            ChipGeometryCase(
                "assist-wide-custom-gap",
                DomposableChipVariant.Assist,
                leading = true,
                trailing = true,
                spacing = 16f,
                widthDp = 240f,
            )
    }

    private fun assertGeometryCase(case: ChipGeometryCase) {
        val domRoot = chipBounds("${case.id}-dom")
        val materialRoot = chipBounds("${case.id}-material")
        assertTrue(domRoot.width > 0f && domRoot.height > 0f, "${case.id} Domposable chip is laid out")
        assertTrue(materialRoot.width > 0f && materialRoot.height > 0f, "${case.id} Material chip is laid out")
        assertGeometryClose(materialRoot.width, domRoot.width, "${case.id} width")
        assertGeometryClose(materialRoot.height, domRoot.height, "${case.id} height")
        assertLabelGeometry(case, domRoot, materialRoot)
        assertIconGeometry(case, domRoot, materialRoot)
    }

    private fun chipBounds(tag: String) =
        compose.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

    private fun assertLabelGeometry(
        case: ChipGeometryCase,
        domRoot: androidx.compose.ui.geometry.Rect,
        materialRoot: androidx.compose.ui.geometry.Rect,
    ) {
        val labels = compose.onAllNodesWithText(case.label, useUnmergedTree = true).fetchSemanticsNodes()
        assertEquals(2, labels.size, "${case.id} label nodes")
        assertGeometryClose(
            labels[0].boundsInRoot.left - domRoot.left,
            labels[1].boundsInRoot.left - materialRoot.left,
            "${case.id} label position",
        )
        assertGeometryClose(
            labels[0].boundsInRoot.top - domRoot.top,
            labels[1].boundsInRoot.top - materialRoot.top,
            "${case.id} label vertical position",
        )
    }

    private fun assertIconGeometry(
        case: ChipGeometryCase,
        domRoot: androidx.compose.ui.geometry.Rect,
        materialRoot: androidx.compose.ui.geometry.Rect,
    ) {
        if (case.leading) {
            assertRelativeIconGeometry(case, "leading", domRoot, materialRoot)
        }
        if (case.trailing) {
            assertRelativeIconGeometry(case, "trailing", domRoot, materialRoot)
        }
    }

    private fun assertRelativeIconGeometry(
        case: ChipGeometryCase,
        placement: String,
        domRoot: androidx.compose.ui.geometry.Rect,
        materialRoot: androidx.compose.ui.geometry.Rect,
    ) {
        val domIcon = chipBounds("${case.id}-dom-$placement")
        val materialIcon = chipBounds("${case.id}-material-$placement")
        val domOffset = if (placement == "leading") domIcon.left - domRoot.left else domRoot.right - domIcon.right
        val materialOffset =
            if (placement == "leading") {
                materialIcon.left - materialRoot.left
            } else {
                materialRoot.right - materialIcon.right
            }
        assertGeometryClose(materialOffset, domOffset, "${case.id} $placement icon position")
        assertGeometryClose(
            materialIcon.top - materialRoot.top,
            domIcon.top - domRoot.top,
            "${case.id} $placement icon vertical position",
        )
    }
}

private data class ChipGeometryCase(
    val id: String,
    val variant: DomposableChipVariant,
    val elevated: Boolean = false,
    val leading: Boolean = false,
    val trailing: Boolean = false,
    val spacing: Float? = null,
    val widthDp: Float? = null,
) {
    val label: String get() = if (widthDp == null) "Label" else "Wide trailing label"
}

@Composable
private fun NativeChipGeometryPair(case: ChipGeometryCase) {
    val appearance = DomposableItemAppearance(contentSpacing = case.spacing?.let(::DomposableLength))
    DomposableChip(
        label = case.label,
        onClick = {},
        modifier =
            (case.widthDp?.let { DomposableModifier.width(DomposableLength(it)) } ?: DomposableModifier)
                .testId("${case.id}-dom"),
        variant = case.variant,
        elevated = case.elevated,
        appearance = appearance,
        leadingIcon =
            if (case.leading) {
                { ChipGeometryIcon("${case.id}-dom-leading") }
            } else {
                null
            },
        trailingIcon =
            if (case.trailing) {
                { ChipGeometryIcon("${case.id}-dom-trailing") }
            } else {
                null
            },
    )
    MaterialChipGeometryReference(case)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialChipGeometryReference(case: ChipGeometryCase) {
    val spacing = case.spacing?.dp
    val leading: (@Composable () -> Unit)? =
        if (case.leading) {
            { ChipGeometryIcon("${case.id}-material-leading") }
        } else {
            null
        }
    val trailing: (@Composable () -> Unit)? =
        if (case.trailing) {
            { ChipGeometryIcon("${case.id}-material-trailing") }
        } else {
            null
        }
    val modifier =
        (case.widthDp?.let { Modifier.width(it.dp) } ?: Modifier)
            .testTag("${case.id}-material")
    val label: @Composable () -> Unit = { Text(case.label) }
    when (case.variant) {
        DomposableChipVariant.Assist -> {
            if (case.elevated) {
                ElevatedAssistChip(
                    onClick = {},
                    label = label,
                    modifier = modifier,
                    leadingIcon = leading,
                    trailingIcon = trailing,
                    horizontalArrangement =
                        spacing?.let { AssistChipDefaults.horizontalArrangement(it) }
                            ?: AssistChipDefaults.horizontalArrangement(),
                )
            } else {
                AssistChip(
                    onClick = {},
                    label = label,
                    modifier = modifier,
                    leadingIcon = leading,
                    trailingIcon = trailing,
                    horizontalArrangement =
                        spacing?.let { AssistChipDefaults.horizontalArrangement(it) }
                            ?: AssistChipDefaults.horizontalArrangement(),
                )
            }
        }

        DomposableChipVariant.Filter -> {
            if (case.elevated) {
                ElevatedFilterChip(
                    selected = false,
                    onClick = {},
                    label = label,
                    modifier = modifier,
                    leadingIcon = leading,
                    trailingIcon = trailing,
                    horizontalArrangement =
                        spacing?.let { FilterChipDefaults.horizontalArrangement(it) }
                            ?: FilterChipDefaults.horizontalArrangement(),
                )
            } else {
                FilterChip(
                    selected = false,
                    onClick = {},
                    label = label,
                    modifier = modifier,
                    leadingIcon = leading,
                    trailingIcon = trailing,
                    horizontalArrangement =
                        spacing?.let { FilterChipDefaults.horizontalArrangement(it) }
                            ?: FilterChipDefaults.horizontalArrangement(),
                )
            }
        }

        DomposableChipVariant.Suggestion -> {
            if (case.elevated) {
                ElevatedSuggestionChip(
                    onClick = {},
                    label = label,
                    modifier = modifier,
                    icon = leading,
                    horizontalArrangement =
                        spacing?.let { SuggestionChipDefaults.horizontalArrangement(it) }
                            ?: SuggestionChipDefaults.horizontalArrangement(),
                )
            } else {
                SuggestionChip(
                    onClick = {},
                    label = label,
                    modifier = modifier,
                    icon = leading,
                    horizontalArrangement =
                        spacing?.let { SuggestionChipDefaults.horizontalArrangement(it) }
                            ?: SuggestionChipDefaults.horizontalArrangement(),
                )
            }
        }

        DomposableChipVariant.Input -> {
            InputChip(
                selected = false,
                onClick = {},
                label = label,
                modifier = modifier,
                leadingIcon = leading,
                trailingIcon = trailing,
                horizontalArrangement =
                    spacing?.let { InputChipDefaults.horizontalArrangement(it) }
                        ?: InputChipDefaults.horizontalArrangement(),
            )
        }
    }
}

@Composable
private fun ChipGeometryIcon(tag: String) {
    Box(Modifier.size(16.dp).testTag(tag))
}

private fun assertGeometryClose(
    expected: Float,
    actual: Float,
    message: String,
) {
    assertTrue(abs(expected - actual) <= 1f, "$message: expected $expected, got $actual")
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
