package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableInteractionAppearance
import io.github.samuolis.domposable.ui.DomposableDataTableSize
import org.jetbrains.compose.web.css.StyleScope

private const val TABLE_STATE_LAYER_PERCENT = 100

internal fun DomposableDataTableSize.htmlCellPadding(): String =
    when (this) {
        DomposableDataTableSize.Compact -> "4px 6px"
        DomposableDataTableSize.Standard -> "8px"
        DomposableDataTableSize.Expanded -> "12px"
    }

internal fun StyleScope.tableInteractionAppearance(appearance: DomposableInteractionAppearance) {
    appearance.hoveredContainer?.let { property("--d-data-hovered-container", it.css()) }
    appearance.hoveredContent?.let { property("--d-data-hovered-content", it.css()) }
    appearance.pressedContainer?.let { property("--d-data-pressed-container", it.css()) }
    appearance.pressedContent?.let { property("--d-data-pressed-content", it.css()) }
    appearance.stateLayerOpacity?.let {
        property(
            "--d-data-hover-feedback",
            "inset 0 0 0 999px color-mix(in srgb,currentColor ${it.hovered * TABLE_STATE_LAYER_PERCENT}%,transparent)",
        )
        property(
            "--d-data-pressed-feedback",
            "inset 0 0 0 999px color-mix(in srgb,currentColor ${it.pressed * TABLE_STATE_LAYER_PERCENT}%,transparent)",
        )
        property(
            "--d-data-focus-feedback",
            "inset 0 0 0 999px color-mix(in srgb,currentColor ${it.focused * TABLE_STATE_LAYER_PERCENT}%,transparent)",
        )
    }
    appearance.focusRingColor?.let { property("--d-data-focus-ring-color", it.css()) }
    appearance.focusRingWidth?.let { property("--d-data-focus-ring-width", "${it.value}px") }
    appearance.focusRingOffset?.let { property("--d-data-focus-ring-offset", "${it.value}px") }
}
