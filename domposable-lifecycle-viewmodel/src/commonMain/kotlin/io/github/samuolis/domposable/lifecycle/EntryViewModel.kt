package io.github.samuolis.domposable.lifecycle

import androidx.compose.runtime.*
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.savedstate.*
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import io.github.samuolis.domposable.screens.*

/** Optional AndroidX owner. Factories and dependency injection remain application-owned. */
class DomposableViewModelOwner internal constructor(entry: DomposableEntryScope<*>) :
    ViewModelStoreOwner, SavedStateRegistryOwner, HasDefaultViewModelProviderFactory {
    override val viewModelStore = ViewModelStore()
    private val registry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = registry
    private val controller = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = controller.savedStateRegistry
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: kotlin.reflect.KClass<T>, extras: CreationExtras): T =
            error("Supply an application-owned ViewModel factory; Domposable does not infer DI")
    }
    override val defaultViewModelCreationExtras: CreationExtras = MutableCreationExtras().apply {
        this[SAVED_STATE_REGISTRY_OWNER_KEY] = this@DomposableViewModelOwner
        this[VIEW_MODEL_STORE_OWNER_KEY] = this@DomposableViewModelOwner
    }

    init {
        controller.performAttach()
        val restored = entry.saveableStateRegistry.consumeRestored("domposable.androidx.savedstate") as? Map<*, *>
        controller.performRestore(restored?.let { restoreSavedState(it) })
        enableSavedStateHandles()
        registry.currentState = Lifecycle.State.CREATED
        val registration = entry.saveableStateRegistry.registerProvider("domposable.androidx.savedstate") {
            savedState().also(controller::performSave).portableState()
        }
        val stopObserving = entry.observeLifecycle { state -> registry.currentState = when (state) {
            DomposableEntryLifecycle.Created -> Lifecycle.State.CREATED
            DomposableEntryLifecycle.Resumed -> Lifecycle.State.RESUMED
            DomposableEntryLifecycle.Destroyed -> Lifecycle.State.DESTROYED
        } }
        entry.onDispose { registration.unregister(); stopObserving(); viewModelStore.clear() }
    }
}

private fun SavedState.portableState(): Map<String, Any?> = read { toMap() }.mapValues { (_, value) -> when (value) {
    is SavedState -> mapOf("savedState" to value.portableState())
    is IntArray -> mapOf("intArray" to value.toList())
    is LongArray -> mapOf("longArray" to value.toList())
    is FloatArray -> mapOf("floatArray" to value.toList())
    is DoubleArray -> mapOf("doubleArray" to value.toList())
    is BooleanArray -> mapOf("booleanArray" to value.toList())
    else -> value
} }

private fun restoreSavedState(values: Map<*, *>): SavedState = savedState(values.entries.associate { (key, value) ->
    key as String to when {
        value is Map<*, *> && "savedState" in value -> restoreSavedState(value["savedState"] as Map<*, *>)
        value is Map<*, *> && "intArray" in value -> (value["intArray"] as List<*>).map { (it as Number).toInt() }.toIntArray()
        value is Map<*, *> && "longArray" in value -> (value["longArray"] as List<*>).map { (it as Number).toLong() }.toLongArray()
        value is Map<*, *> && "floatArray" in value -> (value["floatArray"] as List<*>).map { (it as Number).toFloat() }.toFloatArray()
        value is Map<*, *> && "doubleArray" in value -> (value["doubleArray"] as List<*>).map { (it as Number).toDouble() }.toDoubleArray()
        value is Map<*, *> && "booleanArray" in value -> (value["booleanArray"] as List<*>).map { it as Boolean }.toBooleanArray()
        else -> value
    }
})

@Composable
fun DomposableViewModelEntry(entry: DomposableEntryScope<*>, content: @Composable () -> Unit) {
    val owner = remember(entry) { entry.retain("domposable.androidx.owner") { DomposableViewModelOwner(entry) } }
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides owner,
        LocalLifecycleOwner provides owner,
        LocalSavedStateRegistryOwner provides owner,
        content = content,
    )
}
