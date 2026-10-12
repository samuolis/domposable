package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableSize
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLElement

/** Application-owned HTML adapter for an open [DomposableModifierElement]. */
typealias DomposableHtmlModifierElementRenderer =
    (DomposableModifierElement) -> (AttrsScope<HTMLElement>.() -> Unit)?

/** Product providers can install brand element handling once for every descendant control. */
val LocalDomposableHtmlModifierRenderer =
    staticCompositionLocalOf<DomposableHtmlModifierElementRenderer?> { null }

/** Semantic attributes attach to the control, not to its layout wrappers. */
fun <T : HTMLElement> AttrsScope<T>.domposableAttributes(modifier: DomposableModifier) {
    modifier.elements.forEach { element ->
        when (element) {
            is DomposableModifierOperation.Description -> attr("aria-label", element.text)
            is DomposableModifierOperation.TestId -> attr("data-testid", element.value)
            else -> Unit
        }
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
) {
    val renderer = operationRenderer ?: LocalDomposableHtmlModifierRenderer.current
    val layout = modifier.elements.filter(::isChildLayoutOperation)
    val visual =
        modifier.elements.filterNot(::isChildLayoutOperation).filterNot {
            it is DomposableModifierOperation.Description || it is DomposableModifierOperation.TestId
        }
    if (layout.isEmpty()) {
        HtmlModifierBox(visual, 0, renderer, content)
    } else {
        Div(attrs = {
            attr("data-domposable-layout-item", "true")
            style {
                property("box-sizing", "border-box")
                layout.forEach { element ->
                    when (element) {
                        is DomposableModifierOperation.Weight -> {
                            property("flex-grow", element.weight.toString())
                            property("flex-shrink", if (element.fill) "1" else "0")
                            property("flex-basis", if (element.fill) "0px" else "auto")
                        }

                        is DomposableModifierOperation.AxisAlignment -> {
                            property("align-self", element.alignment.css())
                        }

                        is DomposableModifierOperation.BoxAlignment -> {
                            property("place-self", "${element.vertical.css()} ${element.horizontal.css()}")
                        }

                        else -> {
                            error("Unexpected child layout modifier element: $element")
                        }
                    }
                }
            }
        }) { HtmlModifierBox(visual, 0, renderer, content) }
    }
}

@Composable
private fun HtmlModifierBox(
    elements: List<DomposableModifierElement>,
    index: Int,
    operationRenderer: DomposableHtmlModifierElementRenderer?,
    content: @Composable () -> Unit,
) {
    if (index == elements.size) {
        content()
        return
    }
    val element = elements[index]
    val operation = element as? DomposableModifierOperation
    if (operation == null) {
        Div(attrs = {
            val attributes =
                operationRenderer?.invoke(element)
                    ?: error("No HTML renderer handled application modifier element ${element::class.simpleName}")
            attributes()
        }) {
            HtmlModifierBox(elements, index + 1, operationRenderer, content)
        }
        return
    }
    var scrollElement: HTMLElement? by remember(operation) { mutableStateOf(null) }
    if (operation is DomposableModifierOperation.StatefulScroll) {
        LaunchedEffect(operation.state.value) {
            val target = scrollElement ?: return@LaunchedEffect
            if (operation.orientation == DomposableOrientation.Vertical) {
                target.scrollTop = operation.state.value.toDouble()
            } else {
                target.scrollLeft = operation.state.value.toDouble()
            }
        }
    }
    Div(attrs = {
        attr("data-domposable-modifier", operation::class.simpleName ?: "operation")
        style {
            property("box-sizing", "border-box")
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

                is DomposableModifierOperation.AspectRatio -> {
                    property("aspect-ratio", operation.ratio.toString())
                }

                is DomposableModifierOperation.ZIndex -> {
                    property("z-index", operation.value.toString())
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

            is DomposableModifierOperation.StatefulScroll -> {
                ref { element ->
                    scrollElement = element
                    if (operation.orientation == DomposableOrientation.Vertical) {
                        element.scrollTop = operation.state.value.toDouble()
                    } else {
                        element.scrollLeft = operation.state.value.toDouble()
                    }
                    onDispose { scrollElement = null }
                }
                onScroll { event ->
                    val target = event.nativeEvent.currentTarget as HTMLElement
                    if (operation.orientation == DomposableOrientation.Vertical) {
                        operation.state.updateFromElement(target.scrollTop.toInt())
                    } else {
                        operation.state.updateFromElement(target.scrollLeft.toInt())
                    }
                }
            }

            else -> {
                Unit
            }
        }
    }) {
        HtmlModifierBox(elements, index + 1, operationRenderer, content)
    }
}

private fun isChildLayoutOperation(element: DomposableModifierElement): Boolean =
    element is DomposableModifierOperation.Weight ||
        element is DomposableModifierOperation.AxisAlignment ||
        element is DomposableModifierOperation.BoxAlignment

private fun DomposableAlignment.css(): String =
    when (this) {
        DomposableAlignment.Start -> "flex-start"
        DomposableAlignment.Center -> "center"
        DomposableAlignment.End -> "flex-end"
    }

private fun org.jetbrains.compose.web.css.StyleScope.applyCssShape(shape: DomposableShape) {
    when (shape) {
        DomposableShape.Rectangle -> Unit
        DomposableShape.Circle -> property("border-radius", "50%")
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
