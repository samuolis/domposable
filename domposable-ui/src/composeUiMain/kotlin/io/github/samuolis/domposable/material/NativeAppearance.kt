package io.github.samuolis.domposable.material

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableCheckboxAppearance
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableColors
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableRadioButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableSliderColors
import io.github.samuolis.domposable.foundation.DomposableSwitchColors

internal fun DomposableColor.nativeColor(): Color = Color(argb)

internal data class NativeResolvedItemColors(
    val container: Color,
    val content: Color,
    val border: Color,
    val selectedContainer: Color,
    val selectedContent: Color,
    val selectedBorder: Color,
)

/** Resolves interactive style in disabled, pressed, selected, focused, hovered, default order. */
@Composable
internal fun DomposableItemAppearance.resolveInteractionColors(
    source: MutableInteractionSource,
    selected: Boolean,
    enabled: Boolean,
): NativeResolvedItemColors {
    val pressed by source.collectIsPressedAsState()
    val focused by source.collectIsFocusedAsState()
    val hovered by source.collectIsHoveredAsState()
    val colors = colors

    fun color(
        interactive: DomposableColor?,
        selectedValue: DomposableColor?,
        base: DomposableColor?,
    ): Color = (interactive ?: selectedValue ?: base)?.nativeColor() ?: Color.Unspecified
    if (!enabled) {
        val disabledContainer = color(null, colors.disabledContainer, null)
        val disabledContent = color(null, colors.disabledContent, null)
        val disabledBorder = color(null, colors.disabledBorder, null)
        val disabledSelectedContainer =
            color(null, colors.disabledSelectedContainer, colors.disabledContainer)
        val disabledSelectedContent =
            color(null, colors.disabledSelectedContent, colors.disabledContent)
        val disabledSelectedBorder =
            color(null, colors.disabledSelectedBorder, colors.disabledBorder)
        return NativeResolvedItemColors(
            disabledContainer,
            disabledContent,
            disabledBorder,
            disabledSelectedContainer,
            disabledSelectedContent,
            disabledSelectedBorder,
        )
    }
    val activeContainer =
        when {
            pressed -> colors.pressedContainer
            selected -> colors.selectedContainer
            focused -> colors.focusedContainer
            hovered -> colors.hoveredContainer
            else -> null
        }
    val activeContent =
        when {
            pressed -> colors.pressedContent
            selected -> colors.selectedContent
            focused -> colors.focusedContent
            hovered -> colors.hoveredContent
            else -> null
        }
    val activeBorder =
        when {
            pressed -> colors.pressedBorder
            selected -> colors.selectedBorder
            focused -> colors.focusedBorder
            hovered -> colors.hoveredBorder
            else -> null
        }
    val active = pressed || selected || focused || hovered
    val genericContainer = color(activeContainer, if (selected) colors.selectedContainer else null, colors.container)
    val genericContent = color(activeContent, if (selected) colors.selectedContent else null, colors.content)
    val genericBorder = color(activeBorder, if (selected) colors.selectedBorder else null, colors.border)
    val interacted = pressed || focused || hovered
    val selectedContainer =
        if (interacted) {
            genericContainer
        } else {
            color(
                null,
                colors.selectedContainer,
                colors.container,
            )
        }
    val selectedContent = if (interacted) genericContent else color(null, colors.selectedContent, colors.content)
    val selectedBorder = if (interacted) genericBorder else color(null, colors.selectedBorder, colors.border)
    return NativeResolvedItemColors(
        genericContainer,
        genericContent,
        genericBorder,
        selectedContainer,
        selectedContent,
        selectedBorder,
    )
}

internal fun DomposableInsets.nativePadding(): PaddingValues =
    PaddingValues(start.value.dp, top.value.dp, end.value.dp, bottom.value.dp)

@Composable
internal fun DomposableSliderColors.nativeSliderColors() =
    SliderDefaults.colors(
        thumbColor = thumb?.nativeColor() ?: Color.Unspecified,
        activeTrackColor = activeTrack?.nativeColor() ?: Color.Unspecified,
        inactiveTrackColor = inactiveTrack?.nativeColor() ?: Color.Unspecified,
        activeTickColor = activeTick?.nativeColor() ?: Color.Unspecified,
        inactiveTickColor = inactiveTick?.nativeColor() ?: Color.Unspecified,
        disabledThumbColor = disabledThumb?.nativeColor() ?: Color.Unspecified,
        disabledActiveTrackColor = disabledActiveTrack?.nativeColor() ?: Color.Unspecified,
        disabledInactiveTrackColor = disabledInactiveTrack?.nativeColor() ?: Color.Unspecified,
        disabledActiveTickColor = disabledActiveTick?.nativeColor() ?: Color.Unspecified,
        disabledInactiveTickColor = disabledInactiveTick?.nativeColor() ?: Color.Unspecified,
    )

@Composable
internal fun DomposableCheckboxAppearance.nativeCheckboxColors(enabled: Boolean = true) =
    CheckboxDefaults.colors(
        checkedCheckmarkColor =
            (if (enabled) checkmark else disabledCheckmark ?: checkmark)?.nativeColor() ?: Color.Unspecified,
        disabledCheckmarkColor = disabledCheckmark?.nativeColor() ?: Color.Unspecified,
        checkedBoxColor = colors.selected?.nativeColor() ?: Color.Unspecified,
        disabledCheckedBoxColor = colors.disabledSelected?.nativeColor() ?: Color.Unspecified,
        disabledIndeterminateBoxColor = colors.disabledSelected?.nativeColor() ?: Color.Unspecified,
        checkedBorderColor = colors.selected?.nativeColor() ?: Color.Unspecified,
        uncheckedBorderColor = colors.unselected?.nativeColor() ?: Color.Unspecified,
        disabledBorderColor = colors.disabledSelected?.nativeColor() ?: Color.Unspecified,
        disabledUncheckedBorderColor = colors.disabledUnselected?.nativeColor() ?: Color.Unspecified,
        disabledIndeterminateBorderColor = colors.disabledSelected?.nativeColor() ?: Color.Unspecified,
    )

@Composable
internal fun DomposableSwitchColors.nativeSwitchColors() =
    SwitchDefaults.colors().copy(
        checkedThumbColor = checkedThumb?.nativeColor() ?: Color.Unspecified,
        checkedTrackColor = checkedTrack?.nativeColor() ?: Color.Unspecified,
        checkedBorderColor = checkedBorder?.nativeColor() ?: Color.Unspecified,
        checkedIconColor = checkedIcon?.nativeColor() ?: Color.Unspecified,
        uncheckedThumbColor = uncheckedThumb?.nativeColor() ?: Color.Unspecified,
        uncheckedTrackColor = uncheckedTrack?.nativeColor() ?: Color.Unspecified,
        uncheckedBorderColor = uncheckedBorder?.nativeColor() ?: Color.Unspecified,
        uncheckedIconColor = uncheckedIcon?.nativeColor() ?: Color.Unspecified,
        disabledCheckedThumbColor = disabledCheckedThumb?.nativeColor() ?: Color.Unspecified,
        disabledCheckedTrackColor = disabledCheckedTrack?.nativeColor() ?: Color.Unspecified,
        disabledCheckedBorderColor = disabledCheckedBorder?.nativeColor() ?: Color.Unspecified,
        disabledCheckedIconColor = disabledCheckedIcon?.nativeColor() ?: Color.Unspecified,
        disabledUncheckedThumbColor = disabledUncheckedThumb?.nativeColor() ?: Color.Unspecified,
        disabledUncheckedTrackColor = disabledUncheckedTrack?.nativeColor() ?: Color.Unspecified,
        disabledUncheckedBorderColor = disabledUncheckedBorder?.nativeColor() ?: Color.Unspecified,
        disabledUncheckedIconColor = disabledUncheckedIcon?.nativeColor() ?: Color.Unspecified,
    )

@Composable
internal fun DomposableRadioButtonAppearance.nativeRadioButtonColors() =
    RadioButtonDefaults.colors(
        selectedColor = colors.selected?.nativeColor() ?: Color.Unspecified,
        unselectedColor = colors.unselected?.nativeColor() ?: Color.Unspecified,
        disabledSelectedColor = colors.disabledSelected?.nativeColor() ?: Color.Unspecified,
        disabledUnselectedColor = colors.disabledUnselected?.nativeColor() ?: Color.Unspecified,
    )

@Composable
internal fun DomposableItemAppearance.nativeFilterChipColors() =
    FilterChipDefaults.filterChipColors(
        containerColor = colors.container?.nativeColor() ?: Color.Unspecified,
        labelColor = colors.content?.nativeColor() ?: Color.Unspecified,
        disabledContainerColor = colors.disabledContainer?.nativeColor() ?: Color.Unspecified,
        disabledLabelColor = colors.disabledContent?.nativeColor() ?: Color.Unspecified,
        selectedContainerColor = colors.selectedContainer?.nativeColor() ?: Color.Unspecified,
        selectedLabelColor = colors.selectedContent?.nativeColor() ?: Color.Unspecified,
    )

internal fun DomposableColors.nativeColorScheme(base: ColorScheme): ColorScheme =
    base.copy(
        errorContainer = errorContainer.nativeColorOr(base.errorContainer),
        onError = onError.nativeColorOr(base.onError),
        onErrorContainer = onErrorContainer.nativeColorOr(base.onErrorContainer),
        secondary = secondary.nativeColorOr(base.secondary),
        onSecondary = onSecondary.nativeColorOr(base.onSecondary),
        secondaryContainer = secondaryContainer.nativeColorOr(base.secondaryContainer),
        onSecondaryContainer = onSecondaryContainer.nativeColorOr(base.onSecondaryContainer),
        tertiary = tertiary.nativeColorOr(base.tertiary),
        onTertiary = onTertiary.nativeColorOr(base.onTertiary),
        tertiaryContainer = tertiaryContainer.nativeColorOr(base.tertiaryContainer),
        onTertiaryContainer = onTertiaryContainer.nativeColorOr(base.onTertiaryContainer),
        primaryContainer = primaryContainer.nativeColorOr(base.primaryContainer),
        onPrimaryContainer = onPrimaryContainer.nativeColorOr(base.onPrimaryContainer),
        onBackground = onBackground.nativeColorOr(base.onBackground),
        onSurfaceVariant = onSurfaceVariant.nativeColorOr(base.onSurfaceVariant),
        outlineVariant = outlineVariant.nativeColorOr(base.outlineVariant),
        inverseSurface = inverseSurface.nativeColorOr(base.inverseSurface),
        inverseOnSurface = inverseOnSurface.nativeColorOr(base.inverseOnSurface),
        inversePrimary = inversePrimary.nativeColorOr(base.inversePrimary),
        surfaceTint = surfaceTint.nativeColorOr(base.surfaceTint),
        scrim = scrim.nativeColorOr(base.scrim),
        surfaceBright = surfaceBright.nativeColorOr(base.surfaceBright),
        surfaceDim = surfaceDim.nativeColorOr(base.surfaceDim),
        surfaceContainer = surfaceContainer.nativeColorOr(base.surfaceContainer),
        surfaceContainerHigh = surfaceContainerHigh.nativeColorOr(base.surfaceContainerHigh),
        surfaceContainerHighest = surfaceContainerHighest.nativeColorOr(base.surfaceContainerHighest),
        surfaceContainerLow = surfaceContainerLow.nativeColorOr(base.surfaceContainerLow),
        surfaceContainerLowest = surfaceContainerLowest.nativeColorOr(base.surfaceContainerLowest),
        primaryFixed = primaryFixed.nativeColorOr(base.primaryFixed),
        primaryFixedDim = primaryFixedDim.nativeColorOr(base.primaryFixedDim),
        onPrimaryFixed = onPrimaryFixed.nativeColorOr(base.onPrimaryFixed),
        onPrimaryFixedVariant = onPrimaryFixedVariant.nativeColorOr(base.onPrimaryFixedVariant),
        secondaryFixed = secondaryFixed.nativeColorOr(base.secondaryFixed),
        secondaryFixedDim = secondaryFixedDim.nativeColorOr(base.secondaryFixedDim),
        onSecondaryFixed = onSecondaryFixed.nativeColorOr(base.onSecondaryFixed),
        onSecondaryFixedVariant = onSecondaryFixedVariant.nativeColorOr(base.onSecondaryFixedVariant),
        tertiaryFixed = tertiaryFixed.nativeColorOr(base.tertiaryFixed),
        tertiaryFixedDim = tertiaryFixedDim.nativeColorOr(base.tertiaryFixedDim),
        onTertiaryFixed = onTertiaryFixed.nativeColorOr(base.onTertiaryFixed),
        onTertiaryFixedVariant = onTertiaryFixedVariant.nativeColorOr(base.onTertiaryFixedVariant),
        background = background.nativeColorOr(base.background),
    )

private fun DomposableColor?.nativeColorOr(default: Color): Color = this?.nativeColor() ?: default
