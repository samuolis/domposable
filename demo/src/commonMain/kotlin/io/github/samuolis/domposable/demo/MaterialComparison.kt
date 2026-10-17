package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableDesignLanguage
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.ui.DomposableCheckbox
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRadioButton
import io.github.samuolis.domposable.ui.DomposableRangeSlider
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableSlider
import io.github.samuolis.domposable.ui.DomposableSurface
import io.github.samuolis.domposable.ui.DomposableSwitch
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.fillMaxSize
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.verticalScroll
import io.github.samuolis.domposable.ui.width

private const val COMPARISON_PATH_SEGMENTS = 3

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
    return parts.size == COMPARISON_PATH_SEGMENTS && parts[0] == "compare" && parts[1] in ComparisonThemes &&
        parts[2] in ComparisonFamilies
}

/** The same route, theme, state and composition on both appliers; no application chrome. */
@Composable
internal fun MaterialComparisonScreen(path: String) {
    val (_, theme, family) = path.split('/')
    val dark = theme.endsWith("dark")
    val branded = theme.startsWith("brand")
    val base = materialCatalogTokens(theme.startsWith("expressive"), theme.startsWith("overrides"))
    val palette = if (dark) DarkComparisonColors else LightComparisonColors
    val colors = if (branded) palette.copy(primary = if (dark) DarkBrandPrimary else LightBrandPrimary) else palette
    MaterialComparisonFont {
        DomposableMaterialTheme(
            colors = colors,
            typography = base.typography.copy(fontFamily = "DomposableComparison, sans-serif"),
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
            DomposableSurface(DomposableModifier.fillMaxSize(), color = colors.surface) {
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
    }
}

@Composable
internal expect fun MaterialComparisonFont(content: @Composable () -> Unit)

private val LightBrandPrimary = DomposableColor(0xFF006B5F)
private val DarkBrandPrimary = DomposableColor(0xFF83D5C5)
private val LightComparisonColors =
    DemoTokens.colors.copy(
        onSurfaceVariant = DomposableColor(0xFF49454F),
        secondaryContainer = DomposableColor(0xFFE8DEF8),
        onSecondaryContainer = DomposableColor(0xFF1D192B),
        outline = DomposableColor(0xFF79747E),
        outlineVariant = DomposableColor(0xFFCAC4D0),
        error = DomposableColor(0xFFB3261E),
    )
private val DarkComparisonColors =
    DemoTokens.colors.copy(
        primary = DomposableColor(0xFFC8BFFF),
        onPrimary = DomposableColor(0xFF2F2157),
        surface = DomposableColor(0xFF141218),
        onSurface = DomposableColor(0xFFE6E0E9),
        surfaceVariant = DomposableColor(0xFF49454F),
        onSurfaceVariant = DomposableColor(0xFFCAC4D0),
        secondary = DomposableColor(0xFFCCC2DC),
        onSecondary = DomposableColor(0xFF332D41),
        secondaryContainer = DomposableColor(0xFF49445C),
        onSecondaryContainer = DomposableColor(0xFFE8DEF8),
        primaryContainer = DomposableColor(0xFF4F378B),
        onPrimaryContainer = DomposableColor(0xFFEADDFF),
        tertiary = DomposableColor(0xFFEFB8C8),
        onTertiary = DomposableColor(0xFF492532),
        tertiaryContainer = DomposableColor(0xFF633B48),
        onTertiaryContainer = DomposableColor(0xFFFFD8E4),
        outline = DomposableColor(0xFF938F99),
        outlineVariant = DomposableColor(0xFF49454F),
        error = DomposableColor(0xFFF2B8B5),
        onError = DomposableColor(0xFF601410),
        errorContainer = DomposableColor(0xFF8C1D18),
        onErrorContainer = DomposableColor(0xFFF9DEDC),
        background = DomposableColor(0xFF141218),
        onBackground = DomposableColor(0xFFE6E0E9),
        inverseSurface = DomposableColor(0xFFE6E0E9),
        inverseOnSurface = DomposableColor(0xFF322F35),
        inversePrimary = DomposableColor(0xFF6750A4),
        surfaceTint = DomposableColor(0xFFC8BFFF),
        surfaceBright = DomposableColor(0xFF3B383E),
        surfaceDim = DomposableColor(0xFF141218),
        surfaceContainer = DomposableColor(0xFF211F26),
        surfaceContainerHigh = DomposableColor(0xFF2B2930),
        surfaceContainerHighest = DomposableColor(0xFF36343B),
        surfaceContainerLow = DomposableColor(0xFF1D1B20),
        surfaceContainerLowest = DomposableColor(0xFF0F0D13),
    )

internal fun materialCatalogPath(path: String): Boolean {
    if (!path.startsWith("/material/")) return false
    val section = path.removePrefix("/material/")
    return section in MaterialSections || materialComparisonPath(section)
}

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
        for ((value, label) in listOf(0f to "0", 0.5f to "0.5", 1f to "1")) {
            ComparisonRow("${if (enabled) "Value" else "Disabled"} $label") {
                DomposableSlider(
                    value,
                    {},
                    "Slider $label",
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
        ComparisonRow(if (enabled) "Range" else "Disabled range") {
            DomposableRangeSlider(
                0.2f..0.8f,
                {},
                "Range start",
                "Range end",
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
