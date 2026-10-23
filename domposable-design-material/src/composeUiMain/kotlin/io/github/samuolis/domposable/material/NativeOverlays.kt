package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.samuolis.domposable.ui.DomposableBottomSheetProps
import io.github.samuolis.domposable.ui.DomposableDialogProps
import io.github.samuolis.domposable.ui.DomposableDialogVariant
import io.github.samuolis.domposable.ui.DomposableModalLayout
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
        val title = @Composable {
            if (props.headerContent == null) {
                Text(
                    props.title,
                    style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.headlineSmall),
                )
            } else {
                CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) {
                    props.headerContent?.invoke(props.onDismiss)
                }
            }
        }
        val close = @Composable {
            if (props.headerContent == null) TextButton(props.onDismiss) { Text("Close") }
        }
        when (props.variant) {
            DomposableDialogVariant.Alert -> {
                AlertDialog(
                    onDismissRequest = props.onDismiss,
                    properties = dialogProperties(props),
                    confirmButton = confirm,
                    dismissButton = close,
                    title = title,
                    text = body,
                    modifier =
                        modifier.toComposeModifier().then(
                            Modifier
                                .nativeModifier(
                                    appearance,
                                    AlertDialogDefaults.shape,
                                ).modalLayout(props.layout),
                        ),
                    shape = appearance.nativeShape(AlertDialogDefaults.shape),
                    containerColor = appearance.container?.nativeColor() ?: AlertDialogDefaults.containerColor,
                )
            }

            DomposableDialogVariant.DatePicker -> {
                DatePickerDialog(
                    onDismissRequest = props.onDismiss,
                    properties = dialogProperties(props),
                    confirmButton = confirm,
                    dismissButton = close,
                    modifier = modifier.toComposeModifier().modalLayout(props.layout),
                    shape = appearance.nativeShape(DatePickerDefaults.shape),
                    tonalElevation = appearance.tonalElevation?.value?.dp ?: DatePickerDefaults.TonalElevation,
                    colors =
                        DatePickerDefaults.colors(
                            containerColor =
                                appearance.container?.nativeColor() ?: androidx.compose.ui.graphics.Color.Unspecified,
                        ),
                ) {
                    title()
                    body()
                }
            }

            DomposableDialogVariant.TimePicker -> {
                TimePickerDialog(
                    onDismissRequest = props.onDismiss,
                    properties = dialogProperties(props),
                    confirmButton = confirm,
                    dismissButton = close,
                    title = title,
                    modifier = modifier.toComposeModifier().modalLayout(props.layout),
                    shape = appearance.nativeShape(TimePickerDialogDefaults.shape),
                    containerColor = appearance.container?.nativeColor() ?: TimePickerDialogDefaults.containerColor,
                ) { body() }
            }

            DomposableDialogVariant.Basic, DomposableDialogVariant.FullScreen -> {
                BasicAlertDialog(
                    onDismissRequest = props.onDismiss,
                    modifier =
                        modifier.toComposeModifier().modalLayout(props.layout).then(
                            if (props.variant ==
                                DomposableDialogVariant.FullScreen
                            ) {
                                Modifier.fillMaxSize()
                            } else {
                                Modifier
                            },
                        ),
                    properties =
                        dialogProperties(
                            props,
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
                        Column(
                            modifier = Modifier.padding(layoutPadding(props.layout)),
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    props.layout.sectionSpacing
                                        ?.value
                                        ?.dp ?: 0.dp,
                                ),
                        ) {
                            title()
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

private fun dialogProperties(
    props: DomposableDialogProps,
    usePlatformDefaultWidth: Boolean = true,
) = DialogProperties(
    usePlatformDefaultWidth = usePlatformDefaultWidth && !props.layout.fillViewportWidth,
    dismissOnBackPress = props.dismissOnBackPress,
    dismissOnClickOutside = props.dismissOnClickOutside,
)

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
            Column(
                modifier = Modifier.padding(layoutPadding(props.layout)),
                verticalArrangement =
                    Arrangement.spacedBy(
                        props.layout.sectionSpacing
                            ?.value
                            ?.dp ?: 0.dp,
                    ),
            ) {
                if (props.headerContent == null) {
                    Text(
                        props.title,
                        style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.titleLarge),
                    )
                } else {
                    CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) {
                        props.headerContent?.invoke(props.onDismiss)
                    }
                }
                CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) { content() }
                if (props.headerContent == null) TextButton(props.onDismiss) { Text("Close") }
            }
        }
        if (props.modal) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = props.skipPartiallyExpanded)
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = props.onDismiss,
                sheetGesturesEnabled = props.layout.swipeToDismiss,
                modifier =
                    modifier
                        .toComposeModifier()
                        .then(Modifier.nativeModifier(appearance, shape))
                        .modalLayout(props.layout),
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
                gesturesEnabled = props.layout.swipeToDismiss,
                modifier =
                    modifier
                        .toComposeModifier()
                        .then(Modifier.nativeModifier(appearance, shape))
                        .modalLayout(props.layout),
                shape = shape,
                containerColor = container,
                contentColor = contentColor,
                tonalElevation = appearance.tonalElevation?.value?.dp ?: BottomSheetDefaults.Elevation,
                shadowElevation = appearance.shadowElevation?.value?.dp ?: 0.dp,
            ) { sheetContent() }
        }
    }
}

@Composable
private fun Modifier.modalLayout(layout: DomposableModalLayout): Modifier {
    val density = LocalDensity.current
    val windowSize = LocalWindowInfo.current.containerSize
    val viewportWidth = with(density) { windowSize.width.toDp() }
    val viewportHeight = with(density) { windowSize.height.toDp() }
    val inset = layout.viewportInset.value.dp
    var result: Modifier = this
    layout.maxWidth?.let { result = result.widthIn(max = it.value.dp) }
    if (layout.fillViewportWidth) {
        result = result.width((viewportWidth - inset * 2).coerceAtLeast(0.dp))
    }
    layout.heightFraction?.let {
        result = result.height((viewportHeight * it - inset * 2).coerceAtLeast(0.dp))
    }
    val viewportMax =
        layout.maxViewportFraction?.let {
            minOf(viewportHeight * it, viewportHeight - inset * 2).coerceAtLeast(0.dp)
        }
    val explicitMax = layout.maxHeight?.value?.dp
    val maximum =
        when {
            viewportMax != null && explicitMax != null -> minOf(viewportMax, explicitMax)
            viewportMax != null -> viewportMax
            else -> explicitMax
        }
    maximum?.let { result = result.heightIn(max = it) }
    return result
}

private fun layoutPadding(layout: DomposableModalLayout): androidx.compose.foundation.layout.PaddingValues {
    val insets =
        layout.padding ?: return androidx.compose.foundation.layout
            .PaddingValues(0.dp)
    return androidx.compose.foundation.layout.PaddingValues(
        start = insets.start.value.dp,
        top = insets.top.value.dp,
        end = insets.end.value.dp,
        bottom = insets.bottom.value.dp,
    )
}
