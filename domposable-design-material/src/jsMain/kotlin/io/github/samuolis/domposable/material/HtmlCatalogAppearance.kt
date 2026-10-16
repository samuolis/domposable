package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableItemColors
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableSliderColors
import io.github.samuolis.domposable.foundation.DomposableStateLayerOpacity
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import org.jetbrains.compose.web.css.StyleScope

internal fun DomposableShape.cssRadius(): String =
    when (this) {
        DomposableShape.Rectangle -> {
            "0"
        }

        DomposableShape.Circle -> {
            "9999px"
        }

        is DomposableShape.Rounded -> {
            "${radius.value}px"
        }

        is DomposableShape.Corners -> {
            "${topStart.value}px ${topEnd.value}px ${bottomEnd.value}px ${bottomStart.value}px"
        }
    }

internal fun DomposableInsets.cssPadding(): String = "${top.value}px ${end.value}px ${bottom.value}px ${start.value}px"

internal fun StyleScope.surfaceAppearance(appearance: DomposableSurfaceAppearance) {
    appearance.container?.let { property("background-color", it.css()) }
    appearance.content?.let { property("color", it.css()) }
    appearance.border?.let { property("border", "${appearance.borderWidth.value}px solid ${it.css()}") }
    appearance.shape?.let { materialShape(it) }
    appearance.tonalElevation?.takeIf { appearance.container == null }?.let {
        val amount = if (it.value <= 0f) 0f else (4.5f * kotlin.math.ln(it.value + 1f) + 2f).coerceAtMost(100f)
        val surface = appearance.container?.css() ?: "var(--d-surface)"
        val tintedSurface = "color-mix(in srgb, var(--d-surface-tint, var(--d-primary)) $amount%, $surface)"
        property(
            "background-color",
            tintedSurface,
        )
    }
    appearance.shadowElevation?.let {
        property("box-shadow", if (it.value == 0f) "none" else "0 ${it.value}px ${it.value * 2f}px rgb(0 0 0 / .12)")
    }
    appearance.textAppearance?.let { applyCatalogTextAppearance(it) }
}

internal fun StyleScope.itemAppearance(
    appearance: DomposableItemAppearance,
    selected: Boolean = false,
) {
    itemColors(appearance.colors)
    itemShape(appearance.shape)
    if (appearance.borderWidth.value != 1f) property("--d-item-border-width", "${appearance.borderWidth.value}px")
    appearance.selectedBorderWidth?.let { property("--d-item-selected-border-width", "${it.value}px") }
    if (selected) {
        property("border-width", "${appearance.selectedBorderWidth?.value ?: appearance.borderWidth.value}px")
    }
    itemGeometry(appearance)
    appearance.disabledOpacity?.let { property("--d-item-disabled-opacity", it.toString()) }
    appearance.stateLayerOpacity?.let { applyStateLayerOpacity(it) }
    appearance.textAppearance?.let { applyCatalogTextAppearance(it) }
    if (selected) appearance.selectedTextAppearance?.let { applyCatalogTextAppearance(it) }
}

private const val PERCENT_SCALE = 100

private fun StyleScope.itemShape(shape: DomposableShape?) {
    shape?.let {
        property("--d-item-radius", it.cssRadius())
        materialShape(it)
        if (it is DomposableShape.Corners) {
            property("--d-item-radius-start-start", "${it.topStart.value}px")
            property("--d-item-radius-start-end", "${it.topEnd.value}px")
            property("--d-item-radius-end-end", "${it.bottomEnd.value}px")
            property("--d-item-radius-end-start", "${it.bottomStart.value}px")
        }
    }
}

private fun StyleScope.itemGeometry(appearance: DomposableItemAppearance) {
    appearance.contentPadding?.let { property("--d-item-padding", it.cssPadding()) }
    appearance.minHeight?.let { property("--d-item-min-height", "${it.value}px") }
    appearance.contentSpacing?.let { property("--d-item-gap", "${it.value}px") }
    appearance.minimumInteractiveSize?.let { property("--d-item-min-interactive", "${it.value}px") }
}

private fun StyleScope.applyStateLayerOpacity(opacity: DomposableStateLayerOpacity) {
    property("--d-item-hover-opacity", "${opacity.hovered * PERCENT_SCALE}%")
    property("--d-item-focus-opacity", "${opacity.focused * PERCENT_SCALE}%")
    property("--d-item-pressed-opacity", "${opacity.pressed * PERCENT_SCALE}%")
    property("--d-item-dragged-opacity", "${opacity.dragged * PERCENT_SCALE}%")
}

internal fun StyleScope.itemColors(colors: DomposableItemColors) {
    listOf(
        "container" to colors.container,
        "content" to colors.content,
        "selected-container" to colors.selectedContainer,
        "selected-content" to colors.selectedContent,
        "disabled-container" to colors.disabledContainer,
        "disabled-content" to colors.disabledContent,
        "border" to colors.border,
        "selected-border" to colors.selectedBorder,
        "disabled-border" to colors.disabledBorder,
        "focused-container" to colors.focusedContainer,
        "focused-content" to colors.focusedContent,
        "focused-border" to colors.focusedBorder,
        "pressed-container" to colors.pressedContainer,
        "pressed-content" to colors.pressedContent,
        "pressed-border" to colors.pressedBorder,
        "hovered-container" to colors.hoveredContainer,
        "hovered-content" to colors.hoveredContent,
        "hovered-border" to colors.hoveredBorder,
    ).forEach { (role, color) -> color?.let { property("--d-item-$role", it.css()) } }
}

internal fun StyleScope.sliderColors(
    colors: DomposableSliderColors,
    enabled: Boolean,
) {
    listOf(
        "thumb" to colors.thumb,
        "active" to colors.activeTrack,
        "inactive" to colors.inactiveTrack,
        "active-tick" to colors.activeTick,
        "inactive-tick" to colors.inactiveTick,
        "disabled-thumb" to colors.disabledThumb,
        "disabled-active" to colors.disabledActiveTrack,
        "disabled-inactive" to colors.disabledInactiveTrack,
        "disabled-active-tick" to colors.disabledActiveTick,
        "disabled-inactive-tick" to colors.disabledInactiveTick,
    ).forEach { (role, color) -> color?.let { property("--d-slider-$role", it.css()) } }
    property(
        "--d-slider-thumb-current",
        (if (enabled) colors.thumb else colors.disabledThumb)?.css() ?: "var(--d-primary)",
    )
    property(
        "--d-slider-active-current",
        (if (enabled) colors.activeTrack else colors.disabledActiveTrack)?.css() ?: "var(--d-primary)",
    )
    property(
        "--d-slider-inactive-current",
        (if (enabled) colors.inactiveTrack else colors.disabledInactiveTrack)?.css() ?: "var(--d-outline)",
    )
}

internal fun DomposableColor?.cssOr(fallback: String): String = this?.css() ?: fallback
