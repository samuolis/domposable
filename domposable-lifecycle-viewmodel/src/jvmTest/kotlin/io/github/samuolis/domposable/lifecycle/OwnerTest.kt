@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.samuolis.domposable.lifecycle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.savedstate.read
import androidx.savedstate.savedState
import io.github.samuolis.domposable.screens.DomposableEntryLifecycle
import io.github.samuolis.domposable.screens.DomposableEntryScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.reflect.KClass
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class OwnerTest {
    @BeforeTest fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest fun tearDown() {
        Dispatchers.resetMain()
    }

    class Model(
        val saved: SavedStateHandle,
    ) : ViewModel() {
        var cleared = 0

        override fun onCleared() {
            cleared++
        }
    }

    private val factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: KClass<T>,
                extras: CreationExtras,
            ): T = Model(extras.createSavedStateHandle()) as T
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

    @Test
    fun primitiveArraysAndNestedSavedStateSurviveEntryRecreation() {
        val entry = DomposableEntryScope("detail")
        val model = ViewModelProvider.create(DomposableViewModelOwner(entry), factory)[Model::class]
        model.saved["ints"] = intArrayOf(1, 2)
        model.saved["longs"] = longArrayOf(3L, 4L)
        model.saved["floats"] = floatArrayOf(0.5f, 1.5f)
        model.saved["doubles"] = doubleArrayOf(0.25, 0.75)
        model.saved["booleans"] = booleanArrayOf(true, false)
        model.saved["nested"] = savedState(mapOf("note" to "nested"))
        val saved = entry.saveableStateRegistry.performSave()
        entry.moveTo(DomposableEntryLifecycle.Destroyed)
        val restoredEntry = DomposableEntryScope("detail", entry.id, saved)
        val restored = ViewModelProvider.create(DomposableViewModelOwner(restoredEntry), factory)[Model::class]
        assertContentEquals(intArrayOf(1, 2), restored.saved.get<IntArray>("ints"))
        assertContentEquals(longArrayOf(3L, 4L), restored.saved.get<LongArray>("longs"))
        assertContentEquals(floatArrayOf(0.5f, 1.5f), restored.saved.get<FloatArray>("floats"))
        assertContentEquals(doubleArrayOf(0.25, 0.75), restored.saved.get<DoubleArray>("doubles"))
        assertContentEquals(booleanArrayOf(true, false), restored.saved.get<BooleanArray>("booleans"))
        assertEquals("nested", restored.saved.get<androidx.savedstate.SavedState>("nested")?.read { getString("note") })
        restoredEntry.moveTo(DomposableEntryLifecycle.Destroyed)
    }
}
