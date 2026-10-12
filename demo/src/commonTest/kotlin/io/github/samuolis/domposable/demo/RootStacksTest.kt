package io.github.samuolis.domposable.demo

import io.github.samuolis.domposable.screens.DomposableEntryLifecycle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame

class RootStacksTest {
    @Test fun rootSwitchRestoresEntryIdentityAndUiStateWithFreshOwnership() {
        val stacks = RootStacks()
        stacks.open("message")
        val original = stacks.active.last()
        val saved = original.saveableStateRegistry.registerProvider("draft") { "Inbox draft" }
        stacks.select("Archive")
        assertEquals(DomposableEntryLifecycle.Destroyed, original.lifecycle)
        saved.unregister()
        stacks.open("archived")
        stacks.select("Inbox")
        val restored = stacks.active.last()
        assertNotSame(original, restored)
        assertEquals(original.id, restored.id)
        assertEquals(original.route, restored.route)
        assertEquals(DomposableEntryLifecycle.Created, restored.lifecycle)
        assertEquals("Inbox draft", restored.saveableStateRegistry.consumeRestored("draft"))
        stacks.select("Archive")
        assertEquals(
            "archived",
            stacks.active
                .last()
                .route.detail,
        )
    }

    @Test fun snapshotsIncludeBothRootsAndRootBackDoesNotEmptyTheStack() {
        val stacks = RootStacks()
        stacks.open("message")
        stacks.select("Archive")
        stacks.open("saved")
        val restored = RootStacks(stacks.selected, stacks.snapshot())
        assertEquals(stacks.active.last().id, restored.active.last().id)
        restored.select("Inbox")
        assertEquals(
            "message",
            restored.active
                .last()
                .route.detail,
        )
        restored.back()
        restored.back()
        assertEquals(1, restored.active.size)
        val root = restored.active.single()
        restored.select("Inbox")
        assertEquals(root, restored.active.single())
        assertFailsWith<IllegalArgumentException> { restored.select("unknown") }
        assertFailsWith<IllegalArgumentException> { restored.open("") }
    }
}
