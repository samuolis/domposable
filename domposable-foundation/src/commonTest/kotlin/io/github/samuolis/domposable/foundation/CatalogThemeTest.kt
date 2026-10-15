package io.github.samuolis.domposable.foundation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CatalogThemeTest {
    @Test
    fun interactionOpacityCannotEscapeTheColorRange() {
        kotlin.test.assertFailsWith<IllegalArgumentException> { DomposableStateLayerOpacity(pressed = Float.NaN) }
        kotlin.test.assertFailsWith<IllegalArgumentException> { DomposableStateLayerOpacity(hovered = 1.1f) }
        kotlin.test.assertFailsWith<IllegalArgumentException> { DomposableItemAppearance(disabledOpacity = -0.1f) }
    }

    @Test
    fun localComponentCopyDoesNotChangeOtherComponentDefaults() {
        val brand =
            DomposableComponentStyles(
                card = DomposableSurfaceAppearance(container = DomposableColor(0xFF123456)),
                checkbox =
                    DomposableCheckboxAppearance(
                        colors = DomposableSelectionColors(selected = DomposableColor(0xFF334455)),
                    ),
            )
        val local = brand.copy(card = brand.card.copy(shape = DomposableShape.Rounded(20.dp)))
        assertNull(brand.card.shape)
        assertEquals(DomposableShape.Rounded(20.dp), local.card.shape)
        assertEquals(brand.card.container, local.card.container)
        assertEquals(brand.checkbox, local.checkbox)
    }

    @Test
    fun unspecifiedExpandedPaletteRolesPreserveRendererDefaults() {
        val palette = DomposableColors(primary = DomposableColor(0xFF123456))
        assertNull(palette.secondary)
        assertNull(palette.primaryFixed)
        val expanded = palette.copy(secondary = DomposableColor(0xFF445566), primaryFixed = DomposableColor(0xFF778899))
        assertEquals(palette.primary, expanded.primary)
        assertEquals(DomposableColor(0xFF445566), expanded.secondary)
    }

    @Test
    fun expressiveConfigurationPreservesCallerOwnedStateAndComponentOverrides() {
        val base =
            DomposableTokens(
                components =
                    DomposableComponentStyles(
                        progress = DomposableProgressAppearance(color = DomposableColor(0xFF123456)),
                    ),
            )
        val expressive =
            base.copy(
                designLanguage = DomposableDesignLanguage.Expressive,
                shapes = DomposableShapes(large = DomposableShape.Rounded(28.dp)),
                typography =
                    base.typography.copy(
                        roles =
                            mapOf(DomposableTypographyRole.HeadlineLarge to DomposableTextAppearance(fontSize = 32f)),
                    ),
            )
        assertEquals(base.components, expressive.components)
        assertEquals(
            32f,
            expressive.typography.roles
                .getValue(DomposableTypographyRole.HeadlineLarge)
                .fontSize,
        )
        assertEquals(DomposableDesignLanguage.Standard, base.designLanguage)
    }
}
