package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.DomposableTypographyRole
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.StyleScope
import org.w3c.dom.HTMLElement

/** Applies only Domposable token custom properties to an existing HTML host element. */
fun AttrsScope<HTMLElement>.domposableTokenVariables(tokens: DomposableTokens) {
    style { applyTokenVariables(tokens) }
}

private data class CssTokenColors(
    val name: String,
    val fallbacks: List<DomposableColor?>,
)

private fun StyleScope.applyTokenVariables(tokens: DomposableTokens) {
    applyCoreColorTokens(tokens.colors)
    applyFixedColorTokens(tokens.colors)
    applyTypographyTokens(tokens)
    applyMetricTokens(tokens)
    applyShapeTokens(tokens)
}

private fun StyleScope.applyCoreColorTokens(colors: io.github.samuolis.domposable.foundation.DomposableColors) {
    val variables =
        listOf(
            CssTokenColors("--d-primary", listOf(colors.primary)),
            CssTokenColors("--d-on-primary", listOf(colors.onPrimary)),
            CssTokenColors("--d-surface", listOf(colors.surface)),
            CssTokenColors("--d-on-surface", listOf(colors.onSurface)),
            CssTokenColors("--d-variant", listOf(colors.surfaceVariant)),
            CssTokenColors("--d-outline", listOf(colors.outline)),
            CssTokenColors("--d-error", listOf(colors.error)),
            CssTokenColors("--d-on-error", listOf(colors.onError, colors.surface)),
            CssTokenColors("--d-error-container", listOf(colors.errorContainer, colors.error)),
            CssTokenColors("--d-on-error-container", listOf(colors.onErrorContainer, colors.surface)),
            CssTokenColors("--d-secondary", listOf(colors.secondary, colors.primary)),
            CssTokenColors("--d-on-secondary", listOf(colors.onSecondary, colors.onPrimary)),
            CssTokenColors("--d-secondary-container", listOf(colors.secondaryContainer, colors.surfaceVariant)),
            CssTokenColors("--d-on-secondary-container", listOf(colors.onSecondaryContainer, colors.onSurface)),
            CssTokenColors("--d-tertiary", listOf(colors.tertiary, colors.primary)),
            CssTokenColors("--d-on-tertiary", listOf(colors.onTertiary, colors.onPrimary)),
            CssTokenColors("--d-tertiary-container", listOf(colors.tertiaryContainer, colors.surfaceVariant)),
            CssTokenColors("--d-on-tertiary-container", listOf(colors.onTertiaryContainer, colors.onSurface)),
            CssTokenColors("--d-primary-container", listOf(colors.primaryContainer, colors.surfaceVariant)),
            CssTokenColors("--d-on-primary-container", listOf(colors.onPrimaryContainer, colors.onSurface)),
            CssTokenColors("--d-background", listOf(colors.background, colors.surface)),
            CssTokenColors("--d-on-background", listOf(colors.onBackground, colors.onSurface)),
            CssTokenColors("--d-on-surface-variant", listOf(colors.onSurfaceVariant, colors.onSurface)),
            CssTokenColors("--d-outline-variant", listOf(colors.outlineVariant, colors.outline)),
            CssTokenColors("--d-inverse-surface", listOf(colors.inverseSurface, colors.onSurface)),
            CssTokenColors("--d-inverse-on-surface", listOf(colors.inverseOnSurface, colors.surface)),
            CssTokenColors("--d-inverse-primary", listOf(colors.inversePrimary, colors.primary)),
            CssTokenColors("--d-surface-tint", listOf(colors.surfaceTint, colors.primary)),
            CssTokenColors("--d-scrim", listOf(colors.scrim, colors.onSurface)),
            CssTokenColors("--d-surface-bright", listOf(colors.surfaceBright, colors.surface)),
            CssTokenColors("--d-surface-dim", listOf(colors.surfaceDim, colors.surfaceVariant)),
            CssTokenColors("--d-surface-container", listOf(colors.surfaceContainer, colors.surfaceVariant)),
            CssTokenColors(
                "--d-surface-container-high",
                listOf(colors.surfaceContainerHigh, colors.surfaceVariant),
            ),
            CssTokenColors(
                "--d-surface-container-highest",
                listOf(colors.surfaceContainerHighest, colors.surfaceVariant),
            ),
            CssTokenColors("--d-surface-container-low", listOf(colors.surfaceContainerLow, colors.surface)),
            CssTokenColors("--d-surface-container-lowest", listOf(colors.surfaceContainerLowest, colors.surface)),
        )
    applyColorVariables(variables)
}

private fun StyleScope.applyFixedColorTokens(colors: io.github.samuolis.domposable.foundation.DomposableColors) {
    val variables =
        listOf(
            CssTokenColors("--d-primary-fixed", listOf(colors.primaryFixed, colors.primaryContainer, colors.primary)),
            CssTokenColors(
                "--d-primary-fixed-dim",
                listOf(colors.primaryFixedDim, colors.primaryContainer, colors.primary),
            ),
            CssTokenColors(
                "--d-on-primary-fixed",
                listOf(colors.onPrimaryFixed, colors.onPrimaryContainer, colors.onPrimary),
            ),
            CssTokenColors(
                "--d-on-primary-fixed-variant",
                listOf(colors.onPrimaryFixedVariant, colors.onPrimaryContainer, colors.onPrimary),
            ),
            CssTokenColors(
                "--d-secondary-fixed",
                listOf(colors.secondaryFixed, colors.secondaryContainer, colors.surfaceVariant),
            ),
            CssTokenColors(
                "--d-secondary-fixed-dim",
                listOf(colors.secondaryFixedDim, colors.secondaryContainer, colors.surfaceVariant),
            ),
            CssTokenColors(
                "--d-on-secondary-fixed",
                listOf(colors.onSecondaryFixed, colors.onSecondaryContainer, colors.onSurface),
            ),
            CssTokenColors(
                "--d-on-secondary-fixed-variant",
                listOf(colors.onSecondaryFixedVariant, colors.onSecondaryContainer, colors.onSurface),
            ),
            CssTokenColors(
                "--d-tertiary-fixed",
                listOf(colors.tertiaryFixed, colors.tertiaryContainer, colors.surfaceVariant),
            ),
            CssTokenColors(
                "--d-tertiary-fixed-dim",
                listOf(colors.tertiaryFixedDim, colors.tertiaryContainer, colors.surfaceVariant),
            ),
            CssTokenColors(
                "--d-on-tertiary-fixed",
                listOf(colors.onTertiaryFixed, colors.onTertiaryContainer, colors.onSurface),
            ),
            CssTokenColors(
                "--d-on-tertiary-fixed-variant",
                listOf(colors.onTertiaryFixedVariant, colors.onTertiaryContainer, colors.onSurface),
            ),
        )
    applyColorVariables(variables)
}

private fun StyleScope.applyColorVariables(variables: List<CssTokenColors>) {
    variables.forEach { variable ->
        property(variable.name, variable.fallbacks.firstNotNullOf { it }.css())
    }
}

private fun StyleScope.applyTypographyTokens(tokens: DomposableTokens) {
    val typography = tokens.typography
    property("--d-font", typography.fontFamily)
    property("--d-body", "${typography.bodySize}px")
    property("--d-title", "${typography.titleSize}px")
    property("--d-label", "${typography.labelSize}px")
    property("--d-line-height", typography.lineHeight.toString())
    DomposableTypographyRole.values().forEach { role ->
        val roleName = role.name.toKebabCase()
        applyTypographyRole("--d-type-$roleName", typography.roles[role])
        applyTypographyRole("--d-type-$roleName-emphasized", typography.emphasizedRoles[role])
    }
}

private fun StyleScope.applyTypographyRole(
    prefix: String,
    appearance: DomposableTextAppearance?,
) {
    appearance?.fontSize?.let { property("$prefix-size", "${it}px") }
    appearance?.lineHeight?.let { property("$prefix-line-height", "${it}px") }
    appearance?.fontWeight?.let { property("$prefix-weight", it.css()) }
    appearance?.letterSpacing?.let { property("$prefix-letter-spacing", "${it}px") }
}

private fun StyleScope.applyMetricTokens(tokens: DomposableTokens) {
    property("--d-spacing", "${tokens.spacing.medium.value}px")
    property("--d-radius", "${tokens.radius.value}px")
    property("--d-elevation", "${tokens.elevation.value}px")
    property("--d-motion", "${tokens.motionDurationMillis}ms")
}

private fun StyleScope.applyShapeTokens(tokens: DomposableTokens) {
    property("--d-shape-extra-small", tokens.shapes.extraSmall?.cssRadius() ?: "4px")
    property("--d-shape-small", tokens.shapes.small?.cssRadius() ?: "8px")
    property("--d-shape-medium", tokens.shapes.medium?.cssRadius() ?: "12px")
    property("--d-shape-large", tokens.shapes.large?.cssRadius() ?: "16px")
    property("--d-shape-extra-large", tokens.shapes.extraLarge?.cssRadius() ?: "28px")
    property("--d-shape-large-increased", tokens.shapes.largeIncreased?.cssRadius() ?: "20px")
    property("--d-shape-extra-large-increased", tokens.shapes.extraLargeIncreased?.cssRadius() ?: "32px")
    property("--d-shape-extra-extra-large", tokens.shapes.extraExtraLarge?.cssRadius() ?: "48px")
}
