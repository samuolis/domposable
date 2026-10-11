package io.github.samuolis.domposable.ui

import org.w3c.dom.HTMLElement

@JsName("ResizeObserver")
internal external class DomposableResizeObserver(
    callback: (Array<dynamic>) -> Unit,
) {
    fun observe(element: HTMLElement)

    fun disconnect()
}
