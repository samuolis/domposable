@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package io.github.samuolis.domposable.lifecycle

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.samuolis.domposable.screens.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import kotlin.reflect.KClass
import kotlin.test.*

class OwnerTest {
    @BeforeTest fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterTest fun tearDown() { Dispatchers.resetMain() }
    class Model(val saved: SavedStateHandle) : ViewModel() {
        var cleared = 0
        override fun onCleared() { cleared++ }
    }
    private val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T = Model(extras.createSavedStateHandle()) as T
    }
    @Test fun applicationFactoryRestoresSavedStateIntoANewModelAndClearsTheOldOne() {
        val entry = DomposableEntryScope("detail")
        val owner = DomposableViewModelOwner(entry)
        val provider = ViewModelProvider.create(owner, factory)
        val model = provider[Model::class]
        model.saved["count"] = 7
        entry.moveTo(DomposableEntryLifecycle.Resumed)
        entry.moveTo(DomposableEntryLifecycle.Created)
        assertSame(model, provider[Model::class])
        val saved = entry.saveableStateRegistry.performSave()
        entry.moveTo(DomposableEntryLifecycle.Destroyed)
        assertEquals(1, model.cleared)
        val restoredEntry = DomposableEntryScope("detail", entry.id, saved)
        val restoredOwner = DomposableViewModelOwner(restoredEntry)
        val restoredModel = ViewModelProvider.create(restoredOwner, factory)[Model::class]
        assertNotSame(model, restoredModel)
        assertEquals(7, restoredModel.saved.get<Int>("count"))
        restoredEntry.moveTo(DomposableEntryLifecycle.Destroyed)
        assertEquals(1, restoredModel.cleared)
    }
}
