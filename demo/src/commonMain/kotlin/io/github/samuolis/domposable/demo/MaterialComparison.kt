package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableColors
import io.github.samuolis.domposable.foundation.DomposableDesignLanguage
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.ui.DomposableCheckbox
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRadioButton
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableSlider
import io.github.samuolis.domposable.ui.DomposableSwitch
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.fillMaxSize
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.verticalScroll
import io.github.samuolis.domposable.ui.width

internal val ComparisonThemes =
    listOf(
        "light",
        "dark",
        "brand-light",
        "brand-dark",
        "overrides-light",
        "overrides-dark",
        "expressive-light",
        "expressive-dark",
    )
internal val ComparisonFamilies = listOf("states") + MaterialSections

internal fun materialComparisonPath(path: String): Boolean {
    val parts = path.split('/')
    return parts.size == 3 && parts[0] == "compare" && parts[1] in ComparisonThemes && parts[2] in ComparisonFamilies
}

/** The same route, theme, state and composition on both appliers; no application chrome. */
@Composable
internal fun MaterialComparisonScreen(path: String) {
    val (_, theme, family) = path.split('/')
    val dark = theme.endsWith("dark")
    val branded = theme.startsWith("brand")
    val base = materialCatalogTokens(theme.startsWith("expressive"), theme.startsWith("overrides"))
    val colors = comparisonColors(base.colors, dark, branded)
    DomposableMaterialTheme(
        colors = colors,
        typography = base.typography,
        shapes = base.shapes,
        styles = base.components,
        designLanguage =
            if (theme.startsWith(
                    "expressive",
                )
            ) {
                DomposableDesignLanguage.Expressive
            } else {
                DomposableDesignLanguage.Standard
            },
    ) {
        DomposableColumn(
            DomposableModifier
                .fillMaxSize()
                .verticalScroll()
                .padding(16.dp)
                .testId("material-comparison"),
            gap = 16.dp,
        ) {
            DomposableText("$family · $theme", style = DomposableTextStyle.Title, headingLevel = 1)
            when (family) {
                "states" -> MaterialControlStates()
                "actions" -> MaterialActions()
                "selection" -> MaterialSelection()
                "inputs" -> MaterialInputs()
                "surfaces" -> MaterialSurfaces()
                "navigation" -> MaterialNavigation()
                "feedback" -> MaterialFeedback()
                "gestures" -> MaterialGestures()
                "scaffold" -> MaterialScaffold()
            }
        }
    }
}

private fun comparisonColors(
    base: DomposableColors,
    dark: Boolean,
    branded: Boolean,
): DomposableColors =
    base.copy(
        primary =
            DomposableColor(
                if (dark) {
                    if (branded) {
                        0xFF83D5C5
                    } else {
                        0xFFC8BFFF
                    }
                } else if (branded) {
                    0xFF006B5F
                } else {
                    0xFF5746B2
                },
            ),
        onPrimary = DomposableColor(if (dark) 0xFF182F2B else 0xFFFFFFFF),
        surface = DomposableColor(if (dark) 0xFF141218 else 0xFFFCFAFF),
        onSurface = DomposableColor(if (dark) 0xFFE6E0E9 else 0xFF242137),
        surfaceVariant = DomposableColor(if (dark) 0xFF49454F else 0xFFF0EDF8),
        onSurfaceVariant = DomposableColor(if (dark) 0xFFCAC4D0 else 0xFF49454F),
        secondaryContainer = DomposableColor(if (dark) 0xFF49445C else 0xFFE8DEF8),
        onSecondaryContainer = DomposableColor(if (dark) 0xFFE8DEF8 else 0xFF1D192B),
        outline = DomposableColor(if (dark) 0xFF938F99 else 0xFF79747E),
        outlineVariant = DomposableColor(if (dark) 0xFF49454F else 0xFFCAC4D0),
        error = DomposableColor(if (dark) 0xFFF2B8B5 else 0xFFB3261E),
    )

@Composable
private fun MaterialControlStates() {
    for (enabled in listOf(true, false)) {
        for (selected in listOf(false, true)) {
            val state = "${if (enabled) "Enabled" else "Disabled"} ${if (selected) "selected" else "unselected"}"
            ComparisonRow(state) {
                DomposableCheckbox(selected, {}, "$state checkbox", enabled = enabled, showLabel = false)
                DomposableSwitch(selected, {}, "$state switch", enabled = enabled, showLabel = false)
                DomposableRadioButton(selected, {}, "$state radio", enabled = enabled, showLabel = false)
            }
        }
        ComparisonRow(if (enabled) "Mixed" else "Disabled mixed") {
            DomposableCheckbox(false, {}, "Mixed checkbox", enabled = enabled, showLabel = false, indeterminate = true)
        }
        for (value in listOf(0f, 0.5f, 1f)) {
            ComparisonRow("${if (enabled) "Value" else "Disabled"} $value") {
                DomposableSlider(
                    value,
                    {},
                    "Slider $value",
                    enabled = enabled,
                    modifier = DomposableModifier.width(240.dp),
                )
            }
        }
        ComparisonRow(if (enabled) "Stepped" else "Disabled stepped") {
            DomposableSlider(
                0.5f,
                {},
                "Stepped slider",
                steps = 3,
                enabled = enabled,
                modifier = DomposableModifier.width(240.dp),
            )
        }
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    content: @Composable () -> Unit,
) {
    DomposableRow(DomposableModifier.height(64.dp), gap = 16.dp, alignment = DomposableAlignment.Center) {
        DomposableText(label, modifier = DomposableModifier.width(100.dp))
        content()
    }
}
