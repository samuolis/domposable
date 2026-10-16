package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import org.jetbrains.compose.web.css.StyleScope

internal fun StyleScope.styleAppearance(
    appearance: DomposableButtonAppearance,
    enabled: Boolean = true,
) {
    buttonColors(appearance, enabled)
    buttonGeometry(appearance)
    appearance.textAppearance?.let { applyCatalogTextAppearance(it) }
    appearance.indicatorSize?.let { property("--d-button-indicator-size", "${it.value}px") }
    appearance.indicatorStrokeWidth?.let { property("--d-button-indicator-stroke", "${it.value}px") }
    property("--d-button-pressed-scale", appearance.pressedScale.toString())
    property("--d-button-hovered-scale", appearance.hoveredScale.toString())
    if (appearance.container != null || appearance.content != null || appearance.border != null) {
        property("opacity", "1")
    }
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
