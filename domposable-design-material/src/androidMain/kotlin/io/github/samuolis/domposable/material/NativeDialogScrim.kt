package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import io.github.samuolis.domposable.ui.DomposableDialogProps

internal actual fun dialogProperties(
    props: DomposableDialogProps,
    usePlatformDefaultWidth: Boolean,
): DialogProperties =
    DialogProperties(
        usePlatformDefaultWidth = usePlatformDefaultWidth && !props.layout.fillViewportWidth,
        dismissOnBackPress = props.dismissOnBackPress,
        dismissOnClickOutside = props.dismissOnClickOutside,
    )

@Composable
internal actual fun ApplyNativeDialogScrim(opacity: Float?) {
    if (opacity == null) return
    val window = (LocalView.current.parent as DialogWindowProvider).window
    DisposableEffect(window, opacity) {
        val previous = window.attributes.dimAmount
        window.setDimAmount(opacity)
        onDispose { window.setDimAmount(previous) }
    }
}
