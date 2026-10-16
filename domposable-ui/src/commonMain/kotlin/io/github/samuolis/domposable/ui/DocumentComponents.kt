package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableComponent
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextStyle

/** Controlled semantic form shared by native Compose and HTML renderers. */
@DomposableComponent("Form")
data class DomposableFormProps(
    val onSubmit: () -> Unit,
    val enabled: Boolean = true,
    val label: String? = null,
    val browserValidationEnabled: Boolean = true,
)

/** A controlled disclosure whose expanded content remains owned by the caller. */
@DomposableComponent("Disclosure")
data class DomposableDisclosureProps(
    val title: String,
    val description: String? = null,
    val expanded: Boolean,
    val onToggle: () -> Unit,
    val appearance: DomposableSurfaceAppearance? = null,
)

/** Safe, renderer-neutral rich text parsed as allowlisted markup on every platform. */
@DomposableComponent("RichHtml")
data class DomposableRichHtmlProps(
    val rawHtml: String,
    val selectable: Boolean = false,
    val onLinkClick: ((String) -> Unit)? = null,
    val style: DomposableTextStyle = DomposableTextStyle.Body,
    val appearance: DomposableTextAppearance? = null,
    val color: DomposableColor? = null,
)

private data class DomposableFormContext(
    val onSubmit: () -> Unit,
    val enabled: Boolean,
)

private val LocalDomposableFormContext = staticCompositionLocalOf<DomposableFormContext?> { null }

/** Returns whether controls in the nearest [DomposableForm] should be interactive. */
@Composable
fun domposableFormEnabled(): Boolean = LocalDomposableFormContext.current?.enabled ?: true

@Composable
fun DomposableForm(
    onSubmit: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    label: String? = null,
    browserValidationEnabled: Boolean = true,
    content: @Composable () -> Unit,
) = DomposableForm(
    DomposableFormProps(onSubmit, enabled, label, browserValidationEnabled),
    modifier,
    content,
)

/**
 * Returns an up-to-date submit action for a text field's Done/Go IME callback or a submit
 * control. It does not submit automatically, so a control cannot accidentally submit twice.
 */
@Composable
fun rememberDomposableFormSubmit(): () -> Unit =
    rememberDomposableFormSubmitOrNull()
        ?: error("rememberDomposableFormSubmit() must be called inside DomposableForm")

/**
 * Returns the current enclosing form action for standard input renderers' IME callbacks, or null
 * when the input is outside a [DomposableForm].
 */
@Composable
fun rememberDomposableFormSubmitOrNull(): (() -> Unit)? {
    val context = LocalDomposableFormContext.current ?: return null
    val onSubmit by rememberUpdatedState(context.onSubmit)
    val enabled by rememberUpdatedState(context.enabled)
    return remember { { if (enabled) onSubmit() } }
}

/** Provides a form submit action to custom input renderers and their descendants. */
@Composable
fun ProvideDomposableFormSubmit(
    props: DomposableFormProps,
    content: @Composable () -> Unit,
) {
    val enabled = props.enabled && domposableFormEnabled()
    CompositionLocalProvider(
        LocalDomposableFormContext provides DomposableFormContext(props.onSubmit, enabled),
        content = content,
    )
}

@Composable
fun DomposableDisclosure(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    description: String? = null,
    appearance: DomposableSurfaceAppearance? = null,
    content: @Composable () -> Unit,
) = DomposableDisclosure(
    DomposableDisclosureProps(title, description, expanded, onToggle, appearance = appearance),
    modifier,
    content,
)

@Composable
fun DomposableRichHtml(
    rawHtml: String,
    modifier: DomposableModifier = DomposableModifier,
    selectable: Boolean = false,
    onLinkClick: ((String) -> Unit)? = null,
    style: DomposableTextStyle = DomposableTextStyle.Body,
    appearance: DomposableTextAppearance? = null,
    color: DomposableColor? = null,
) = DomposableRichHtml(
    DomposableRichHtmlProps(rawHtml, selectable, onLinkClick, style, appearance, color),
    modifier,
)
