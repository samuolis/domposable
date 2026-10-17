package io.github.samuolis.domposable.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class DomposableBrowserHistoryRecord<S : Any>(
    val position: Int,
    val value: S,
)

interface DomposableBrowserHistoryCodec<S : Any> {
    fun encode(
        position: Int,
        value: S,
    ): String

    fun decode(snapshot: String): DomposableBrowserHistoryRecord<S>?

    fun url(value: S): String
}

internal fun <S : Any> decodeHistoryRecord(
    snapshot: String?,
    codec: DomposableBrowserHistoryCodec<S>,
): DomposableBrowserHistoryRecord<S>? =
    snapshot
        ?.let { runCatching { codec.decode(it) }.getOrNull() }
        ?.takeIf { it.position >= 0 }

internal fun validateHistoryUrl(url: String) {
    require(
        ((url.startsWith("/") && !url.startsWith("//")) || url.startsWith("#")) &&
            '\\' !in url && url.none { it <= '\u001f' || it == '\u007f' },
    ) {
        "Browser history URLs must be root-relative paths or fragments"
    }
}

internal fun <S : Any> restoreBrowserHistoryRecord(
    state: DomposableBrowserHistoryState<S>,
    record: DomposableBrowserHistoryRecord<S>?,
    onBeforeRestore: () -> Unit,
    onRestore: (DomposableBrowserHistoryRecord<S>?) -> Unit,
) {
    onBeforeRestore()
    if (record != null) {
        state.restore(record, clearPending = true)
    } else {
        state.takePending()
    }
    onRestore(record)
}

internal class DomposableHistoryBranch<S : Any>(
    initialPosition: Int,
    initialValue: S,
) {
    private val snapshots = mutableStateMapOf(initialPosition to initialValue)
    private var positionState by mutableStateOf(initialPosition)
    private var currentState by mutableStateOf(initialValue)
    val position: Int get() = positionState
    val current: S get() = currentState

    val retainedSnapshots: List<Pair<Int, S>> get() = snapshots.entries.map { it.key to it.value }

    fun snapshotAt(position: Int): S? = snapshots[position]

    fun push(value: S): Int {
        require(positionState < Int.MAX_VALUE) { "History position overflow" }
        snapshots.keys.filter { it > positionState }.forEach(snapshots::remove)
        positionState++
        currentState = value
        snapshots[positionState] = value
        return positionState
    }

    fun replace(value: S) {
        currentState = value
        snapshots[positionState] = value
    }

    fun reset(value: S) {
        snapshots.clear()
        replace(value)
    }

    fun restore(
        position: Int,
        value: S,
    ) {
        require(position >= 0) { "History position must be non-negative" }
        positionState = position
        currentState = value
        snapshots[position] = value
    }
}

/** Browser-history mechanics for a caller-owned stack or snapshot. */
class DomposableBrowserHistoryState<S : Any>(
    initialValue: S,
    initialPosition: Int = 0,
) {
    private val branch: DomposableHistoryBranch<S>
    private val pending = mutableStateListOf<Intent<S>>()

    init {
        require(initialPosition >= 0) { "History position must be non-negative" }
        branch = DomposableHistoryBranch(initialPosition, initialValue)
    }

    val position: Int get() = branch.position
    val current: S get() = branch.current
    val retainedSnapshots: List<Pair<Int, S>> get() = branch.retainedSnapshots
    internal val pendingCount: Int get() = pending.size
    internal val pendingIntents: List<Intent<S>> get() = pending.toList()

    fun push(
        value: S,
        url: String? = null,
    ): Int {
        url?.let(::validateHistoryUrl)
        val position = branch.push(value)
        pending.add(Intent(position, value, url, replace = false))
        return position
    }

    fun replace(
        value: S,
        url: String? = null,
    ) {
        url?.let(::validateHistoryUrl)
        branch.replace(value)
        pending.add(Intent(position, value, url, replace = true))
    }

    /** Forget retained in-memory entries while replacing the current browser position. */
    fun reset(
        value: S,
        url: String? = null,
    ) {
        url?.let(::validateHistoryUrl)
        branch.reset(value)
        pending.add(Intent(position, value, url, replace = true))
    }

    fun restore(record: DomposableBrowserHistoryRecord<S>) = restore(record, clearPending = false)

    internal fun restore(
        record: DomposableBrowserHistoryRecord<S>,
        clearPending: Boolean,
    ) {
        branch.restore(record.position, record.value)
        if (clearPending) pending.clear()
    }

    internal fun takePending(): List<Intent<S>> = pending.toList().also { pending.clear() }

    internal data class Intent<S : Any>(
        val position: Int,
        val value: S,
        val url: String?,
        val replace: Boolean,
    )
}

/** The caller owns the value, URL codec, restore policy, and browser stack semantics. */
@Composable
expect fun <S : Any> DomposableBrowserHistory(
    state: DomposableBrowserHistoryState<S>,
    codec: DomposableBrowserHistoryCodec<S>,
    onBeforeRestore: () -> Unit = {},
    onRestore: (DomposableBrowserHistoryRecord<S>?) -> Unit,
)
