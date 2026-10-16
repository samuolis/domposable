package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BottomSheet
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.samuolis.domposable.ui.DomposableBottomSheetProps
import io.github.samuolis.domposable.ui.DomposableDialogProps
import io.github.samuolis.domposable.ui.DomposableDialogVariant
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.toComposeModifier

/** Platform window IDs never enter a portable entry registry. Feature slots restore their owner. */
@Composable
internal fun NativeWindow(content: @Composable (SaveableStateRegistry?) -> Unit) {
    val registry = LocalSaveableStateRegistry.current
    CompositionLocalProvider(LocalSaveableStateRegistry provides null) { content(registry) }
}

/** Platform window identifiers are transient; the feature's content keeps its portable registry. */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun NativeDialog(
    props: DomposableDialogProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeWindow { featureRegistry ->
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.dialog
        val confirm = @Composable {
            CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) { props.actions() }
        }
        val body = @Composable {
            CompositionLocalProvider(
                LocalSaveableStateRegistry provides featureRegistry,
            ) { content() }
        }
        val close = @Composable { TextButton(props.onDismiss) { Text("Close") } }
        when (props.variant) {
            DomposableDialogVariant.Alert -> {
                AlertDialog(
                    onDismissRequest = props.onDismiss,
                    confirmButton = confirm,
                    dismissButton = close,
                    title = {
                        Text(
                            props.title,
                            style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.headlineSmall),
                        )
                    },
                    text = body,
                    modifier =
                        modifier.toComposeModifier().then(
                            Modifier.nativeModifier(appearance, AlertDialogDefaults.shape),
                        ),
                    shape = appearance.nativeShape(AlertDialogDefaults.shape),
                    containerColor = appearance.container?.nativeColor() ?: AlertDialogDefaults.containerColor,
                )
            }

            DomposableDialogVariant.DatePicker -> {
                DatePickerDialog(
                    onDismissRequest = props.onDismiss,
                    confirmButton = confirm,
                    dismissButton = close,
                    modifier = modifier.toComposeModifier(),
                    shape = appearance.nativeShape(DatePickerDefaults.shape),
                    tonalElevation = appearance.tonalElevation?.value?.dp ?: DatePickerDefaults.TonalElevation,
                    colors =
                        DatePickerDefaults.colors(
                            containerColor =
                                appearance.container?.nativeColor() ?: androidx.compose.ui.graphics.Color.Unspecified,
                        ),
                ) {
                    Text(props.title)
                    body()
                }
            }

            DomposableDialogVariant.TimePicker -> {
                TimePickerDialog(
                    onDismissRequest = props.onDismiss,
                    confirmButton = confirm,
                    dismissButton = close,
                    title = { Text(props.title) },
                    modifier = modifier.toComposeModifier(),
                    shape = appearance.nativeShape(TimePickerDialogDefaults.shape),
                    containerColor = appearance.container?.nativeColor() ?: TimePickerDialogDefaults.containerColor,
                ) { body() }
            }

            DomposableDialogVariant.VibrantTimePicker -> {
                // The vibrant dialog shell is 1.13-only; keep its slots on the stable dialog API.
                TimePickerDialog(
                    onDismissRequest = props.onDismiss,
                    confirmButton = confirm,
                    dismissButton = close,
                    title = { Text(props.title) },
                    modifier = modifier.toComposeModifier(),
                    shape = appearance.nativeShape(MaterialTheme.shapes.extraLarge),
                    containerColor =
                        appearance.container?.nativeColor() ?: MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    body()
                }
            }

            DomposableDialogVariant.Basic, DomposableDialogVariant.FullScreen -> {
                BasicAlertDialog(
                    onDismissRequest = props.onDismiss,
                    modifier =
                        modifier.toComposeModifier().then(
                            if (props.variant ==
                                DomposableDialogVariant.FullScreen
                            ) {
                                Modifier.fillMaxSize()
                            } else {
                                Modifier
                            },
                        ),
                    properties =
                        DialogProperties(
                            usePlatformDefaultWidth =
                                props.variant != DomposableDialogVariant.FullScreen,
                        ),
                ) {
                    Surface(
                        modifier = Modifier,
                        shape = appearance.nativeShape(MaterialTheme.shapes.extraLarge),
                        color = appearance.container?.nativeColor() ?: MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = appearance.content?.nativeColor() ?: MaterialTheme.colorScheme.onSurface,
                        tonalElevation = appearance.tonalElevation?.value?.dp ?: 0.dp,
                        shadowElevation = appearance.shadowElevation?.value?.dp ?: 0.dp,
                        border =
                            appearance.border?.let {
                                androidx.compose.foundation.BorderStroke(
                                    appearance.borderWidth.value.dp,
                                    it.nativeColor(),
                                )
                            },
                    ) {
                        Column {
                            Text(
                                props.title,
                                style =
                                    appearance.textAppearance.nativeTextStyle(
                                        MaterialTheme.typography.headlineSmall,
                                    ),
                            )
                            body()
                            confirm()
                            close()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NativeBottomSheet(
    props: DomposableBottomSheetProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.bottomSheet
    val shape = appearance.nativeShape(BottomSheetDefaults.ExpandedShape)
    val container = appearance.container?.nativeColor() ?: BottomSheetDefaults.ContainerColor
    val contentColor = appearance.content?.nativeColor() ?: contentColorFor(container)
    NativeWindow { featureRegistry ->
        val sheetContent: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit = {
            Text(props.title, style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.titleLarge))
            CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) { content() }
            TextButton(props.onDismiss) { Text("Close") }
        }
        if (props.modal) {
            ModalBottomSheet(
                onDismissRequest = props.onDismiss,
                modifier = modifier.toComposeModifier().then(Modifier.nativeModifier(appearance, shape)),
                shape = shape,
                containerColor = container,
                contentColor = contentColor,
                tonalElevation = appearance.tonalElevation?.value?.dp ?: BottomSheetDefaults.Elevation,
            ) { sheetContent() }
        } else {
            val state = rememberBottomSheetState(initialValue = SheetValue.Expanded)
            BottomSheet(
                state = state,
                onDismissRequest = props.onDismiss,
                modifier = modifier.toComposeModifier().then(Modifier.nativeModifier(appearance, shape)),
                shape = shape,
                containerColor = container,
                contentColor = contentColor,
                tonalElevation = appearance.tonalElevation?.value?.dp ?: BottomSheetDefaults.Elevation,
                shadowElevation = appearance.shadowElevation?.value?.dp ?: 0.dp,
            ) { sheetContent() }
        }
    }
}
