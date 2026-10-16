@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Scrim
import androidx.compose.material3.ScrimDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.ui.DomposableBadgedBoxProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableScrimProps
import io.github.samuolis.domposable.ui.DomposableSnackbarDuration
import io.github.samuolis.domposable.ui.DomposableSnackbarHostProps
import io.github.samuolis.domposable.ui.DomposableSnackbarResult
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.toComposeModifier

@Composable
internal fun NativeBadgedBox(
    props: DomposableBadgedBoxProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    BadgedBox(badge = { props.badge() }, modifier = modifier.toComposeModifier()) { content() }
}

@Composable
internal fun NativeScrim(
    props: DomposableScrimProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.toComposeModifier()) {
        content()
        Scrim(
            contentDescription = props.label,
            modifier = Modifier.matchParentSize(),
            onClick = props.onClick,
            alpha = { props.alpha },
            color = props.color?.nativeColor() ?: ScrimDefaults.color,
        )
    }
}

@Composable
internal fun NativeSnackbarHost(
    props: DomposableSnackbarHostProps,
    modifier: DomposableModifier,
) {
    val state = remember { SnackbarHostState() }
    val data = props.state.currentSnackbarData
    LaunchedEffect(data) {
        if (data != null) {
            val result =
                state.showSnackbar(
                    message = data.message,
                    actionLabel = data.actionLabel,
                    withDismissAction = data.withDismissAction,
                    duration =
                        when (data.duration) {
                            DomposableSnackbarDuration.Short -> SnackbarDuration.Short
                            DomposableSnackbarDuration.Long -> SnackbarDuration.Long
                            DomposableSnackbarDuration.Indefinite -> SnackbarDuration.Indefinite
                        },
                )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                data.performAction()
            } else {
                data.dismiss()
            }
        }
    }
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.snackbar
    SnackbarHost(
        hostState = state,
        modifier = modifier.toComposeModifier(),
        snackbar = { snackbarData ->
            val shape = appearance.nativeShape(SnackbarDefaults.shape)
            Snackbar(
                snackbarData = snackbarData,
                modifier = Modifier.then(Modifier.nativeModifier(appearance, shape)),
                shape = shape,
                containerColor = appearance.container?.nativeColor() ?: SnackbarDefaults.color,
                contentColor = appearance.content?.nativeColor() ?: SnackbarDefaults.contentColor,
                actionColor = appearance.content?.nativeColor() ?: SnackbarDefaults.actionColor,
                actionContentColor = appearance.content?.nativeColor() ?: SnackbarDefaults.actionContentColor,
                dismissActionContentColor =
                    appearance.content?.nativeColor() ?: SnackbarDefaults.dismissActionContentColor,
            )
        },
    )
}
