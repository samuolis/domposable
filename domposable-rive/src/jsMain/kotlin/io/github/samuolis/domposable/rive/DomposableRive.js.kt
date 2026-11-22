package io.github.samuolis.domposable.rive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.domposableAttributes
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement

@Composable
actual fun DomposableRive(
    props: DomposableRiveProps,
    modifier: DomposableModifier,
    fallback: (@Composable () -> Unit)?,
) {
    val host = remember { mutableStateOf<HTMLElement?>(null) }
    val loaded = remember(props) { mutableStateOf(false) }
    DisposableEffect(host.value, props) {
        val parent = host.value
        if (parent == null) {
            onDispose {}
        } else {
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            canvas.setAttribute("aria-hidden", "true")
            canvas.style.position = "absolute"
            canvas.style.setProperty("inset", "0")
            canvas.style.width = "100%"
            canvas.style.height = "100%"
            canvas.style.visibility = "hidden"
            parent.appendChild(canvas)
            @Suppress("UNUSED_VARIABLE")
            val fileSize = props.file.size
            val bytes = js("new Uint8Array(fileSize)")
            props.file.forEachIndexed { index, byte -> bytes[index] = byte.toInt() and 0xff }
            val options = js("({})")
            options.buffer = bytes.buffer
            options.canvas = canvas
            options.autoplay = props.autoplay
            props.artboardName?.let { options.artboard = it }
            props.animationName?.let { options.animations = it }
            props.stateMachineName?.let { options.stateMachine = it }
            var rive: Rive? = null
            var loadFrame: Int? = null
            options.onLoad = {
                loadFrame =
                    window.requestAnimationFrame {
                        rive?.resizeDrawingSurfaceToCanvas()
                        loaded.value = true
                        canvas.style.visibility = "visible"
                    }
            }
            rive = runCatching { Rive(options) }.getOrNull()
            @Suppress("UNUSED_VARIABLE")
            val resize: (dynamic, dynamic) -> Unit = { _, _ ->
                if (loaded.value) rive?.resizeDrawingSurfaceToCanvas()
            }
            val observer = js("new ResizeObserver(resize)")
            observer.observe(parent)
            onDispose {
                loadFrame?.let(window::cancelAnimationFrame)
                observer.disconnect()
                rive?.cleanup()
                canvas.remove()
            }
        }
    }
    Div(attrs = {
        domposableAttributes(modifier)
        attr("role", "img")
        props.contentDescription?.let { attr("aria-label", it) }
    }) {
        Div(attrs = {
            style {
                property("position", "relative")
                property("width", "100%")
                property("height", "100%")
            }
            ref { element ->
                host.value = element
                onDispose { host.value = null }
            }
        }) {
            fallback?.let { content ->
                Div(attrs = {
                    if (loaded.value) style { property("visibility", "hidden") }
                }) {
                    content()
                }
            }
        }
    }
}
