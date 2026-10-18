package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import org.jetbrains.compose.web.css.StyleScope

private const val PERCENT_SCALE = 100

internal fun StyleScope.styleAppearance(
    appearance: DomposableButtonAppearance,
    enabled: Boolean = true,
) {
    buttonColors(appearance, enabled)
    buttonGeometry(appearance)
    appearance.textAppearance?.let { applyCatalogTextAppearance(it) }
    buttonInteractionAppearance(appearance)
    appearance.indicatorSize?.let { property("--d-button-indicator-size", "${it.value}px") }
    appearance.indicatorStrokeWidth?.let { property("--d-button-indicator-stroke", "${it.value}px") }
    property("--d-button-pressed-scale", appearance.pressedScale.toString())
    property("--d-button-hovered-scale", appearance.hoveredScale.toString())
    if (appearance.container != null || appearance.content != null || appearance.border != null) {
        property("opacity", "1")
    }
    if (!enabled) appearance.disabledOpacity?.let { property("opacity", it.toString()) }
}

internal fun StyleScope.buttonInteractionAppearance(appearance: DomposableButtonAppearance) {
    val interaction = appearance.interaction ?: return
    interaction.hoveredContainer?.let { property("--d-button-hovered-container", it.css()) }
    interaction.hoveredContent?.let { property("--d-button-hovered-content", it.css()) }
    interaction.pressedContainer?.let { property("--d-button-pressed-container", it.css()) }
    interaction.pressedContent?.let { property("--d-button-pressed-content", it.css()) }
    interaction.stateLayerOpacity?.let {
        if (interaction.hoveredContainer == null) {
            property(
                "--d-button-hover-feedback",
                "inset 0 0 0 999px color-mix(in srgb,currentColor ${it.hovered * PERCENT_SCALE}%,transparent)",
            )
        }
        if (interaction.pressedContainer == null) {
            property(
                "--d-button-pressed-feedback",
                "inset 0 0 0 999px color-mix(in srgb,currentColor ${it.pressed * PERCENT_SCALE}%,transparent)",
            )
        }
        property(
            "--d-button-focus-feedback",
            "inset 0 0 0 999px color-mix(in srgb,currentColor ${it.focused * PERCENT_SCALE}%,transparent)",
        )
    }
    interaction.focusRingColor?.let { property("--d-button-focus-ring-color", it.css()) }
    interaction.focusRingWidth?.let { property("--d-button-focus-ring-width", "${it.value}px") }
    interaction.focusRingOffset?.let { property("--d-button-focus-ring-offset", "${it.value}px") }
}

private fun StyleScope.buttonColors(
    appearance: DomposableButtonAppearance,
    enabled: Boolean,
) {
    (if (enabled) appearance.container else appearance.disabledContainer)?.let {
        property("background-color", it.css())
    }
    (if (enabled) appearance.content else appearance.disabledContent)?.let { property("color", it.css()) }
    (if (enabled) appearance.border else appearance.disabledBorder)?.let { property("border-color", it.css()) }
}

private fun StyleScope.buttonGeometry(appearance: DomposableButtonAppearance) {
    appearance.borderWidth?.let { property("border-width", "${it.value}px") }
    appearance.shape?.let { materialShape(it) }
    appearance.minHeight?.let { property("min-height", "${it.value}px") }
    appearance.minWidth?.let { property("min-width", "${it.value}px") }
    appearance.contentPadding?.let {
        property("padding-block", "${it.top.value}px ${it.bottom.value}px")
        property("padding-inline", "${it.start.value}px ${it.end.value}px")
    }
}
