package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableInputPurpose
import io.github.samuolis.domposable.foundation.DomposableStateColors
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextFieldAppearance
import io.github.samuolis.domposable.foundation.DomposableTextFieldVariant
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTextFieldProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableAttributes
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import org.w3c.dom.HTMLElement

@Composable
internal fun HtmlTextField(
    props: DomposableTextFieldProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.textField
    val id = props.id ?: remember { nextMaterialInputId() }
    val supportingText = props.error ?: props.helperText
    var focused by remember { mutableStateOf(false) }
    val floating = appearance.floatingLabel
    val labelFloated = floating && (focused || props.value.isNotEmpty())
    val autoSizeBinding = remember { HtmlTextAreaAutoSizeBinding() }
    autoSizeBinding.minLines = props.minLines
    autoSizeBinding.maxLines = props.maxLines
    LaunchedEffect(props.value, props.minLines, props.maxLines, appearance.autoSize) {
        if (appearance.autoSize) autoSizeBinding.resize()
    }
    DomposableHtmlModifiers(modifier) {
        Label(attrs = {
            classes("d-field")
            attr("for", id)
        }) {
            if (props.showLabel && !floating) {
                Span(attrs = { fieldLabelStyle(appearance, props, focused, floating = false) }) { Text(props.label) }
            }
            val needsControl =
                floating || props.leadingIcon != null || props.prefix != null || props.trailingIcon != null
            if (needsControl) {
                Div(attrs = {
                    classes("d-field-control")
                    if (floating) style { property("position", "relative") }
                    if (floating) floatingControlStyle(appearance)
                }) {
                    if (floating && props.showLabel) {
                        Span(attrs = {
                            fieldLabelStyle(appearance, props, focused, floating = labelFloated)
                        }) { Text(props.label) }
                    }
                    props.leadingIcon?.let { icon -> fieldIcon(appearance, props, focused, leading = true, icon) }
                    props.prefix?.invoke()
                    fieldInput(props, modifier, id, appearance, focused, autoSizeBinding) { focused = it }
                    props.trailingIcon?.let { icon -> fieldIcon(appearance, props, focused, leading = false, icon) }
                }
            } else {
                fieldInput(props, modifier, id, appearance, focused, autoSizeBinding) { focused = it }
            }
            supportingText?.let { message ->
                Span(attrs = {
                    id("$id-supporting")
                    if (props.error != null) {
                        classes("d-error")
                        attr("aria-live", "polite")
                    } else {
                        classes("d-helper")
                    }
                    appearance.colors.supporting.color(props.enabled, props.isError, focused)?.let {
                        style { property("color", it.css()) }
                    }
                    appearance.supportingAppearance?.let { style { applyTextAppearance(it) } }
                }) { Text(message) }
            }
        }
    }
}

@Composable
private fun fieldInput(
    props: DomposableTextFieldProps,
    modifier: DomposableModifier,
    id: String,
    appearance: DomposableTextFieldAppearance,
    focused: Boolean,
    autoSizeBinding: HtmlTextAreaAutoSizeBinding,
    onFocusChange: (Boolean) -> Unit,
) {
    fun <T : HTMLElement> AttrsScope<T>.applyInputAttributes() {
        id(id)
        domposableAttributes(modifier)
        inputSemantics(props, id, appearance)
        inputEvents(props, onFocusChange)
        inputAppearance(appearance, props, focused)
        if (appearance.autoSize && (props.multiline || !props.singleLine)) {
            ref { element ->
                val cleanup = installHtmlTextAreaAutoSize(element, autoSizeBinding)
                onDispose { cleanup() }
            }
        }
    }
    if (props.multiline || !props.singleLine) {
        TextArea(value = props.value, attrs = {
            applyInputAttributes()
            val initialRows = maxOf(2, props.minLines).coerceAtMost(props.maxLines)
            attr("rows", initialRows.toString())
            if (appearance.minHeight == null) style { property("min-height", "calc(${props.minLines}lh + 26px)") }
            if (props.maxLines != Int.MAX_VALUE) style { property("max-height", "calc(${props.maxLines}lh + 26px)") }
            onInput {
                props.onValueChange(it.target.value)
                autoSizeBinding.resize()
            }
        })
    } else {
        Input(type = props.purpose.inputType(props.passwordVisible), attrs = {
            applyInputAttributes()
            value(props.value)
            onInput { props.onValueChange(it.target.value) }
        })
    }
}

private fun <T : HTMLElement> AttrsScope<T>.inputSemantics(
    props: DomposableTextFieldProps,
    id: String,
    appearance: DomposableTextFieldAppearance,
) {
    inputHints(props)
    if (props.imeAction != io.github.samuolis.domposable.foundation.DomposableImeAction.Default) {
        attr("enterkeyhint", props.imeAction.name.lowercase())
    }
    if (!props.showLabel) attr("aria-label", props.label)
    if (props.isError) attr("aria-invalid", "true")
    if (props.error != null || props.helperText != null) attr("aria-describedby", "$id-supporting")
    props.autocomplete?.let { attr("autocomplete", it.htmlValue()) }
    if (appearance.floatingLabel) {
        attr("placeholder", props.placeholder.orEmpty())
    } else {
        props.placeholder?.let { attr("placeholder", it) }
    }
    if (props.readOnly) attr("readonly", "")
    if (props.autofocus) attr("autofocus", "")
    if (!props.enabled) attr("disabled", "")
    props.onClick?.let { callback -> onClick { callback() } }
}

private fun <T : HTMLElement> AttrsScope<T>.inputHints(props: DomposableTextFieldProps) {
    when (props.purpose) {
        DomposableInputPurpose.Phone -> {
            attr("inputmode", "tel")
        }

        DomposableInputPurpose.Decimal -> {
            attr("inputmode", "decimal")
            attr("step", "any")
        }

        DomposableInputPurpose.OneTimeCode -> {
            attr("inputmode", "numeric")
            if (props.autocomplete == null) attr("autocomplete", "one-time-code")
        }

        else -> {
            Unit
        }
    }
}

private fun <T : HTMLElement> AttrsScope<T>.inputEvents(
    props: DomposableTextFieldProps,
    onFocusChange: (Boolean) -> Unit,
) {
    props.onImeAction?.let { submit ->
        val imeEnabled = props.enabled && !props.readOnly
        onKeyDown { event ->
            if (event.key == "Enter" && props.singleLine && imeEnabled) {
                event.preventDefault()
                submit()
            }
        }
    }
    onFocus {
        onFocusChange(true)
        props.onFocusChange?.invoke(true)
    }
    onBlur {
        onFocusChange(false)
        props.onFocusChange?.invoke(false)
    }
}

// Each optional appearance field independently overrides its CSS property.
@Suppress("CyclomaticComplexMethod")
private fun <T : HTMLElement> AttrsScope<T>.inputAppearance(
    appearance: DomposableTextFieldAppearance,
    props: DomposableTextFieldProps,
    focused: Boolean,
) {
    val colors = appearance.colors
    val stateText = colors.text.color(props.enabled, props.isError, focused)
    val stateContainer = colors.container.color(props.enabled, props.isError, focused)
    val stateOutline = colors.outline.color(props.enabled, props.isError, focused)
    style {
        stateText?.let { property("color", it.css()) }
        stateContainer?.let { property("background-color", it.css()) }
        stateOutline?.let { property("border-color", it.css()) }
        colors.cursor.color(props.enabled, props.isError, focused)?.let { property("caret-color", it.css()) }
        colors.placeholder
            .color(
                props.enabled,
                props.isError,
                focused,
            )?.let { property("--d-field-placeholder", it.css()) }
        val activeWidth = if (focused) appearance.focusedOutlineWidth else appearance.outlineWidth
        (
            activeWidth?.value ?: if (appearance.floatingLabel) {
                if (focused) {
                    2f
                } else {
                    1f
                }
            } else {
                null
            }
        )?.let {
            property("border-width", "${it}px")
        }
        appearance.shape?.let { materialShape(it) }
        appearance.textAppearance?.let { applyTextAppearance(it) }
        appearance.contentPadding?.let { padding ->
            property("padding-block", "${padding.top.value}px ${padding.bottom.value}px")
            property("padding-inline", "${padding.start.value}px ${padding.end.value}px")
        }
        if (appearance.contentPadding == null && appearance.floatingLabel) {
            property("padding-block", "16px")
            property("padding-inline", "16px")
        }
        appearance.minHeight?.let { property("min-height", "${it.value}px") }
        if (appearance.minHeight == null && appearance.floatingLabel) property("min-height", "56px")
        if (appearance.variant == DomposableTextFieldVariant.Filled) {
            property("border-style", "solid")
            if (activeWidth == null) property("border-width", "0 0 1px")
            if (appearance.shape == null) property("border-radius", "4px 4px 0 0")
        } else if (appearance.shape == null && appearance.floatingLabel) {
            property("border-radius", "4px")
        }
        if (appearance.floatingLabel && props.value.isEmpty() && !focused) {
            property("--d-field-placeholder", "transparent")
        }
    }
}

private const val MATERIAL_FIELD_MIN_HEIGHT = 56f

private fun <T : HTMLElement> AttrsScope<T>.floatingControlStyle(appearance: DomposableTextFieldAppearance) {
    style {
        property("min-height", "${appearance.minHeight?.value ?: MATERIAL_FIELD_MIN_HEIGHT}px")
        if (appearance.shape == null) property("border-radius", "4px")
    }
}

private fun <T : HTMLElement> AttrsScope<T>.fieldLabelStyle(
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
    style {
        appearance.colors.label
            .color(props.enabled, props.isError, focused)
            ?.let { property("color", it.css()) }
        textAppearance?.let { applyTextAppearance(it) }
        if (appearance.floatingLabel) {
            property("pointer-events", "none")
            property(
                "inset-inline-start",
                if (floating) {
                    "12px"
                } else if (props.leadingIcon != null) {
                    "64px"
                } else {
                    "16px"
                },
            )
            property("top", if (floating) "0" else "50%")
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
private fun fieldIcon(
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

private fun DomposableStateColors.color(
    enabled: Boolean,
    isError: Boolean,
    focused: Boolean,
) = when {
    !enabled -> disabled
    isError -> error
    focused -> this.focused
    else -> unfocused
}

private fun StyleScope.applyTextAppearance(appearance: DomposableTextAppearance) {
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

private fun DomposableInputPurpose.inputType(passwordVisible: Boolean) =
    when (this) {
        DomposableInputPurpose.Email -> InputType.Email
        DomposableInputPurpose.Password -> if (passwordVisible) InputType.Text else InputType.Password
        DomposableInputPurpose.Number, DomposableInputPurpose.Decimal -> InputType.Number
        DomposableInputPurpose.Phone -> InputType.Tel
        DomposableInputPurpose.Search -> InputType.Search
        DomposableInputPurpose.Date -> InputType.Date
        DomposableInputPurpose.Time -> InputType.Time
        else -> InputType.Text
    }
