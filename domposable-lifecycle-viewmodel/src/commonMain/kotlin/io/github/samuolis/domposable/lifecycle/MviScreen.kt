package io.github.samuolis.domposable.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

/** Optional state/action contract. Models need no Domposable or ViewModel base class. */
interface DomposableMvi<State, Action> {
    val state: StateFlow<State>
    fun dispatch(action: Action)
}

/** Collects a model's state while its entry is active and forwards actions to that model. */
@Composable
fun <State, Action> DomposableMviScreen(
    model: DomposableMvi<State, Action>,
    content: @Composable (State, (Action) -> Unit) -> Unit,
) {
    val state by model.state.collectAsStateWithLifecycle()
    content(state, model::dispatch)
}
