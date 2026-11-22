package io.github.samuolis.domposable.rive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.domposableAttributes
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import kotlin.js.JsModule
import kotlin.js.JsNonModule

@JsModule("@rive-app/canvas")
@JsNonModule
private external class Rive(options: dynamic) {
    fun cleanup()
}

@Composable
actual fun DomposableRive(
    props: DomposableRiveProps,
    modifier: DomposableModifier,
    fallback: (@Composable () -> Unit)?,
) {
    val host = remember { mutableStateOf<HTMLElement?>(null) }
    DisposableEffect(host.value, props) {
        val parent = host.value
        if (parent == null) {
            onDispose {}
        } else {
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            canvas.setAttribute("aria-hidden", "true")
            canvas.style.width = "100%"
            canvas.style.height = "100%"
            parent.appendChild(canvas)
            val bytes = js("new Uint8Array(props.file.length)")
            props.file.forEachIndexed { index, byte -> bytes[index] = byte.toInt() and 0xff }
            val options = js("({})")
            options.buffer = bytes.buffer
            options.canvas = canvas
            options.autoplay = props.autoplay
            props.artboardName?.let { options.artboard = it }
            props.animationName?.let { options.animations = it }
            props.stateMachineName?.let { options.stateMachine = it }
            val rive = Rive(options)
            onDispose {
                rive.cleanup()
                canvas.remove()
            }
        }
    }
    Div(attrs = {
        domposableAttributes(modifier)
        attr("role", "img")
        props.contentDescription?.let { attr("aria-label", it) }
        ref { element ->
            host.value = element
            onDispose { host.value = null }
        }
    }) {
        fallback?.invoke()
    }
}
