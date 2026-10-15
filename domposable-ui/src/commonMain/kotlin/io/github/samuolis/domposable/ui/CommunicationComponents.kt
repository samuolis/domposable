package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableComponent
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

enum class DomposableSnackbarDuration { Short, Long, Indefinite }

enum class DomposableSnackbarResult { ActionPerformed, Dismissed }

/** One request owned by [DomposableSnackbarHostState]; completion is idempotent. */
@Stable
class DomposableSnackbarData internal constructor(
    val message: String,
    val actionLabel: String?,
    val withDismissAction: Boolean,
    val duration: DomposableSnackbarDuration,
    private val complete: (DomposableSnackbarResult) -> Unit,
) {
    fun performAction() {
        if (actionLabel != null) complete(DomposableSnackbarResult.ActionPerformed)
    }

    fun dismiss() {
        complete(DomposableSnackbarResult.Dismissed)
    }
}

/** Serializes requests; cancelling a caller removes its active or queued snackbar. */
@Stable
class DomposableSnackbarHostState {
    private val mutex = Mutex()
    var currentSnackbarData: DomposableSnackbarData? by mutableStateOf(null)
        private set

    suspend fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: DomposableSnackbarDuration =
            if (actionLabel ==
                null
            ) {
                DomposableSnackbarDuration.Short
            } else {
                DomposableSnackbarDuration.Indefinite
            },
    ): DomposableSnackbarResult {
        require(message.isNotBlank()) { "A snackbar needs a message" }
        require(actionLabel == null || actionLabel.isNotBlank()) { "A snackbar action needs a label" }
        return mutex.withLock {
            try {
                suspendCancellableCoroutine { continuation ->
                    currentSnackbarData =
                        DomposableSnackbarData(message, actionLabel, withDismissAction, duration) { result ->
                            if (continuation.isActive) continuation.resume(result)
                        }
                }
            } finally {
                currentSnackbarData = null
            }
        }
    }
}

@Composable
fun rememberDomposableSnackbarHostState(): DomposableSnackbarHostState = remember { DomposableSnackbarHostState() }

@DomposableComponent("SnackbarHost")
data class DomposableSnackbarHostProps(
    val state: DomposableSnackbarHostState,
    val appearance: DomposableSurfaceAppearance? = null,
)

@Composable
fun DomposableSnackbarHost(
    state: DomposableSnackbarHostState,
    modifier: DomposableModifier = DomposableModifier,
    appearance: DomposableSurfaceAppearance? = null,
) = DomposableSnackbarHost(DomposableSnackbarHostProps(state, appearance), modifier)

@DomposableComponent("BadgedBox")
data class DomposableBadgedBoxProps(
    val badge: @Composable () -> Unit,
)

@Composable
fun DomposableBadgedBox(
    badge: @Composable () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable () -> Unit,
) = DomposableBadgedBox(DomposableBadgedBoxProps(badge), modifier, content)

/** A backdrop in the caller's overlay container; a dismissible scrim needs an accessible label. */
@DomposableComponent("Scrim")
data class DomposableScrimProps(
    val onClick: (() -> Unit)? = null,
    val label: String? = null,
    val alpha: Float = 1f,
    val color: io.github.samuolis.domposable.foundation.DomposableColor? = null,
) {
    init {
        require(alpha.isFinite() && alpha in 0f..1f)
        require(onClick == null || !label.isNullOrBlank()) { "A dismissible scrim needs an accessible label" }
    }
}

@Composable
fun DomposableScrim(
    modifier: DomposableModifier = DomposableModifier,
    onClick: (() -> Unit)? = null,
    label: String? = null,
    alpha: Float = 1f,
    color: io.github.samuolis.domposable.foundation.DomposableColor? = null,
) = DomposableScrim(DomposableScrimProps(onClick, label, alpha, color), modifier)
