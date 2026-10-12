package io.github.samuolis.domposable.material

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import io.github.samuolis.domposable.ui.DomposableBottomSheetProps
import io.github.samuolis.domposable.ui.DomposableDialogProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.toComposeModifier

/** Platform window identifiers are transient; the feature's content keeps its portable registry. */
@Composable
internal fun NativeDialog(
    props: DomposableDialogProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val featureRegistry = LocalSaveableStateRegistry.current
    CompositionLocalProvider(LocalSaveableStateRegistry provides null) {
        AlertDialog(
            onDismissRequest = props.onDismiss,
            confirmButton = {
                CompositionLocalProvider(
                    LocalSaveableStateRegistry provides featureRegistry,
                ) { props.actions() }
            },
            dismissButton = { TextButton(props.onDismiss) { Text("Close") } },
            title = { Text(props.title) },
            text = { CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) { content() } },
            modifier = modifier.toComposeModifier(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NativeBottomSheet(
    props: DomposableBottomSheetProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val featureRegistry = LocalSaveableStateRegistry.current
    CompositionLocalProvider(LocalSaveableStateRegistry provides null) {
        ModalBottomSheet(onDismissRequest = props.onDismiss, modifier = modifier.toComposeModifier()) {
            Text(props.title, style = MaterialTheme.typography.titleLarge)
            CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) { content() }
            TextButton(props.onDismiss) { Text("Close") }
        }
    }
}
