package io.github.samuolis.domposable.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotMutableState
import androidx.compose.runtime.structuralEqualityPolicy
import kotlin.random.Random
import kotlin.reflect.KClass

private const val ENTRY_ID_RADIX = 16

enum class DomposableEntryLifecycle { Created, Resumed, Destroyed }

/** Route identity, saveable UI state and resource disposal; no model or DI requirements. */
class DomposableEntryScope<R : Any>(
    val route: R,
    val id: String = Random.nextLong().toString(ENTRY_ID_RADIX),
    restoredState: Map<String, List<Any?>> = emptyMap(),
) {
    var lifecycle: DomposableEntryLifecycle by mutableStateOf(DomposableEntryLifecycle.Created)
        private set
    val saveableStateRegistry: SaveableStateRegistry = EntrySaveableRegistry(restoredState)
    private val resources = mutableMapOf<String, Any>()
    private val disposers = mutableListOf<() -> Unit>()
    private val observers = mutableListOf<(DomposableEntryLifecycle) -> Unit>()

    fun onDispose(callback: () -> Unit) {
        check(lifecycle != DomposableEntryLifecycle.Destroyed)
        disposers += callback
    }

    fun observeLifecycle(callback: (DomposableEntryLifecycle) -> Unit): () -> Unit {
        observers += callback
        callback(lifecycle)
        return { observers -= callback }
    }

    /** Used by optional adapters. Applications may build their own ownership helpers. */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> retain(
        key: String,
        dispose: (T) -> Unit = {},
        factory: () -> T,
    ): T {
        check(lifecycle != DomposableEntryLifecycle.Destroyed)
        return resources.getOrPut(key) { factory().also { resource -> onDispose { dispose(resource) } } } as T
    }

    fun moveTo(state: DomposableEntryLifecycle) {
        if (lifecycle == state) return
        check(lifecycle != DomposableEntryLifecycle.Destroyed) {
            "Disposed entries cannot be resumed; recreate them from saved state"
        }
        lifecycle = state
        observers.toList().forEach { it(state) }
        if (state == DomposableEntryLifecycle.Destroyed) {
            val callbacks = disposers.toList()
            disposers.clear()
            resources.clear()
            observers.clear()
            callbacks.forEach { it() }
        }
    }
}

/** Capture providers before their compositions leave, so history can restore disposed screens. */
private class EntrySaveableRegistry(
    restored: Map<String, List<Any?>>,
) : SaveableStateRegistry {
    private class Slot(
        var saved: Any?,
        var provider: (() -> Any?)? = null,
        var claimed: Boolean = false,
    )

    private val slots = restored.mapValues { (_, values) -> values.map { Slot(it) }.toMutableList() }.toMutableMap()
    private val consumed = mutableMapOf<String, MutableList<Slot>>()

    override fun canBeSaved(value: Any): Boolean = portableSaveable(value)

    override fun consumeRestored(key: String): Any? {
        val slot = slots[key]?.firstOrNull { it.provider == null && !it.claimed } ?: return null
        slot.claimed = true
        consumed.getOrPut(key) { mutableListOf() }.add(slot)
        return slot.saved
    }

    override fun registerProvider(
        key: String,
        valueProvider: () -> Any?,
    ): SaveableStateRegistry.Entry {
        require(key.isNotBlank())
        val pending = consumed[key]
        val slot =
            if (!pending.isNullOrEmpty()) {
                pending.removeAt(0)
            } else {
                Slot(null).also {
                    slots.getOrPut(key) { mutableListOf() }.add(it)
                }
            }
        slot.provider = valueProvider
        slot.claimed = false
        var registered = true
        return object : SaveableStateRegistry.Entry {
            override fun unregister() {
                if (!registered) return
                registered = false
                slot.saved = valueProvider()
                slot.provider = null
            }
        }
    }

    override fun performSave(): Map<String, List<Any?>> =
        slots.mapValues { (_, values) ->
            values.map { slot ->
                val value = if (slot.provider == null) slot.saved else slot.provider!!.invoke()
                require(value == null || canBeSaved(value)) { "Use a portable Saver for ${value?.let { it::class }}" }
                value
            }
        }
}

/** Browser-restorable saveables: scalar values and recursively composed lists/maps. */
fun portableSaveable(value: Any): Boolean =
    when (value) {
        is SnapshotMutableState<*> -> {
            value.policy in
                listOf(structuralEqualityPolicy<Any?>(), referentialEqualityPolicy<Any?>(), neverEqualPolicy<Any?>()) &&
                value.value.let { it == null || portableSaveable(it) }
        }

        is String, is Boolean, is Int, is Long, is Float, is Double, is Short, is Byte -> {
            true
        }

        is List<*> -> {
            value.all { it == null || portableSaveable(it) }
        }

        is Map<*, *> -> {
            value.all { (key, item) -> key is String && (item == null || portableSaveable(item)) }
        }

        else -> {
            false
        }
    }

class DomposableDestination<R : Any>(
    val routeType: KClass<R>,
    val render: @Composable DomposableEntryScope<R>.() -> Unit,
)

class DomposableScreenRegistry internal constructor(
    private val destinations: Map<KClass<*>, DomposableDestination<*>>,
) {
    @Composable
    @Suppress("UNCHECKED_CAST")
    fun <R : Any> Render(entry: DomposableEntryScope<R>) {
        val destination =
            destinations[entry.route::class] as? DomposableDestination<R>
                ?: error(
                    "No screen registered for ${entry.route::class}; register a destination in " +
                        "domposableScreenRegistry",
                )
        key(entry.id) {
            CompositionLocalProvider(LocalSaveableStateRegistry provides entry.saveableStateRegistry) {
                DisposableEffect(entry) {
                    entry.moveTo(DomposableEntryLifecycle.Resumed)
                    onDispose {
                        if (entry.lifecycle !=
                            DomposableEntryLifecycle.Destroyed
                        ) {
                            entry.moveTo(DomposableEntryLifecycle.Created)
                        }
                    }
                }
                destination.render(entry)
            }
        }
    }
}

class DomposableScreenRegistryBuilder {
    private val destinations = mutableMapOf<KClass<*>, DomposableDestination<*>>()

    fun <R : Any> register(destination: DomposableDestination<R>) {
        require(destination.routeType !in destinations) { "Duplicate destination for ${destination.routeType}" }
        destinations[destination.routeType] = destination
    }

    fun <R : Any> register(
        route: KClass<R>,
        render: @Composable DomposableEntryScope<R>.() -> Unit,
    ) = register(DomposableDestination(route, render))

    internal fun build(): DomposableScreenRegistry = DomposableScreenRegistry(destinations.toMap())
}

fun domposableScreenRegistry(build: DomposableScreenRegistryBuilder.() -> Unit): DomposableScreenRegistry =
    DomposableScreenRegistryBuilder().apply(build).build()
