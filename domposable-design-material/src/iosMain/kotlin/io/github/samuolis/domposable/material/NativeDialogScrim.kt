package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties
import io.github.samuolis.domposable.ui.DomposableDialogProps

@OptIn(ExperimentalComposeUiApi::class)
internal actual fun dialogProperties(
    props: DomposableDialogProps,
    usePlatformDefaultWidth: Boolean,
): DialogProperties =
    props.scrimOpacity?.let { opacity ->
        DialogProperties(
            usePlatformDefaultWidth = usePlatformDefaultWidth && !props.layout.fillViewportWidth,
            dismissOnBackPress = props.dismissOnBackPress,
            dismissOnClickOutside = props.dismissOnClickOutside,
            scrimColor = Color.Black.copy(alpha = opacity),
        )
    } ?: DialogProperties(
        usePlatformDefaultWidth = usePlatformDefaultWidth && !props.layout.fillViewportWidth,
        dismissOnBackPress = props.dismissOnBackPress,
        dismissOnClickOutside = props.dismissOnClickOutside,
    )

@Composable
internal actual fun ApplyNativeDialogScrim(opacity: Float?) = Unit
