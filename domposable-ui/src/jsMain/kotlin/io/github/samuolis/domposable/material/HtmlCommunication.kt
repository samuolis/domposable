package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.ui.DomposableBadgedBoxProps
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableScrimProps
import io.github.samuolis.domposable.ui.DomposableSnackbarDuration
import io.github.samuolis.domposable.ui.DomposableSnackbarHostProps
import io.github.samuolis.domposable.ui.DomposableSnackbarProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableAttributes
import io.github.samuolis.domposable.ui.domposableFormEnabled
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

@Composable
internal fun HtmlBadgedBox(
    props: DomposableBadgedBoxProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    DomposableHtmlModifiers(modifier) {
        TagElement<HTMLElement>("div", applyAttrs = {
            domposableAttributes(modifier)
            classes("d-badged-box")
            style { property("position", "relative") }
        }) {
            Div(attrs = { classes("d-badged-box-content") }) { content() }
            Div(attrs = {
                classes("d-badged-box-badge")
                style {
                    property("position", "absolute")
                    property("inset-block-start", "0")
                    property("inset-inline-end", "0")
                    property("z-index", "1")
                }
            }) { props.badge() }
        }
    }
}

@Composable
internal fun HtmlScrim(
    props: DomposableScrimProps,
    modifier: DomposableModifier,
) {
    val enabled = domposableFormEnabled()
    val clickAction = props.onClick
    DomposableHtmlModifiers(modifier) {
        if (clickAction != null) {
            Button(attrs = {
                domposableAttributes(modifier)
                attr("type", "button")
                attr("aria-label", props.label!!)
                classes("d-scrim")
                style {
                    property("position", "absolute")
                    property("inset", "0")
                    property("z-index", "30")
                    property("border", "0")
                    property("padding", "0")
                    property("background-color", props.color?.css() ?: "var(--d-scrim)")
                    property("opacity", props.alpha.toString())
                }
                if (!enabled) attr("disabled", "")
                onClick { if (enabled) clickAction() }
            }) {}
        } else {
            Div(attrs = {
                domposableAttributes(modifier)
                classes("d-scrim")
                attr("aria-hidden", "true")
                style {
                    property("position", "absolute")
                    property("inset", "0")
                    property("z-index", "30")
                    property("background-color", props.color?.css() ?: "var(--d-scrim)")
                    property("opacity", props.alpha.toString())
                    property("pointer-events", "none")
                }
            }) {}
        }
    }
}

@Composable
internal fun HtmlSnackbarHost(
    props: DomposableSnackbarHostProps,
    modifier: DomposableModifier,
) {
    val data = props.state.currentSnackbarData
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.snackbar
    val snackbarContent = props.snackbarContent
    val timeout =
        when (data?.duration) {
            DomposableSnackbarDuration.Short -> 4_000L
            DomposableSnackbarDuration.Long -> 10_000L
            DomposableSnackbarDuration.Indefinite, null -> null
        }
    var hovered by remember(data) { mutableStateOf(false) }
    var focused by remember(data) { mutableStateOf(false) }
    var remaining by remember(data) { mutableStateOf(timeout) }

    LaunchedEffect(data, hovered, focused) {
        val current = data
        val waitMillis = remaining
        if (current == null || waitMillis == null) return@LaunchedEffect
        if (hovered || focused) return@LaunchedEffect
        val startedAt = kotlin.js.Date.now()
        try {
            delay(waitMillis)
            current.dismiss()
        } finally {
            val elapsed = (kotlin.js.Date.now() - startedAt).toLong().coerceAtLeast(0L)
            remaining = (waitMillis - elapsed).coerceAtLeast(0L)
        }
    }

    DomposableHtmlModifiers(modifier) {
        TagElement<HTMLElement>("div", applyAttrs = {
            domposableAttributes(modifier)
            classes("d-snackbar-host")
            style {
                property("display", "flex")
                property("justify-content", "center")
                property("pointer-events", "none")
            }
            ref { element ->
                val onFocusIn: (org.w3c.dom.events.Event) -> Unit = { focused = true }
                val onFocusOut: (org.w3c.dom.events.Event) -> Unit = { event ->
                    val nextTarget = event.asDynamic().relatedTarget as? org.w3c.dom.Node
                    if (nextTarget == null || !element.contains(nextTarget)) focused = false
                }
                element.addEventListener("focusin", onFocusIn)
                element.addEventListener("focusout", onFocusOut)
                onDispose {
                    element.removeEventListener("focusin", onFocusIn)
                    element.removeEventListener("focusout", onFocusOut)
                }
            }
            onMouseEnter { hovered = true }
            onMouseLeave { hovered = false }
        }) {
            if (data != null) {
                if (snackbarContent != null) {
                    snackbarContent(data)
                } else {
                    HtmlSnackbar(
                        DomposableSnackbarProps(
                            message = data.message,
                            actionLabel = data.actionLabel,
                            onAction = data.actionLabel?.let { { data.performAction() } },
                            onDismiss = if (data.withDismissAction) ({ data.dismiss() }) else null,
                            appearance = appearance,
                        ),
                        DomposableModifier,
                    )
                }
            }
        }
    }
}
