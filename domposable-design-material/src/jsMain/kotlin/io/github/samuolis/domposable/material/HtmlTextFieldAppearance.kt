package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableTextFieldAppearance
import org.jetbrains.compose.web.css.StyleScope

private const val DEFAULT_INPUT_LABEL_FONT_SIZE = 14f
private const val DEFAULT_INPUT_PLACEHOLDER_FONT_SIZE = 16f

internal fun StyleScope.textFieldAppearance(appearance: DomposableTextFieldAppearance) {
    textFieldTextColors(appearance)
    textFieldContainerColors(appearance)
    textFieldOutlineColors(appearance)
    textFieldGeometry(appearance)
    textFieldTypography(appearance)
}

private fun StyleScope.textFieldTextColors(appearance: DomposableTextFieldAppearance) {
    appearance.colors.text.unfocused
        ?.let { property("--d-input-text", it.css()) }
    appearance.colors.text.focused
        ?.let { property("--d-input-text-focused", it.css()) }
    appearance.colors.text.disabled
        ?.let { property("--d-input-text-disabled", it.css()) }
    appearance.colors.cursor.unfocused
        ?.let { property("--d-input-cursor", it.css()) }
}

private fun StyleScope.textFieldContainerColors(appearance: DomposableTextFieldAppearance) {
    appearance.colors.container.unfocused
        ?.let { property("--d-input-container", it.css()) }
    appearance.colors.container.focused
        ?.let { property("--d-input-container-focused", it.css()) }
    appearance.colors.container.disabled
        ?.let { property("--d-input-container-disabled", it.css()) }
}

private fun StyleScope.textFieldOutlineColors(appearance: DomposableTextFieldAppearance) {
    appearance.colors.outline.unfocused
        ?.let { property("--d-input-outline", it.css()) }
    appearance.colors.outline.focused
        ?.let { property("--d-input-outline-focused", it.css()) }
    appearance.colors.outline.disabled
        ?.let { property("--d-input-outline-disabled", it.css()) }
}

private fun StyleScope.textFieldGeometry(appearance: DomposableTextFieldAppearance) {
    appearance.shape?.let { materialShape(it) }
    appearance.contentPadding?.let { property("--d-input-padding", it.cssPadding()) }
    appearance.minHeight?.let { property("--d-input-min-height", "${it.value}px") }
    appearance.outlineWidth?.let { property("--d-input-outline-width", "${it.value}px") }
    appearance.focusedOutlineWidth?.let { property("--d-input-outline-focused-width", "${it.value}px") }
}

private fun StyleScope.textFieldTypography(appearance: DomposableTextFieldAppearance) {
    appearance.textAppearance?.let { applyCatalogTextAppearance(it) }
    appearance.labelAppearance?.let {
        property("--d-input-label-size", "${it.fontSize ?: DEFAULT_INPUT_LABEL_FONT_SIZE}px")
    }
    appearance.placeholderAppearance?.let {
        property("--d-input-placeholder-size", "${it.fontSize ?: DEFAULT_INPUT_PLACEHOLDER_FONT_SIZE}px")
    }
}
