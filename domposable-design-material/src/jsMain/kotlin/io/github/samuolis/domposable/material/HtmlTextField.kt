package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableInputPurpose
import io.github.samuolis.domposable.foundation.DomposableTextFieldAppearance
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTextFieldProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableAttributes
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.InputType
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
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.textField
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
        if (floating) {
            Div(attrs = { fieldRootStyle(props, floating = true) }) {
                fieldControl(props, modifier, id, appearance, focused, autoSizeBinding, labelFloated) { focused = it }
                fieldSupportingText(props, id, appearance, focused, supportingText)
            }
        } else {
            Label(attrs = {
                fieldRootStyle(props, floating = false)
                attr("for", id)
            }) {
                if (props.showLabel) {
                    Span(
                        attrs = { fieldLabelStyle(appearance, props, focused, floating = false) },
                    ) { Text(props.label) }
                }
                fieldControl(props, modifier, id, appearance, focused, autoSizeBinding, labelFloated) { focused = it }
                fieldSupportingText(props, id, appearance, focused, supportingText)
            }
        }
    }
}

private fun <T : HTMLElement> AttrsScope<T>.fieldRootStyle(
    props: DomposableTextFieldProps,
    floating: Boolean,
) {
    classes("d-field")
    if (floating && props.showLabel) {
        style {
            property("padding-block-start", "8px")
            property("row-gap", "4px")
        }
    }
}

@Composable
private fun fieldControl(
    props: DomposableTextFieldProps,
    modifier: DomposableModifier,
    id: String,
    appearance: DomposableTextFieldAppearance,
    focused: Boolean,
    autoSizeBinding: HtmlTextAreaAutoSizeBinding,
    labelFloated: Boolean,
    onFocusChange: (Boolean) -> Unit,
) {
    val floating = appearance.floatingLabel
    val needsControl = floating || props.leadingIcon != null || props.prefix != null || props.trailingIcon != null
    if (!needsControl) {
        fieldInput(
            props,
            modifier,
            id,
            appearance,
            focused,
            autoSizeBinding,
            floating = false,
            onFocusChange = onFocusChange,
        )
        return
    }
    Div(attrs = {
        classes("d-field-control")
        if (floating) floatingControlStyle(appearance, props, focused)
    }) {
        if (floating && props.showLabel) {
            Label(attrs = {
                attr("for", id)
                fieldLabelStyle(appearance, props, focused, floating = labelFloated)
            }) { Text(props.label) }
        }
        if (floating) {
            props.leadingIcon?.let { icon ->
                Span(attrs = {
                    classes("d-field-leading")
                    floatingSlotStyle(leading = true)
                }) { fieldIcon(appearance, props, focused, leading = true, icon) }
            }
            props.prefix?.let { prefix ->
                Span(attrs = {
                    classes("d-field-prefix")
                    floatingPrefixStyle()
                }) { prefix() }
            }
            fieldInput(
                props,
                modifier,
                id,
                appearance,
                focused,
                autoSizeBinding,
                floating = true,
                onFocusChange = onFocusChange,
            )
            props.trailingIcon?.let { icon ->
                Span(attrs = {
                    classes("d-field-trailing")
                    floatingSlotStyle(leading = false)
                }) { fieldIcon(appearance, props, focused, leading = false, icon) }
            }
        } else {
            props.leadingIcon?.let { icon -> fieldIcon(appearance, props, focused, leading = true, icon) }
            props.prefix?.invoke()
            fieldInput(
                props,
                modifier,
                id,
                appearance,
                focused,
                autoSizeBinding,
                floating = false,
                onFocusChange = onFocusChange,
            )
            props.trailingIcon?.let { icon -> fieldIcon(appearance, props, focused, leading = false, icon) }
        }
    }
}

@Composable
private fun fieldSupportingText(
    props: DomposableTextFieldProps,
    id: String,
    appearance: DomposableTextFieldAppearance,
    focused: Boolean,
    supportingText: String?,
) {
    supportingText?.let { message ->
        Span(attrs = {
            id("$id-supporting")
            style {
                property("min-width", "0")
                property("overflow-wrap", "anywhere")
            }
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

@Composable
private fun fieldInput(
    props: DomposableTextFieldProps,
    modifier: DomposableModifier,
    id: String,
    appearance: DomposableTextFieldAppearance,
    focused: Boolean,
    autoSizeBinding: HtmlTextAreaAutoSizeBinding,
    floating: Boolean,
    onFocusChange: (Boolean) -> Unit,
) {
    fun <T : HTMLElement> AttrsScope<T>.applyInputAttributes() {
        id(id)
        domposableAttributes(modifier)
        inputSemantics(props, id, appearance)
        inputEvents(props, onFocusChange)
        inputAppearance(appearance, props, focused, floating)
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
            if (!appearance.autoSize && props.maxLines != Int.MAX_VALUE) {
                style { property("max-height", "calc(${props.maxLines}lh + 26px)") }
            }
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
