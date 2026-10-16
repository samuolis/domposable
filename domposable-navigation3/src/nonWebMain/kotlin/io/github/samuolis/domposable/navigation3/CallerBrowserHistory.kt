package io.github.samuolis.domposable.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect

@Composable
@Suppress("UNUSED_PARAMETER")
actual fun <S : Any> DomposableBrowserHistory(
    state: DomposableBrowserHistoryState<S>,
    codec: DomposableBrowserHistoryCodec<S>,
    onBeforeRestore: () -> Unit,
    onRestore: (DomposableBrowserHistoryRecord<S>?) -> Unit,
) {
    SideEffect { state.takePending() }
}
