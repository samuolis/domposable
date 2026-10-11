package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableSize
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLElement

/** Semantic attributes attach to the control, not to its layout wrappers. */
fun <T : HTMLElement> AttrsScope<T>.domposableAttributes(modifier: DomposableModifier) {
    modifier.operations.forEach { operation ->
        when (operation) {
            is DomposableModifierOperation.Description -> attr("aria-label", operation.text)
            is DomposableModifierOperation.TestId -> attr("data-testid", operation.value)
            else -> Unit
        }
    }
}

/** Each layout operation has its own box, preserving padding/background/width order. */
@Composable
fun DomposableHtmlModifiers(
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    HtmlModifierBox(
        modifier.operations.filterNot {
            it is DomposableModifierOperation.Description ||
                it is DomposableModifierOperation.TestId
        },
        0,
        content,
    )
}

@Composable
private fun HtmlModifierBox(
    operations: List<DomposableModifierOperation>,
    index: Int,
    content: @Composable () -> Unit,
) {
    if (index == operations.size) {
        content()
        return
    }
    val operation = operations[index]
    Div(attrs = {
        attr("data-domposable-modifier", operation::class.simpleName ?: "operation")
        style {
            property("box-sizing", "border-box")
            when (operation) {
                is DomposableModifierOperation.Padding -> {
                    property(
                        "padding",
                        "${operation.insets.top.value}px ${operation.insets.end.value}px " +
                            "${operation.insets.bottom.value}px ${operation.insets.start.value}px",
                    )
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

                DomposableModifierOperation.FillWidth -> {
                    property("width", "100%")
                }

                DomposableModifierOperation.FillHeight -> {
                    property("height", "100%")
                }

                is DomposableModifierOperation.Background -> {
                    property("background", operation.color.css())
                }

                is DomposableModifierOperation.Radius -> {
                    property("border-radius", "${operation.length.value}px")
                    property("overflow", "hidden")
                }

                is DomposableModifierOperation.Opacity -> {
                    property("opacity", operation.value.toString())
                }

                is DomposableModifierOperation.Scroll -> {
                    property(
                        if (operation.orientation ==
                            DomposableOrientation.Vertical
                        ) {
                            "overflow-y"
                        } else {
                            "overflow-x"
                        },
                        "auto",
                    )
                    property("max-height", "100%")
                }

                else -> {
                    Unit
                }
            }
        }
        if (operation is DomposableModifierOperation.Click) {
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
    }) { HtmlModifierBox(operations, index + 1, content) }
}

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
