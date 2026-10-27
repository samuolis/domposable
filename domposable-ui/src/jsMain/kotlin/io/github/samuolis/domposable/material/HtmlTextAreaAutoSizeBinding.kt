package io.github.samuolis.domposable.material

import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLTextAreaElement

/** Keeps the observer attached while controlled values and line limits change. */
internal class HtmlTextAreaAutoSizeBinding {
    var minLines: Int = 1
    var maxLines: Int = Int.MAX_VALUE
    private var resizeAction: (() -> Unit)? = null

    fun resize() {
        resizeAction?.invoke()
    }

    fun install(action: () -> Unit) {
        resizeAction = action
    }

    fun clear() {
        resizeAction = null
    }
}

internal fun installHtmlTextAreaAutoSize(
    element: Element,
    binding: HtmlTextAreaAutoSizeBinding,
): () -> Unit {
    val textarea = element as? HTMLTextAreaElement ?: return {}
    var frame: Int? = null
    val resize = {
        if (frame == null) {
            frame =
                window.requestAnimationFrame {
                    frame = null
                    val computed = window.getComputedStyle(textarea)
                    val lineHeight = computed.lineHeight.removeSuffix("px").toFloatOrNull() ?: 24f
                    val chrome =
                        listOf(
                            computed.paddingTop,
                            computed.paddingBottom,
                            computed.borderTopWidth,
                            computed.borderBottomWidth,
                        ).sumOf { (it.removeSuffix("px").toDoubleOrNull() ?: 0.0) }
                            .toFloat()
                    val border =
                        (computed.borderTopWidth.removeSuffix("px").toFloatOrNull() ?: 0f) +
                            (computed.borderBottomWidth.removeSuffix("px").toFloatOrNull() ?: 0f)
                    val minimum = binding.minLines * lineHeight + chrome
                    val maximum =
                        if (binding.maxLines ==
                            Int.MAX_VALUE
                        ) {
                            Float.POSITIVE_INFINITY
                        } else {
                            binding.maxLines * lineHeight + chrome
                        }
                    textarea.style.height = "auto"
                    val contentHeight = textarea.scrollHeight + border
                    textarea.style.height = "${contentHeight.coerceIn(minimum, maximum)}px"
                    textarea.style.overflowY = if (contentHeight > maximum) "auto" else "hidden"
                }
        }
    }
    binding.install(resize)
    resize()
    val observer = ResizeObserver { _, _ -> resize() }
    observer.observe(textarea)
    textarea.parentElement?.let(observer::observe)
    return {
        observer.disconnect()
        frame?.let(window::cancelAnimationFrame)
        binding.clear()
    }
}

private external class ResizeObserver(
    callback: (Array<dynamic>, ResizeObserver) -> Unit,
) {
    fun observe(target: Element)

    fun disconnect()
}
