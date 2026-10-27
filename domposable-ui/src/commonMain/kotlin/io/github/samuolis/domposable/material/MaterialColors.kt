package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableColors

// Match the pinned Material color scheme; the native oracle verifies every optional role.
internal fun DomposableColors.withMaterialColorDefaults(): DomposableColors =
    copy(
        onError = onError.orMaterial(LightOnError),
        errorContainer = errorContainer.orMaterial(LightErrorContainer),
        onErrorContainer = onErrorContainer.orMaterial(LightOnErrorContainer),
        secondary = secondary.orMaterial(LightSecondary),
        onSecondary = onSecondary.orMaterial(LightOnSecondary),
        secondaryContainer = secondaryContainer.orMaterial(surfaceVariant),
        onSecondaryContainer = onSecondaryContainer.orMaterial(onSurface),
        tertiary = tertiary.orMaterial(LightTertiary),
        onTertiary = onTertiary.orMaterial(LightOnTertiary),
        tertiaryContainer = tertiaryContainer.orMaterial(LightTertiaryContainer),
        onTertiaryContainer = onTertiaryContainer.orMaterial(LightOnTertiaryContainer),
        primaryContainer = primaryContainer.orMaterial(LightPrimaryContainer),
        onPrimaryContainer = onPrimaryContainer.orMaterial(LightOnPrimaryContainer),
        background = background.orMaterial(surface),
        onBackground = onBackground.orMaterial(LightOnBackground),
        onSurfaceVariant = onSurfaceVariant.orMaterial(LightOnSurfaceVariant),
        outlineVariant = outlineVariant.orMaterial(LightOutlineVariant),
        inverseSurface = inverseSurface.orMaterial(LightInverseSurface),
        inverseOnSurface = inverseOnSurface.orMaterial(LightInverseOnSurface),
        inversePrimary = inversePrimary.orMaterial(LightInversePrimary),
        surfaceTint = surfaceTint.orMaterial(primary),
        scrim = scrim.orMaterial(LightScrim),
        surfaceBright = surfaceBright.orMaterial(LightSurfaceBright),
        surfaceDim = surfaceDim.orMaterial(LightSurfaceDim),
        surfaceContainer = surfaceContainer.orMaterial(surfaceVariant),
        surfaceContainerHigh = surfaceContainerHigh.orMaterial(LightSurfaceContainerHigh),
        surfaceContainerHighest = surfaceContainerHighest.orMaterial(surfaceVariant),
        surfaceContainerLow = surfaceContainerLow.orMaterial(LightSurfaceContainerLow),
        surfaceContainerLowest = surfaceContainerLowest.orMaterial(LightSurfaceContainerLowest),
        primaryFixed = primaryFixed.orMaterial(LightPrimaryFixed),
        primaryFixedDim = primaryFixedDim.orMaterial(LightPrimaryFixedDim),
        onPrimaryFixed = onPrimaryFixed.orMaterial(LightOnPrimaryFixed),
        onPrimaryFixedVariant = onPrimaryFixedVariant.orMaterial(LightOnPrimaryFixedVariant),
        secondaryFixed = secondaryFixed.orMaterial(LightSecondaryFixed),
        secondaryFixedDim = secondaryFixedDim.orMaterial(LightSecondaryFixedDim),
        onSecondaryFixed = onSecondaryFixed.orMaterial(LightOnSecondaryFixed),
        onSecondaryFixedVariant = onSecondaryFixedVariant.orMaterial(LightOnSecondaryFixedVariant),
        tertiaryFixed = tertiaryFixed.orMaterial(LightTertiaryFixed),
        tertiaryFixedDim = tertiaryFixedDim.orMaterial(LightTertiaryFixedDim),
        onTertiaryFixed = onTertiaryFixed.orMaterial(LightOnTertiaryFixed),
        onTertiaryFixedVariant = onTertiaryFixedVariant.orMaterial(LightOnTertiaryFixedVariant),
    )

private val LightOnError = DomposableColor(0xFFFFFFFF)
private val LightErrorContainer = DomposableColor(0xFFF9DEDC)
private val LightOnErrorContainer = DomposableColor(0xFF410E0B)
private val LightSecondary = DomposableColor(0xFF625B71)
private val LightOnSecondary = DomposableColor(0xFFFFFFFF)
private val LightTertiary = DomposableColor(0xFF7D5260)
private val LightOnTertiary = DomposableColor(0xFFFFFFFF)
private val LightTertiaryContainer = DomposableColor(0xFFFFD8E4)
private val LightOnTertiaryContainer = DomposableColor(0xFF31111D)
private val LightPrimaryContainer = DomposableColor(0xFFEADDFF)
private val LightOnPrimaryContainer = DomposableColor(0xFF21005D)
private val LightOnBackground = DomposableColor(0xFF1D1B20)
private val LightOnSurfaceVariant = DomposableColor(0xFF49454F)
private val LightOutlineVariant = DomposableColor(0xFFCAC4D0)
private val LightInverseSurface = DomposableColor(0xFF322F35)
private val LightInverseOnSurface = DomposableColor(0xFFF5EFF7)
private val LightInversePrimary = DomposableColor(0xFFD0BCFF)
private val LightScrim = DomposableColor(0xFF000000)
private val LightSurfaceBright = DomposableColor(0xFFFEF7FF)
private val LightSurfaceDim = DomposableColor(0xFFDED8E1)
private val LightSurfaceContainerHigh = DomposableColor(0xFFECE6F0)
private val LightSurfaceContainerLow = DomposableColor(0xFFF7F2FA)
private val LightSurfaceContainerLowest = DomposableColor(0xFFFFFFFF)
private val LightPrimaryFixed = DomposableColor(0xFFEADDFF)
private val LightPrimaryFixedDim = DomposableColor(0xFFD0BCFF)
private val LightOnPrimaryFixed = DomposableColor(0xFF21005D)
private val LightOnPrimaryFixedVariant = DomposableColor(0xFF4F378B)
private val LightSecondaryFixed = DomposableColor(0xFFE8DEF8)
private val LightSecondaryFixedDim = DomposableColor(0xFFCCC2DC)
private val LightOnSecondaryFixed = DomposableColor(0xFF1D192B)
private val LightOnSecondaryFixedVariant = DomposableColor(0xFF4A4458)
private val LightTertiaryFixed = DomposableColor(0xFFFFD8E4)
private val LightTertiaryFixedDim = DomposableColor(0xFFEFB8C8)
private val LightOnTertiaryFixed = DomposableColor(0xFF31111D)
private val LightOnTertiaryFixedVariant = DomposableColor(0xFF633B48)

private fun DomposableColor?.orMaterial(fallback: DomposableColor): DomposableColor = this ?: fallback
