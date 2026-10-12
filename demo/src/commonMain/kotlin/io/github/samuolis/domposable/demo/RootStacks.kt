package io.github.samuolis.domposable.demo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.screens.DomposableEntryLifecycle
import io.github.samuolis.domposable.screens.DomposableEntryScope

private const val OVERLAY_SNAPSHOT_INDEX = 3

internal val recipeRoots = listOf("Inbox", "Archive")

internal data class RootRoute(
    val root: String,
    val detail: String = "",
    val overlay: Boolean = false,
)

internal data class RootEntrySnapshot(
    val route: RootRoute,
    val id: String,
    val state: Map<String, List<Any?>>,
)

/** Application policy: inactive roots keep snapshots and dispose their live resources. */
internal class RootStacks(
    selected: String = recipeRoots.first(),
    restored: Map<String, List<RootEntrySnapshot>> = emptyMap(),
) {
    var selected by mutableStateOf(selected)
        private set
    private val saved = restored.toMutableMap()
    val active = mutableStateListOf<DomposableEntryScope<RootRoute>>()

    init {
        require(selected in recipeRoots)
        require(restored.keys.all { it in recipeRoots })
        active.addAll(recreate(selected))
    }

    fun select(root: String) {
        require(root in recipeRoots)
        if (root == selected) return
        saved[selected] = captureActive()
        active.forEach { it.moveTo(DomposableEntryLifecycle.Destroyed) }
        selected = root
        active.clear()
        active.addAll(recreate(root))
    }

    fun open(detail: String) {
        require(detail.isNotBlank())
        if (active.last().route.detail == detail) return
        active.add(DomposableEntryScope(RootRoute(selected, detail)))
    }

    fun overlay() {
        if (!active.last().route.overlay) active.add(DomposableEntryScope(RootRoute(selected, "confirmation", true)))
    }

    fun back() {
        if (active.size > 1) active.removeAt(active.lastIndex)
    }

    fun snapshot(): Map<String, List<RootEntrySnapshot>> = saved + (selected to captureActive())

    private fun captureActive() =
        active.map { RootEntrySnapshot(it.route, it.id, it.saveableStateRegistry.performSave()) }

    private fun recreate(root: String): List<DomposableEntryScope<RootRoute>> =
        saved[root]?.map { DomposableEntryScope(it.route, it.id, it.state) }
            ?: listOf(DomposableEntryScope(RootRoute(root)))

    companion object {
        val saver =
            Saver<RootStacks, List<Any>>(
                save = { stacks ->
                    listOf(
                        stacks.selected,
                        stacks.snapshot().mapValues { (_, entries) ->
                            entries.map { listOf(it.route.detail, it.id, it.state, it.route.overlay) }
                        },
                    )
                },
                restore = { restoreRoots(it) },
            )
    }
}

// The saver contains only route strings, entry IDs and portable UI saveables; never a live model.
@Suppress("UNCHECKED_CAST")
private fun restoreRoots(value: List<Any>): RootStacks {
    val saved = value[1] as Map<String, List<List<Any>>>
    return RootStacks(
        value[0] as String,
        saved.mapValues { (root, entries) ->
            entries.map {
                RootEntrySnapshot(
                    RootRoute(root, it[0] as String, it.getOrNull(OVERLAY_SNAPSHOT_INDEX) as? Boolean ?: false),
                    it[1] as String,
                    it[2] as Map<String, List<Any?>>,
                )
            }
        },
    )
}
