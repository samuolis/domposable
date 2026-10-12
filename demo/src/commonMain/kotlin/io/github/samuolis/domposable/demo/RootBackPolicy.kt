package io.github.samuolis.domposable.demo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random

/** An application/DI scope owns these plain models, separately from entry ownership. */
internal class RootModels {
    private val models = mutableMapOf<RootRoute, RootLiveModel>()
    private var closed = false

    fun get(route: RootRoute): RootLiveModel {
        check(!closed)
        require(!route.overlay)
        return models.getOrPut(route) { RootLiveModel() }
    }

    fun close() {
        models.values.forEach { it.close() }
        models.clear()
        closed = true
    }
}

internal class RootLiveModel {
    val instance = Random.nextLong().toString(16)
    var note by mutableStateOf("")
        private set
    var dirty by mutableStateOf(false)
        private set
    var closed = false
        private set

    fun edit(value: String) {
        note = value
        dirty = true
    }

    fun acceptLeaving() {
        dirty = false
    }

    fun close() {
        closed = true
    }
}

internal data class BackPreview(
    val entryId: String,
    val root: String,
    val progress: Float,
)

/** Progress never mutates the stack; commit is valid only for the entry that began the preview. */
internal class RootBackPolicy(
    private val stacks: RootStacks,
    private val models: RootModels,
) {
    var confirmDirty by mutableStateOf(true)
    var preview by mutableStateOf<BackPreview?>(null)
        private set

    fun progress(value: Float) {
        require(value.isFinite())
        if (stacks.active.size == 1 ||
            stacks.active
                .last()
                .route.overlay
        ) {
            return
        }
        val current = preview ?: BackPreview(stacks.active.last().id, stacks.selected, 0f)
        preview = current.copy(progress = value.coerceIn(0f, 1f))
    }

    fun cancel() {
        preview = null
    }

    fun commit() {
        val pending = preview ?: return
        preview = null
        if (pending.root == stacks.selected && pending.entryId == stacks.active.last().id) request()
    }

    fun request() {
        cancel()
        val route = stacks.active.last().route
        if (route.overlay) {
            stacks.back()
        } else if (stacks.active.size > 1 && confirmDirty && models.get(route).dirty) {
            stacks.overlay()
        } else {
            stacks.back()
        }
    }

    fun confirm(entryId: String = stacks.active.last().id) {
        if (stacks.active.last().id != entryId) return
        if (!stacks.active
                .last()
                .route.overlay
        ) {
            return
        }
        stacks.back()
        models.get(stacks.active.last().route).acceptLeaving()
        stacks.back()
        cancel()
    }
}
