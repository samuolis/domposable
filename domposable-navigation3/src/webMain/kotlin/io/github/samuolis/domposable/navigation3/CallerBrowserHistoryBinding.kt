package io.github.samuolis.domposable.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.browser.window
import org.w3c.dom.events.Event

internal class CallerBrowserHistoryBinding<S : Any>(
    val state: DomposableBrowserHistoryState<S>,
    var codec: DomposableBrowserHistoryCodec<S>,
) {
    fun initialize(onRestore: (DomposableBrowserHistoryRecord<S>?) -> Unit) {
        val snapshot = browserHistoryState()
        val record = decode(snapshot)
        if (record != null) {
            state.restore(record, clearPending = false)
            onRestore(record)
        } else if (browserHistoryStateIsNull()) {
            // Seed only an empty entry; keep the current URL (including auth query parameters).
            replaceBrowserHistoryState(codec.encode(state.position, state.current), null)
        }
    }

    fun flush() {
        val pending =
            state.pendingIntents.map { intent ->
                val url = intent.url ?: codec.url(intent.value)
                validateHistoryUrl(url)
                intent to (codec.encode(intent.position, intent.value) to url)
            }
        state.takePending()
        pending.forEach { (intent, encoded) ->
            if (intent.replace) {
                replaceBrowserHistoryState(encoded.first, encoded.second)
            } else {
                pushBrowserHistoryState(encoded.first, encoded.second)
            }
        }
    }

    fun decode(snapshot: String?): DomposableBrowserHistoryRecord<S>? = decodeHistoryRecord(snapshot, codec)

    fun pop(
        onBeforeRestore: () -> Unit,
        onRestore: (DomposableBrowserHistoryRecord<S>?) -> Unit,
    ) {
        val record = decode(browserHistoryState())
        restoreBrowserHistoryRecord(state, record, onBeforeRestore, onRestore)
    }
}

@Composable
actual fun <S : Any> DomposableBrowserHistory(
    state: DomposableBrowserHistoryState<S>,
    codec: DomposableBrowserHistoryCodec<S>,
    onBeforeRestore: () -> Unit,
    onRestore: (DomposableBrowserHistoryRecord<S>?) -> Unit,
) {
    val binding = remember(state, codec) { CallerBrowserHistoryBinding(state, codec) }
    binding.codec = codec
    val currentOnRestore = rememberUpdatedState(onRestore)
    val currentOnBeforeRestore = rememberUpdatedState(onBeforeRestore)
    val pendingCount = state.pendingCount
    DisposableEffect(binding) {
        binding.initialize { currentOnRestore.value(it) }
        val pop: (Event) -> Unit = {
            binding.pop(currentOnBeforeRestore.value) { currentOnRestore.value(it) }
        }
        window.addEventListener("popstate", pop)
        onDispose {
            window.removeEventListener("popstate", pop)
        }
    }
    SideEffect { if (pendingCount > 0) binding.flush() }
}
