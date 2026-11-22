package io.github.samuolis.domposable.rive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.toComposeModifier

@Composable
actual fun DomposableRive(
    props: DomposableRiveProps,
    modifier: DomposableModifier,
    fallback: (@Composable () -> Unit)?,
) {
    Box(
        modifier.toComposeModifier().semantics {
            props.contentDescription?.let { contentDescription = it }
        },
    ) {
        fallback?.invoke()
        key(
            props.file,
            props.artboardName,
            props.animationName,
            props.stateMachineName,
            props.autoplay,
            props.playbackKey,
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    RiveAnimationView
                        .Builder(context)
                        .setResource(props.file)
                        .apply { props.artboardName?.let { setArtboardName(it) } }
                        .apply { props.animationName?.let { setAnimationName(it) } }
                        .apply { props.stateMachineName?.let { setStateMachineName(it) } }
                        .setAutoplay(props.autoplay)
                        .build()
                },
            )
        }
    }
}
