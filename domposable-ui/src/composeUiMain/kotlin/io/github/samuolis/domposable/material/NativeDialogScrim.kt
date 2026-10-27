package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import io.github.samuolis.domposable.ui.DomposableDialogProps

internal expect fun dialogProperties(
    props: DomposableDialogProps,
    usePlatformDefaultWidth: Boolean = true,
): DialogProperties

@Composable
internal expect fun ApplyNativeDialogScrim(opacity: Float?)
