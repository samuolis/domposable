package io.github.samuolis.domposable.rive

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.key
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.UIKitView
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.toComposeModifier
import platform.UIKit.UIView
import platform.UIKit.UIColor

/** App-provided adapter to Rive's official Swift runtime. Register it during iOS app startup. */
interface DomposableRiveIosRenderer {
    fun createView(props: DomposableRiveProps): UIView
    fun updateView(view: UIView, props: DomposableRiveProps)
    fun disposeView(view: UIView)
}

/** Keeps the Rive iOS SDK optional; apps link RiveRuntime and install their adapter here. */
object DomposableRiveIosRuntime {
    var renderer: DomposableRiveIosRenderer? = null
}

@Composable
actual fun DomposableRive(
    props: DomposableRiveProps,
    modifier: DomposableModifier,
    fallback: (@Composable () -> Unit)?,
) {
    val renderer = DomposableRiveIosRuntime.renderer
    key(props.file, props.artboardName, props.animationName, props.stateMachineName, renderer) {
        Box(modifier.toComposeModifier().semantics {
            props.contentDescription?.let { contentDescription = it }
        }) {
            fallback?.invoke()
            UIKitView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    renderer?.createView(props) ?: UIView().apply {
                        setOpaque(false)
                        backgroundColor = UIColor.clearColor
                    }
                },
                update = { view -> renderer?.updateView(view, props) },
                onRelease = { view -> renderer?.disposeView(view) },
            )
        }
    }
}
