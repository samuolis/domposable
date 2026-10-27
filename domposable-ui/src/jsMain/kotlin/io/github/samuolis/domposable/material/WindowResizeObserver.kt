package io.github.samuolis.domposable.material

import org.w3c.dom.Element

@JsName("ResizeObserver")
internal external class WindowResizeObserver(
    callback: (Array<dynamic>) -> Unit,
) {
    fun observe(target: Element)

    fun disconnect()
}
