package io.github.samuolis.domposable.material

import androidx.compose.ui.graphics.toArgb
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableColors
import kotlin.test.Test
import kotlin.test.assertEquals

class MaterialColorParityTest {
    @Test
    fun htmlFallbacksMatchTheInstalledNativeMaterialColorScheme() {
        val colors = DomposableColors()
        val actual = colors.withMaterialColorDefaults()
        val expected = colors.nativeMaterialColorScheme()
        mapOf(
            "onError" to (expected.onError to actual.onError),
            "errorContainer" to (expected.errorContainer to actual.errorContainer),
            "onErrorContainer" to (expected.onErrorContainer to actual.onErrorContainer),
            "secondary" to (expected.secondary to actual.secondary),
            "onSecondary" to (expected.onSecondary to actual.onSecondary),
            "secondaryContainer" to (expected.secondaryContainer to actual.secondaryContainer),
            "onSecondaryContainer" to (expected.onSecondaryContainer to actual.onSecondaryContainer),
            "tertiary" to (expected.tertiary to actual.tertiary),
            "onTertiary" to (expected.onTertiary to actual.onTertiary),
            "tertiaryContainer" to (expected.tertiaryContainer to actual.tertiaryContainer),
            "onTertiaryContainer" to (expected.onTertiaryContainer to actual.onTertiaryContainer),
            "primaryContainer" to (expected.primaryContainer to actual.primaryContainer),
            "onPrimaryContainer" to (expected.onPrimaryContainer to actual.onPrimaryContainer),
            "background" to (expected.background to actual.background),
            "onBackground" to (expected.onBackground to actual.onBackground),
            "onSurfaceVariant" to (expected.onSurfaceVariant to actual.onSurfaceVariant),
            "outlineVariant" to (expected.outlineVariant to actual.outlineVariant),
            "inverseSurface" to (expected.inverseSurface to actual.inverseSurface),
            "inverseOnSurface" to (expected.inverseOnSurface to actual.inverseOnSurface),
            "inversePrimary" to (expected.inversePrimary to actual.inversePrimary),
            "surfaceTint" to (expected.surfaceTint to actual.surfaceTint),
            "scrim" to (expected.scrim to actual.scrim),
            "surfaceBright" to (expected.surfaceBright to actual.surfaceBright),
            "surfaceDim" to (expected.surfaceDim to actual.surfaceDim),
            "surfaceContainer" to (expected.surfaceContainer to actual.surfaceContainer),
            "surfaceContainerHigh" to (expected.surfaceContainerHigh to actual.surfaceContainerHigh),
            "surfaceContainerHighest" to (expected.surfaceContainerHighest to actual.surfaceContainerHighest),
            "surfaceContainerLow" to (expected.surfaceContainerLow to actual.surfaceContainerLow),
            "surfaceContainerLowest" to (expected.surfaceContainerLowest to actual.surfaceContainerLowest),
            "primaryFixed" to (expected.primaryFixed to actual.primaryFixed),
            "primaryFixedDim" to (expected.primaryFixedDim to actual.primaryFixedDim),
            "onPrimaryFixed" to (expected.onPrimaryFixed to actual.onPrimaryFixed),
            "onPrimaryFixedVariant" to (expected.onPrimaryFixedVariant to actual.onPrimaryFixedVariant),
            "secondaryFixed" to (expected.secondaryFixed to actual.secondaryFixed),
            "secondaryFixedDim" to (expected.secondaryFixedDim to actual.secondaryFixedDim),
            "onSecondaryFixed" to (expected.onSecondaryFixed to actual.onSecondaryFixed),
            "onSecondaryFixedVariant" to (expected.onSecondaryFixedVariant to actual.onSecondaryFixedVariant),
            "tertiaryFixed" to (expected.tertiaryFixed to actual.tertiaryFixed),
            "tertiaryFixedDim" to (expected.tertiaryFixedDim to actual.tertiaryFixedDim),
            "onTertiaryFixed" to (expected.onTertiaryFixed to actual.onTertiaryFixed),
            "onTertiaryFixedVariant" to (expected.onTertiaryFixedVariant to actual.onTertiaryFixedVariant),
        ).forEach { (name, pair) ->
            assertEquals(
                pair.first
                    .toArgb()
                    .toUInt()
                    .toLong(),
                pair.second!!.argb,
                name,
            )
        }
    }

    @Test
    fun explicitBrandRolesKeepTheirConfiguredColors() {
        val custom = DomposableColor(0xFF123456)
        val colors = DomposableColors(primaryContainer = custom, onError = custom, surfaceContainerLow = custom)
        val actual = colors.withMaterialColorDefaults()
        assertEquals(custom, actual.primaryContainer)
        assertEquals(custom, actual.onError)
        assertEquals(custom, actual.surfaceContainerLow)
    }
}
