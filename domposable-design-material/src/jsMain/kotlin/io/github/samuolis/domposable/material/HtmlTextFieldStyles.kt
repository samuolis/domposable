package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableStateColors
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextFieldAppearance
import io.github.samuolis.domposable.foundation.DomposableTextFieldVariant
import io.github.samuolis.domposable.ui.DomposableTextFieldProps
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.Span
import org.w3c.dom.HTMLElement

// Each optional appearance field independently overrides its CSS property.
@Suppress("CyclomaticComplexMethod")
internal fun <T : HTMLElement> AttrsScope<T>.inputAppearance(
    appearance: DomposableTextFieldAppearance,
    props: DomposableTextFieldProps,
    focused: Boolean,
    insideFloatingControl: Boolean,
) {
    val colors = appearance.colors
    val stateText = colors.text.color(props.enabled, props.isError, focused)
    val stateContainer = colors.container.color(props.enabled, props.isError, focused)
    val stateOutline = colors.outline.color(props.enabled, props.isError, focused)
    style {
        stateText?.let { property("color", it.css()) }
        if (insideFloatingControl) {
            property("background", "transparent")
            property("border", "0")
            property("border-radius", "0")
            property("outline", "0")
            property("box-sizing", "border-box")
        } else {
            stateContainer?.let { property("background-color", it.css()) }
            stateOutline?.let { property("border-color", it.css()) }
        }
        colors.cursor.color(props.enabled, props.isError, focused)?.let { property("caret-color", it.css()) }
        colors.placeholder
            .color(
                props.enabled,
                props.isError,
                focused,
            )?.let { property("--d-field-placeholder", it.css()) }
        appearance.textAppearance?.let { applyTextAppearance(it) }
        appearance.placeholderAppearance?.let { placeholder ->
            placeholder.fontSize?.let { property("--d-field-placeholder-size", "${it}px") }
            placeholder.lineHeight?.let { property("--d-field-placeholder-line", "${it}px") }
            placeholder.fontWeight?.let { property("--d-field-placeholder-weight", it.css()) }
            placeholder.letterSpacing?.let { property("--d-field-placeholder-spacing", "${it}px") }
        }
        if (insideFloatingControl) {
            floatingInputGeometry(appearance, props, focused)
        } else {
            standaloneInputGeometry(appearance, focused)
        }
        val labelCoversPlaceholder = props.showLabel && props.value.isEmpty() && !focused
        if (appearance.floatingLabel && labelCoversPlaceholder) {
            property("--d-field-placeholder", "transparent")
        }
    }
}

private const val MATERIAL_FIELD_MIN_HEIGHT = 56f
private const val MATERIAL_FIELD_PADDING = 16f

// Optional theme values independently override renderer defaults.
@Suppress("CyclomaticComplexMethod")
internal fun <T : HTMLElement> AttrsScope<T>.floatingControlStyle(
    appearance: DomposableTextFieldAppearance,
    props: DomposableTextFieldProps,
    focused: Boolean,
) {
    val width =
        (if (focused) appearance.focusedOutlineWidth else appearance.outlineWidth)?.value
            ?: if (focused) 2f else 1f
    val borderColor =
        appearance.colors.outline
            .color(props.enabled, props.isError, focused)
            ?.css()
            ?: when {
                !props.enabled -> "var(--d-outline)"
                props.isError -> "var(--d-error)"
                focused -> "var(--d-primary)"
                else -> "var(--d-outline)"
            }
    style {
        property("position", "relative")
        property("display", "flex")
        property("align-items", "center")
        property("gap", if (props.prefix != null) "0" else "8px")
        property("box-sizing", "border-box")
        property("min-height", "${appearance.minHeight?.value ?: MATERIAL_FIELD_MIN_HEIGHT}px")
        property("border", "${width}px solid $borderColor")
        appearance.colors.container.color(props.enabled, props.isError, focused)?.let {
            property("background-color", it.css())
        } ?: property("background-color", "var(--d-surface)")
        if (appearance.variant == DomposableTextFieldVariant.Filled) {
            property("border-width", "0 0 ${width}px")
        }
        appearance.shape?.let { materialShape(it) } ?: run {
            property(
                "border-radius",
                if (appearance.variant ==
                    DomposableTextFieldVariant.Filled
                ) {
                    "4px 4px 0 0"
                } else {
                    "4px"
                },
            )
        }
    }
}

internal fun <T : HTMLElement> AttrsScope<T>.floatingSlotStyle(leading: Boolean) {
    style {
        property("position", "absolute")
        property("top", "2px")
        property("bottom", "2px")
        property("z-index", "1")
        property("display", "flex")
        property("align-items", "center")
        property("justify-content", "center")
        if (leading) {
            property("inset-inline-start", "2px")
            property("width", "48px")
        } else {
            property("inset-inline-end", "2px")
            property("width", "48px")
        }
    }
}

internal fun <T : HTMLElement> AttrsScope<T>.floatingPrefixStyle() {
    style {
        property("display", "flex")
        property("flex", "0 0 auto")
        property("align-items", "center")
        property("padding-inline-start", "16px")
    }
}

// Optional theme values independently override renderer defaults.
@Suppress("CyclomaticComplexMethod")
internal fun <T : HTMLElement> AttrsScope<T>.fieldLabelStyle(
    appearance: DomposableTextFieldAppearance,
    props: DomposableTextFieldProps,
    focused: Boolean,
    floating: Boolean,
) {
    val textAppearance =
        if (floating) {
            appearance.floatingLabelAppearance ?: appearance.labelAppearance
        } else {
            appearance.labelAppearance
        }
    val borderWidth =
        (if (focused) appearance.focusedOutlineWidth else appearance.outlineWidth)?.value
            ?: if (focused) 2f else 1f
    val labelStart =
        if (props.leadingIcon != null && !floating) {
            64f - borderWidth
        } else {
            (appearance.contentPadding?.start?.value ?: MATERIAL_FIELD_PADDING) - borderWidth
        }
    style {
        appearance.colors.label
            .color(props.enabled, props.isError, focused)
            ?.let { property("color", it.css()) }
        textAppearance?.let { applyTextAppearance(it) }
        if (appearance.floatingLabel) {
            property("pointer-events", "none")
            property(
                "max-width",
                if (props.leadingIcon != null &&
                    !floating
                ) {
                    "calc(100% - 80px)"
                } else {
                    "calc(100% - 32px)"
                },
            )
            property("overflow", "hidden")
            property("text-overflow", "ellipsis")
            property("white-space", "nowrap")
            property("inset-inline-start", "${labelStart}px")
            property("top", if (floating) "-${borderWidth}px" else "50%")
            property("transform", "translateY(-50%)")
            property("transition", "top 150ms ease, transform 150ms ease, font-size 150ms ease")
            property("position", "absolute")
            property("z-index", "1")
            if (floating) {
                property("padding-inline", "4px")
                property(
                    "background",
                    appearance.colors.container
                        .color(props.enabled, props.isError, focused)
                        ?.css()
                        ?: "var(--d-surface)",
                )
                if (textAppearance?.fontSize == null) property("font-size", "12px")
            }
        }
    }
}

@Composable
internal fun fieldIcon(
    appearance: DomposableTextFieldAppearance,
    props: DomposableTextFieldProps,
    focused: Boolean,
    leading: Boolean,
    content: @Composable () -> Unit,
) {
    val palette = if (leading) appearance.colors.leadingIcon else appearance.colors.trailingIcon
    val color = palette.color(props.enabled, props.isError, focused)
    if (color == null) {
        content()
    } else {
        Span(attrs = { style { property("color", color.css()) } }) { content() }
    }
}

internal fun DomposableStateColors.color(
    enabled: Boolean,
    isError: Boolean,
    focused: Boolean,
) = when {
    !enabled -> disabled
    isError -> error
    focused -> this.focused
    else -> unfocused
}

internal fun StyleScope.applyTextAppearance(appearance: DomposableTextAppearance) {
    appearance.fontSize?.let { property("font-size", "${it}px") }
    appearance.lineHeight?.let { property("line-height", "${it}px") }
    appearance.fontWeight?.let { property("font-weight", it.css()) }
    appearance.alignment?.let { property("text-align", it.name.lowercase()) }
    appearance.decoration?.let {
        property(
            "text-decoration",
            if (it.name ==
                "LineThrough"
            ) {
                "line-through"
            } else {
                it.name.lowercase()
            },
        )
    }
    appearance.letterSpacing?.let { property("letter-spacing", "${it}px") }
}

// Compensate border growth without moving the text or its icon slots.
@Suppress("CyclomaticComplexMethod")
private fun StyleScope.floatingInputGeometry(
    appearance: DomposableTextFieldAppearance,
    props: DomposableTextFieldProps,
    focused: Boolean,
) {
    val activeWidth = if (focused) appearance.focusedOutlineWidth else appearance.outlineWidth
    val borderWidth = activeWidth?.value ?: if (focused) 2f else 1f
    val borderDelta = borderWidth - (appearance.outlineWidth?.value ?: 1f)
    val verticalBorder =
        if (appearance.variant ==
            DomposableTextFieldVariant.Filled
        ) {
            borderWidth
        } else {
            borderWidth + borderWidth
        }
    val height = (appearance.minHeight?.value ?: MATERIAL_FIELD_MIN_HEIGHT) - verticalBorder
    val padding = appearance.contentPadding
    property(
        "padding-block",
        "${padding?.top?.value ?: MATERIAL_FIELD_PADDING}px ${padding?.bottom?.value ?: MATERIAL_FIELD_PADDING}px",
    )
    val startPadding =
        when {
            props.prefix != null -> 8f
            props.leadingIcon != null -> 64f - borderDelta
            else -> (padding?.start?.value ?: MATERIAL_FIELD_PADDING) - borderDelta
        }
    val endPadding =
        if (props.trailingIcon != null) {
            52f - borderDelta
        } else {
            (padding?.end?.value ?: MATERIAL_FIELD_PADDING) - borderDelta
        }
    property("padding-inline", "${startPadding}px ${endPadding}px")
    if (props.multiline || !props.singleLine) {
        property("min-height", "${height}px")
    } else {
        property("height", "${height}px")
    }
}

private fun StyleScope.standaloneInputGeometry(
    appearance: DomposableTextFieldAppearance,
    focused: Boolean,
) {
    val activeWidth = if (focused) appearance.focusedOutlineWidth else appearance.outlineWidth
    val borderWidth = activeWidth?.value ?: if (focused) 2f else 1f
    property("border-width", "${borderWidth}px")
    appearance.shape?.let { materialShape(it) } ?: property("border-radius", "4px")
    appearance.contentPadding?.let { padding ->
        property("padding-block", "${padding.top.value}px ${padding.bottom.value}px")
        property("padding-inline", "${padding.start.value}px ${padding.end.value}px")
    } ?: property("padding", "${MATERIAL_FIELD_PADDING}px")
    property("min-height", "${appearance.minHeight?.value ?: MATERIAL_FIELD_MIN_HEIGHT}px")
    if (appearance.variant == DomposableTextFieldVariant.Filled) {
        property("border-style", "solid")
        if (activeWidth == null) property("border-width", if (focused) "0 0 2px" else "0 0 1px")
        if (appearance.shape == null) property("border-radius", "4px 4px 0 0")
    }
}
