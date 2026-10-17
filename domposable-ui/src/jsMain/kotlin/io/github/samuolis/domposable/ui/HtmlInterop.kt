package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableLayoutAxis
import io.github.samuolis.domposable.foundation.DomposableLinearGradient
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableSize
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

/** Application-owned HTML adapter for an open [DomposableModifierElement]. */
typealias DomposableHtmlModifierElementRenderer =
    (DomposableModifierElement) -> (AttrsScope<HTMLElement>.() -> Unit)?

/** Product providers can install brand element handling once for every descendant control. */
val LocalDomposableHtmlModifierRenderer =
    staticCompositionLocalOf<DomposableHtmlModifierElementRenderer?> { null }

/** App-owned HTML/SVG decoration around the remaining ordered modifier chain. Render content once. */
typealias DomposableHtmlModifierDecoration = @Composable (content: @Composable () -> Unit) -> Unit

/** Return null for unhandled elements, allowing the attribute renderer to handle them. */
typealias DomposableHtmlModifierDecorationRenderer = (DomposableModifierElement) -> DomposableHtmlModifierDecoration?

val LocalDomposableHtmlModifierDecorationRenderer =
    staticCompositionLocalOf<DomposableHtmlModifierDecorationRenderer?> { null }

private data class DomposableScrollBinding(
    val state: DomposableScrollState,
    val orientation: DomposableOrientation,
)

private val domposableScrollBindings = js("new WeakMap()")

private val updateDomposableScrollBinding: (HTMLElement, DomposableScrollBinding) -> Unit = { element, binding ->
    val previous = domposableScrollBindings.get(element) as? DomposableScrollBinding
    val unchanged = previous != null && previous.state === binding.state && previous.orientation == binding.orientation
    if (!unchanged) {
        previous?.state?.detach(element)
        domposableScrollBindings.set(element, binding)
        binding.state.attach(element, binding.orientation)
    }
}

private fun domposableScrollBinding(element: HTMLElement): DomposableScrollBinding? =
    domposableScrollBindings.get(element) as? DomposableScrollBinding

private data class HtmlAttributes(
    val attributes: AttrsScope<*>.() -> Unit,
) : DomposableModifierElement

internal val POINTER_PASSTHROUGH_CSS =
    """
    [data-domposable-pointer-passthrough]{pointer-events:none}
    [data-domposable-pointer-passthrough] :is(a,button,input,select,textarea,[role=button],[role=link],[tabindex]:not([tabindex='-1'])){pointer-events:auto}
    [data-domposable-pointer-passthrough]:is(a,button,input,select,textarea,[role=button],[role=link],[tabindex]:not([tabindex='-1'])){pointer-events:auto}
    """.trimIndent()

/** Applies app-owned attributes and styles directly to the rendered HTML element. */
fun DomposableModifier.htmlAttributes(attributes: AttrsScope<*>.() -> Unit): DomposableModifier =
    then(HtmlAttributes(attributes))

/** Semantic attributes attach to the control, not to its layout wrappers. */
fun <T : HTMLElement> AttrsScope<T>.domposableAttributes(modifier: DomposableModifier) {
    modifier.elements.forEach { element ->
        when (element) {
            is DomposableModifierOperation.Description -> {
                attr("aria-label", element.text)
            }

            is DomposableModifierOperation.TestId -> {
                attr("data-testid", element.value)
            }

            is HtmlAttributes -> {
                element.attributes(this)
            }

            else -> {
                Unit
            }
        }
    }
}

/** Binds a DOM scrollable element to a shared scroll state, including animated commands. */
fun AttrsScope<HTMLElement>.domposableScrollState(
    state: DomposableScrollState,
    orientation: DomposableOrientation,
) {
    attr("tabindex", "0")
    prop(updateDomposableScrollBinding, DomposableScrollBinding(state, orientation))
    ref { element ->
        domposableScrollBinding(element)?.let { it.state.attach(element, it.orientation) }
        onDispose {
            domposableScrollBinding(element)?.state?.detach(element)
            domposableScrollBindings.delete(element)
        }
    }
    onKeyDown { event ->
        val nativeEvent = event.nativeEvent
        val hasShortcutModifier = event.ctrlKey || event.altKey || event.metaKey
        if (nativeEvent.target === nativeEvent.currentTarget && !hasShortcutModifier) {
            val element = nativeEvent.currentTarget as HTMLElement
            val handled =
                domposableScrollBinding(element)?.state?.scrollFromKey(element, event.key, event.shiftKey) == true
            if (handled) event.preventDefault()
        }
    }
    onScroll { event ->
        val element = event.nativeEvent.currentTarget as HTMLElement
        domposableScrollBinding(element)?.let { it.state.updateFromElement(element, it.orientation) }
    }
}

/**
 * Applies portable operations in order. Child-layout operations get an outer item node so they remain
 * effective when preceding operations add padding or styling wrappers. Application elements must
 * have an explicit renderer; unsupported elements fail closed.
 */
@Composable
fun DomposableHtmlModifiers(
    modifier: DomposableModifier,
    operationRenderer: DomposableHtmlModifierElementRenderer? = null,
    content: @Composable () -> Unit,
) = DomposableHtmlModifiers(modifier, operationRenderer, decorationRenderer = null, content)

/** A matching decoration owns the element; returning null falls back to attribute handling. */
@Composable
fun DomposableHtmlModifiers(
    modifier: DomposableModifier,
    operationRenderer: DomposableHtmlModifierElementRenderer? = null,
    decorationRenderer: DomposableHtmlModifierDecorationRenderer?,
    content: @Composable () -> Unit,
) {
    val renderer = operationRenderer ?: LocalDomposableHtmlModifierRenderer.current
    val decoration = decorationRenderer ?: LocalDomposableHtmlModifierDecorationRenderer.current
    val layout = modifier.elements.filter(::isChildLayoutOperation)
    val visual =
        modifier.elements.filterNot(::isChildLayoutOperation).filterNot {
            it is DomposableModifierOperation.Description ||
                it is DomposableModifierOperation.TestId ||
                it is HtmlAttributes
        }
    val pointerPassthrough = visual.contains(DomposableModifierOperation.PointerPassthrough)
    val stackingOrder = layout.sumOf { if (it is DomposableModifierOperation.ZIndex) it.value.toDouble() else 0.0 }
    if (layout.isEmpty()) {
        HtmlModifierBox(visual, 0, renderer, decoration, false, pointerPassthrough, content)
    } else {
        Div(attrs = {
            attr("data-domposable-layout-item", "true")
            if (pointerPassthrough) attr("data-domposable-pointer-passthrough", "")
            style {
                childLayoutSizing(visual)
                property("box-sizing", "border-box")
                layout.forEach { element ->
                    when (element) {
                        is DomposableModifierOperation.Weight -> {
                            property("min-width", "0")
                            property("min-height", "0")
                            property("flex-grow", element.weight.toString())
                            property("flex-shrink", if (element.fill) "1" else "0")
                            // Percent basis keeps intrinsic content when the document axis is unbounded.
                            property("flex-basis", if (element.fill) "0%" else "auto")
                        }

                        is DomposableModifierOperation.AxisAlignment -> {
                            property("align-self", element.alignment.css())
                        }

                        is DomposableModifierOperation.BoxAlignment -> {
                            property("place-self", "${element.vertical.css()} ${element.horizontal.css()}")
                        }

                        is DomposableModifierOperation.ZIndex -> {
                            property("position", "relative")
                            property("z-index", stackingOrder.toString())
                        }

                        else -> {
                            error("Unexpected child layout modifier element: $element")
                        }
                    }
                }
            }
        }) { HtmlModifierBox(visual, 0, renderer, decoration, true, pointerPassthrough, content) }
    }
}

@Composable
private fun HtmlModifierBox(
    elements: List<DomposableModifierElement>,
    index: Int,
    operationRenderer: DomposableHtmlModifierElementRenderer?,
    decorationRenderer: DomposableHtmlModifierDecorationRenderer?,
    fillAvailableHeight: Boolean,
    pointerPassthrough: Boolean,
    content: @Composable () -> Unit,
) {
    if (index == elements.size) {
        content()
        return
    }
    val element = elements[index]
    val operation = element as? DomposableModifierOperation
    val fillHeightAhead =
        operation != DomposableModifierOperation.FillHeight &&
            elements.asSequence().drop(index).firstOrNull { candidate ->
                when (candidate) {
                    DomposableModifierOperation.FillHeight,
                    is DomposableModifierOperation.Height,
                    is DomposableModifierOperation.WrapContentSize,
                    -> {
                        true
                    }

                    is DomposableModifierOperation.Scroll -> {
                        candidate.orientation == DomposableOrientation.Vertical
                    }

                    is DomposableModifierOperation.StatefulScroll -> {
                        candidate.orientation ==
                            DomposableOrientation.Vertical
                    }

                    else -> {
                        false
                    }
                }
            } == DomposableModifierOperation.FillHeight
    if (operation == null) {
        val decoration = decorationRenderer?.invoke(element)
        if (decoration != null) {
            decoration {
                HtmlModifierBox(
                    elements,
                    index + 1,
                    operationRenderer,
                    decorationRenderer,
                    fillAvailableHeight,
                    pointerPassthrough,
                    content,
                )
            }
            return
        }
        Div(attrs = {
            if (pointerPassthrough) attr("data-domposable-pointer-passthrough", "")
            if (fillAvailableHeight || fillHeightAhead) {
                style {
                    property("display", "grid")
                    property("grid-template-columns", "minmax(0, 1fr)")
                    property("grid-template-rows", "minmax(0, 1fr)")
                    if (fillHeightAhead) property("height", "100%")
                }
            }
            val attributes =
                operationRenderer?.invoke(element)
                    ?: error("No HTML renderer handled application modifier element ${element::class.simpleName}")
            attributes()
        }) {
            HtmlModifierBox(
                elements,
                index + 1,
                operationRenderer,
                decorationRenderer,
                fillAvailableHeight,
                pointerPassthrough,
                content,
            )
        }
        return
    }
    val wrapContentSize = operation is DomposableModifierOperation.WrapContentSize
    val verticalScroll =
        when (operation) {
            is DomposableModifierOperation.Scroll -> operation.orientation == DomposableOrientation.Vertical
            is DomposableModifierOperation.StatefulScroll -> operation.orientation == DomposableOrientation.Vertical
            else -> false
        }
    val establishesHeight =
        operation is DomposableModifierOperation.Height ||
            operation is DomposableModifierOperation.MinHeight ||
            operation is DomposableModifierOperation.MaxHeight ||
            operation == DomposableModifierOperation.FillHeight
    // CSS block height stops at each nested wrapper; Compose passes these constraints through to its child.
    val childFillsHeight =
        !verticalScroll &&
            (establishesHeight || (fillAvailableHeight && !wrapContentSize) || fillHeightAhead)
    Div(attrs = {
        attr("data-domposable-modifier", operation::class.simpleName ?: "operation")
        if (fillHeightAhead) attr("data-domposable-fill-height-ahead", "")
        if (pointerPassthrough) {
            attr("data-domposable-pointer-passthrough", "")
        }
        style {
            property("box-sizing", "border-box")
            if (childFillsHeight) {
                property("display", "grid")
                property("grid-template-columns", "minmax(0, 1fr)")
                property("grid-template-rows", "minmax(0, 1fr)")
            }
            if (fillHeightAhead) property("height", "100%")
            if (fillAvailableHeight && wrapContentSize) property("height", "100%")
            if (operation == DomposableModifierOperation.PointerPassthrough) property("pointer-events", "none")
            when (operation) {
                is DomposableModifierOperation.Padding -> {
                    property("padding-block-start", "${operation.insets.top.value}px")
                    property("padding-inline-end", "${operation.insets.end.value}px")
                    property("padding-block-end", "${operation.insets.bottom.value}px")
                    property("padding-inline-start", "${operation.insets.start.value}px")
                }

                is DomposableModifierOperation.Width -> {
                    property("width", "${operation.length.value}px")
                }

                is DomposableModifierOperation.Height -> {
                    property("height", "${operation.length.value}px")
                }

                is DomposableModifierOperation.MaxWidth -> {
                    property("max-width", "${operation.length.value}px")
                }

                is DomposableModifierOperation.FillWidthFraction -> {
                    property("width", "${operation.fraction * 100}%")
                    operation.minWidth?.let { property("min-width", "min(${it.value}px, 100%)") }
                    operation.maxWidth?.let { property("max-width", "min(${it.value}px, 100%)") }
                }

                is DomposableModifierOperation.MinWidth -> {
                    property("min-width", "${operation.length.value}px")
                }

                is DomposableModifierOperation.MinHeight -> {
                    property("min-height", "${operation.length.value}px")
                }

                is DomposableModifierOperation.MaxHeight -> {
                    property("max-height", "${operation.length.value}px")
                }

                DomposableModifierOperation.FillWidth -> {
                    property("width", "100%")
                }

                DomposableModifierOperation.PointerPassthrough -> {
                    Unit
                }

                DomposableModifierOperation.FillHeight -> {
                    property("height", "100%")
                }

                is DomposableModifierOperation.Background -> {
                    property("background", operation.color.css())
                }

                is DomposableModifierOperation.ShapeBackground -> {
                    property("background", operation.color.css())
                    applyCssShape(operation.shape)
                }

                is DomposableModifierOperation.GradientBackground -> {
                    property("background", operation.gradient.css())
                    applyCssShape(operation.shape)
                }

                is DomposableModifierOperation.Radius -> {
                    property("border-radius", "${operation.length.value}px")
                    property("overflow", "hidden")
                }

                is DomposableModifierOperation.ShapeClip -> {
                    applyCssShape(operation.shape)
                    property("overflow", "hidden")
                }

                is DomposableModifierOperation.Border -> {
                    property("border", "${operation.width.value}px solid ${operation.color.css()}")
                    applyCssShape(operation.shape)
                }

                is DomposableModifierOperation.EdgeBorder -> {
                    property(operation.edge.cssProperty(), "${operation.width.value}px solid ${operation.color.css()}")
                }

                is DomposableModifierOperation.Region -> {
                    Unit
                }

                is DomposableModifierOperation.AspectRatio -> {
                    property("aspect-ratio", operation.ratio.toString())
                }

                is DomposableModifierOperation.Offset -> {
                    // Compose's horizontal offset follows layout direction. Logical insets
                    // preserve that behavior in RTL without changing the layout slot.
                    property("position", "relative")
                    property("inset-inline-start", "${operation.value.x}px")
                    property("inset-block-start", "${operation.value.y}px")
                }

                is DomposableModifierOperation.WrapContentSize -> {
                    property("display", "flex")
                    property("justify-content", operation.horizontal.css())
                    property("align-items", operation.vertical.css())
                }

                is DomposableModifierOperation.Opacity -> {
                    property("opacity", operation.value.toString())
                }

                is DomposableModifierOperation.Scale -> {
                    property("transform", "scale(${operation.x},${operation.y})")
                }

                is DomposableModifierOperation.Rotation -> {
                    property("transform", "rotate(${operation.degrees}deg)")
                }

                is DomposableModifierOperation.Shadow -> {
                    applyCssShape(operation.shape)
                    val elevation = operation.elevation.value
                    property(
                        "box-shadow",
                        if (elevation ==
                            0f
                        ) {
                            "none"
                        } else {
                            "0 ${elevation}px ${elevation * 2}px rgb(0 0 0 / .24)"
                        },
                    )
                }

                is DomposableModifierOperation.Click -> {
                    property("cursor", "pointer")
                }

                is DomposableModifierOperation.InteractiveClick -> {
                    if (operation.enabled) {
                        property("cursor", "pointer")
                    }
                }

                is DomposableModifierOperation.Description,
                is DomposableModifierOperation.TestId,
                -> {
                    Unit
                }

                is DomposableModifierOperation.Scroll -> {
                    val vertical = operation.orientation == DomposableOrientation.Vertical
                    property(
                        if (vertical) "overflow-y" else "overflow-x",
                        "auto",
                    )
                    property(if (vertical) "max-height" else "max-width", "100%")
                }

                is DomposableModifierOperation.StatefulScroll -> {
                    val vertical = operation.orientation == DomposableOrientation.Vertical
                    property(
                        if (vertical) "overflow-y" else "overflow-x",
                        "auto",
                    )
                    property(if (vertical) "max-height" else "max-width", "100%")
                }

                is DomposableModifierOperation.Weight,
                is DomposableModifierOperation.AxisAlignment,
                is DomposableModifierOperation.BoxAlignment,
                is DomposableModifierOperation.ZIndex,
                -> {
                    error("Child layout operation should have been applied to the item wrapper")
                }
            }
        }
        when (operation) {
            is DomposableModifierOperation.Click -> {
                attr("role", "button")
                attr("tabindex", "0")
                attr("aria-label", operation.label)
                onClick { operation.onClick() }
                onKeyDown {
                    if (it.key == "Enter" || it.key == " ") {
                        it.preventDefault()
                        operation.onClick()
                    }
                }
            }

            is DomposableModifierOperation.InteractiveClick -> {
                attr("role", operation.role.htmlRole())
                if (operation.label != null) attr("aria-label", operation.label)
                if (operation.enabled) {
                    attr("tabindex", "0")
                    onClick { operation.onClick() }
                    onKeyDown {
                        val activate =
                            if (operation.role == DomposableClickRole.Link) {
                                it.key == "Enter"
                            } else {
                                it.key == "Enter" || it.key == " "
                            }
                        if (activate) {
                            it.preventDefault()
                            operation.onClick()
                        }
                    }
                } else {
                    attr("aria-disabled", "true")
                }
            }

            is DomposableModifierOperation.Region -> {
                attr("role", "region")
                attr("aria-label", operation.label)
            }

            is DomposableModifierOperation.StatefulScroll -> {
                domposableScrollState(operation.state, operation.orientation)
            }

            else -> {
                Unit
            }
        }
    }) {
        if (operation == DomposableModifierOperation.PointerPassthrough) {
            TagElement<HTMLElement>("style", applyAttrs = {}) { Text(POINTER_PASSTHROUGH_CSS) }
        }
        HtmlModifierBox(
            elements,
            index + 1,
            operationRenderer,
            decorationRenderer,
            childFillsHeight,
            pointerPassthrough,
            content,
        )
    }
}

internal fun DomposableLinearGradient.css(): String {
    val direction =
        when (axis) {
            DomposableLayoutAxis.Horizontal -> "to right"
            DomposableLayoutAxis.Vertical -> "to bottom"
        }
    val stops = stops.joinToString(", ") { "${it.color.css()} ${it.position * 100}%" }
    return "linear-gradient($direction, $stops)"
}

internal fun DomposableBorderEdge.cssProperty(): String =
    when (this) {
        DomposableBorderEdge.Top -> "border-block-start"
        DomposableBorderEdge.End -> "border-inline-end"
        DomposableBorderEdge.Bottom -> "border-block-end"
        DomposableBorderEdge.Start -> "border-inline-start"
    }

private fun isChildLayoutOperation(element: DomposableModifierElement): Boolean =
    element is DomposableModifierOperation.Weight ||
        element is DomposableModifierOperation.AxisAlignment ||
        element is DomposableModifierOperation.BoxAlignment ||
        element is DomposableModifierOperation.ZIndex

private fun DomposableAlignment.css(): String =
    when (this) {
        DomposableAlignment.Start -> "flex-start"
        DomposableAlignment.Center -> "center"
        DomposableAlignment.End -> "flex-end"
    }

private fun org.jetbrains.compose.web.css.StyleScope.applyCssShape(shape: DomposableShape) {
    when (shape) {
        DomposableShape.Rectangle -> Unit
        DomposableShape.Circle -> property("border-radius", "9999px")
        is DomposableShape.Rounded -> property("border-radius", "${shape.radius.value}px")
        is DomposableShape.Corners -> cornerRadii(shape)
    }
}

private fun org.jetbrains.compose.web.css.StyleScope.cornerRadii(shape: DomposableShape.Corners) {
    property("border-start-start-radius", "${shape.topStart.value}px")
    property("border-start-end-radius", "${shape.topEnd.value}px")
    property("border-end-end-radius", "${shape.bottomEnd.value}px")
    property("border-end-start-radius", "${shape.bottomStart.value}px")
}

private fun DomposableClickRole.htmlRole(): String =
    when (this) {
        DomposableClickRole.Button -> "button"
        DomposableClickRole.Link -> "link"
        DomposableClickRole.Checkbox -> "checkbox"
        DomposableClickRole.RadioButton -> "radio"
        DomposableClickRole.Switch -> "switch"
        DomposableClickRole.Image -> "img"
    }

/** HTML measures and scrolls in CSS pixels; native Compose exposes its pixel ScrollState value. */
@Composable
actual fun DomposableMeasure(
    modifier: DomposableModifier,
    content: @Composable (DomposableSize) -> Unit,
) {
    var size by remember { mutableStateOf(DomposableSize(0f, 0f)) }
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            style {
                property("width", "100%")
                property("min-height", "0")
                property("height", "100%")
            }
            ref { element ->
                val observer =
                    DomposableResizeObserver { entries ->
                        if (entries.isNotEmpty()) {
                            val rect = entries[0].contentRect
                            size = DomposableSize((rect.width as Double).toFloat(), (rect.height as Double).toFloat())
                        }
                    }
                observer.observe(element)
                onDispose { observer.disconnect() }
            }
        }) { content(size) }
    }
}
