package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.ui.DomposableButtonProps
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableAttributes
import io.github.samuolis.domposable.ui.domposableFormEnabled
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Span
import org.w3c.dom.HTMLElement

@Composable
internal fun HtmlButton(
    props: DomposableButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
    respectFormEnabled: Boolean = true,
) {
    val tokens = LocalDomposableTokens.current
    val appearance = props.appearance ?: tokens.button
    val customAppearance = appearance != DomposableButtonAppearance()
    val formEnabled = !respectFormEnabled || domposableFormEnabled()
    val useEnabledColors = (props.enabled && formEnabled) || props.loading
    DomposableHtmlModifiers(modifier) {
        Button(attrs = {
            domposableAttributes(modifier)
            attr("type", "button")
            classes("d-button", "d-button-${props.style.name.lowercase()}")
            if (appearance.textAppearance?.maxLines == 1) classes("d-button-single-line")
            if (!props.enabled || !formEnabled || props.loading) attr("disabled", "")
            props.expanded?.let { attr("aria-expanded", it.toString()) }
            if (props.loading) {
                attr("aria-busy", "true")
                props.loadingLabel?.let { attr("aria-label", it) }
            }
            style {
                when (props.style) {
                    DomposableButtonStyle.Elevated -> {
                        if (appearance.container ==
                            null
                        ) {
                            property(
                                "background",
                                (tokens.colors.surfaceContainerLow ?: tokens.colors.surface).css(),
                            )
                        }
                        if (appearance.content == null) property("color", tokens.colors.primary.css())
                        property("box-shadow", "0 1px 3px rgb(0 0 0 / .24)")
                    }

                    DomposableButtonStyle.FilledTonal -> {
                        if (appearance.container ==
                            null
                        ) {
                            property(
                                "background",
                                (
                                    tokens.colors.secondaryContainer
                                        ?: tokens.colors.surfaceVariant
                                ).css(),
                            )
                        }
                        if (appearance.content ==
                            null
                        ) {
                            property("color", (tokens.colors.onSecondaryContainer ?: tokens.colors.onSurface).css())
                        }
                    }

                    else -> {
                        Unit
                    }
                }
                (if (useEnabledColors) appearance.container else appearance.disabledContainer)?.let {
                    property(
                        "background",
                        it.css(),
                    )
                }
                (if (useEnabledColors) appearance.content else appearance.disabledContent)?.let {
                    property(
                        "color",
                        it.css(),
                    )
                }
                (if (useEnabledColors) appearance.border else appearance.disabledBorder)?.let {
                    property("border-color", it.css())
                }
                if (!useEnabledColors) {
                    val surface = tokens.colors.onSurface.argb and 0xFFFFFFL
                    if (appearance.disabledContainer == null) {
                        val container =
                            if (props.style == DomposableButtonStyle.Filled ||
                                props.style == DomposableButtonStyle.Elevated ||
                                props.style == DomposableButtonStyle.FilledTonal
                            ) {
                                DomposableColor(surface or DISABLED_CONTAINER_ALPHA).css()
                            } else {
                                "transparent"
                            }
                        property("background", container)
                    }
                    if (appearance.disabledContent ==
                        null
                    ) {
                        property("color", DomposableColor(surface or DISABLED_CONTENT_ALPHA).css())
                    }
                }
                if (!useEnabledColors && props.style == DomposableButtonStyle.Elevated) property("box-shadow", "none")
                if (!useEnabledColors && props.style == DomposableButtonStyle.Outlined &&
                    appearance.disabledBorder == null
                ) {
                    property(
                        "border-color",
                        DomposableColor((tokens.colors.onSurface.argb and 0xFFFFFFL) or DISABLED_CONTAINER_ALPHA).css(),
                    )
                }
                appearance.borderWidth?.let { property("border-width", "${it.value}px") }
                appearance.shape?.let { materialShape(it) }
                appearance.minHeight?.let { property("min-height", "${it.value}px") }
                appearance.minWidth?.let { property("min-width", "${it.value}px") }
                appearance.contentPadding?.let {
                    property("padding-block", "${it.top.value}px ${it.bottom.value}px")
                    property("padding-inline", "${it.start.value}px ${it.end.value}px")
                }
                appearance.textAppearance?.let {
                    it.fontSize?.let { property("font-size", "${it}px") }
                    it.lineHeight?.let { property("line-height", "${it}px") }
                    it.letterSpacing?.let { property("letter-spacing", "${it}px") }
                    it.fontWeight?.let { property("font-weight", it.css()) }
                }
                appearance.indicatorSize?.let { property("--d-button-indicator-size", "${it.value}px") }
                appearance.indicatorStrokeWidth?.let { property("--d-button-indicator-stroke", "${it.value}px") }
                property("--d-button-pressed-scale", appearance.pressedScale.toString())
                property("--d-button-hovered-scale", appearance.hoveredScale.toString())
                if (customAppearance || props.loading || !useEnabledColors) {
                    property("opacity", "1")
                }
            }
            onClick {
                ((it.target as? HTMLElement)?.closest("button") as? HTMLElement)?.focus()
                if (props.enabled && formEnabled && !props.loading) props.onClick()
            }
        }) {
            // Keep the measured content in place so loading never changes the button's width.
            Span(attrs = {
                classes("d-button-copy")
                style { property("gap", "${appearance.contentSpacing.value}px") }
                if (props.loading) {
                    style { property("opacity", "0") }
                    if (props.loadingLabel != null) attr("aria-hidden", "true")
                }
            }) {
                CompositionLocalProvider(
                    LocalMaterialButtonTextAppearance provides appearance.textAppearance,
                ) { content() }
            }
            if (props.loading) {
                Span(attrs = {
                    classes("d-button-spinner")
                    attr("aria-hidden", "true")
                }) {}
            }
        }
    }
}

internal fun StyleScope.materialShape(shape: DomposableShape) {
    when (shape) {
        DomposableShape.Rectangle -> {
            property("border-radius", "0")
        }

        DomposableShape.Circle -> {
            property("border-radius", "9999px")
        }

        is DomposableShape.Rounded -> {
            property("border-radius", "${shape.radius.value}px")
        }

        is DomposableShape.Corners -> {
            property("border-start-start-radius", "${shape.topStart.value}px")
            property("border-start-end-radius", "${shape.topEnd.value}px")
            property("border-end-end-radius", "${shape.bottomEnd.value}px")
            property("border-end-start-radius", "${shape.bottomStart.value}px")
        }
    }
}

private const val DISABLED_CONTAINER_ALPHA = 0x1F000000L
private const val DISABLED_CONTENT_ALPHA = 0x61000000L
