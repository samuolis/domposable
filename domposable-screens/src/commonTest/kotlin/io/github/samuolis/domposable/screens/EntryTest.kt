package io.github.samuolis.domposable.screens

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class EntryTest {
    @Test fun standardMutationPoliciesArePortableAndCustomPoliciesRequireASaver() {
        assertTrue(
            portableSaveable(
                androidx.compose.runtime.mutableStateOf("text", androidx.compose.runtime.neverEqualPolicy()),
            ),
        )
        assertTrue(
            portableSaveable(
                androidx.compose.runtime.mutableStateOf(1, androidx.compose.runtime.referentialEqualityPolicy()),
            ),
        )
        val custom =
            object : androidx.compose.runtime.SnapshotMutationPolicy<Int> {
                override fun equivalent(
                    a: Int,
                    b: Int,
                ): Boolean = a / 10 == b / 10
            }
        assertFalse(portableSaveable(androidx.compose.runtime.mutableStateOf(1, custom)))
    }

    @Test fun retainedResourceIsDisposedOnceAndCannotBeRevived() {
        val entry = DomposableEntryScope("route")
        var creations = 0
        var disposals = 0
        val first =
            entry.retain("model", dispose = { _: Any -> disposals++ }) {
                creations++
                Any()
            }
        assertSame(first, entry.retain("model") { Any() })
        entry.moveTo(DomposableEntryLifecycle.Resumed)
        entry.moveTo(DomposableEntryLifecycle.Created)
        assertEquals(0, disposals)
        entry.moveTo(DomposableEntryLifecycle.Destroyed)
        entry.moveTo(DomposableEntryLifecycle.Destroyed)
        assertEquals(1, creations)
        assertEquals(1, disposals)
        assertFailsWith<IllegalStateException> { entry.moveTo(DomposableEntryLifecycle.Resumed) }
    }

    @Test fun disposedCompositionKeepsLatestSaveablesForHistory() {
        val entry = DomposableEntryScope("route")
        var note = "initial"
        val provider = entry.saveableStateRegistry.registerProvider("note") { note }
        note = "latest"
        provider.unregister()
        val saved = entry.saveableStateRegistry.performSave()
        val restored = DomposableEntryScope("route", entry.id, saved)
        assertEquals("latest", restored.saveableStateRegistry.consumeRestored("note"))
        assertEquals("latest", entry.saveableStateRegistry.consumeRestored("note"))
    }

    @Test fun registrationRejectsDuplicatesAndSaveablesStayPortable() {
        assertFailsWith<IllegalArgumentException> {
            domposableScreenRegistry {
                register(String::class) {}
                register(String::class) {}
            }
        }
        assertTrue(portableSaveable(mapOf("count" to 1, "values" to listOf("a", null, true))))
        assertFalse(portableSaveable(Any()))
    }

    @Test fun multipleProvidersSharingAKeyKeepTheirOrderAfterDisposal() {
        val registry = DomposableEntryScope("route").saveableStateRegistry
        val first = registry.registerProvider("shared") { "text" }
        val second = registry.registerProvider("shared") { 0.5f }
        first.unregister()
        second.unregister()
        assertEquals(listOf("text", 0.5f), registry.performSave()["shared"])
        assertEquals("text", registry.consumeRestored("shared"))
        assertEquals(0.5f, registry.consumeRestored("shared"))
        registry.registerProvider("shared") { "updated" }
        registry.registerProvider("shared") { 0.75f }
        assertEquals(listOf("updated", 0.75f), registry.performSave()["shared"])
    }
}
