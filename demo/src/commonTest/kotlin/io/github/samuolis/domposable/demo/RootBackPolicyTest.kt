package io.github.samuolis.domposable.demo

import io.github.samuolis.domposable.screens.DomposableEntryLifecycle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RootBackPolicyTest {
    @Test fun liveModelsSurviveDisposedEntryScopesUntilTheirAppOwnerCloses() {
        val stacks = RootStacks()
        val models = RootModels()
        stacks.open("message")
        val old = stacks.active.last()
        val model = models.get(old.route)
        model.edit("Live draft")
        stacks.select("Archive")
        val other = models.get(stacks.active.last().route)
        assertEquals(DomposableEntryLifecycle.Destroyed, old.lifecycle)
        assertFalse(model.closed)
        stacks.select("Inbox")
        assertSame(model, models.get(stacks.active.last().route))
        assertEquals("Live draft", model.note)
        models.close()
        assertTrue(model.closed && other.closed)
        assertFailsWith<IllegalStateException> { models.get(old.route) }
    }

    @Test fun cancelDoesNotPopAndStaleOrRepeatedCommitsCannotRemoveAnotherEntry() {
        val stacks = RootStacks()
        val policy = RootBackPolicy(stacks, RootModels())
        stacks.open("message")
        val id = stacks.active.last().id
        policy.progress(0.6f)
        assertEquals(id, stacks.active.last().id)
        policy.cancel()
        policy.commit()
        assertEquals(2, stacks.active.size)
        policy.progress(0.5f)
        stacks.open("another")
        policy.commit()
        assertEquals(3, stacks.active.size)
        policy.progress(1f)
        policy.commit()
        policy.commit()
        assertEquals(2, stacks.active.size)
        policy.progress(0.5f)
        stacks.select("Archive")
        policy.commit()
        assertEquals(1, stacks.active.size)
        assertNull(policy.preview)
        assertFailsWith<IllegalArgumentException> { policy.progress(Float.NaN) }
    }

    @Test fun editedEntriesUseOneOverlayAndConfirmationOnlyPopsTheSelectedRoot() {
        val stacks = RootStacks()
        val models = RootModels()
        val policy = RootBackPolicy(stacks, models)
        stacks.open("message")
        val model = models.get(stacks.active.last().route)
        model.edit("Kept locally")
        policy.progress(1f)
        policy.commit()
        assertEquals(3, stacks.active.size)
        assertTrue(
            stacks.active
                .last()
                .route.overlay,
        )
        stacks.overlay()
        assertEquals(3, stacks.active.size)
        policy.request() // Dismiss the overlay without removing its underlying edited entry.
        assertEquals(2, stacks.active.size)
        assertTrue(model.dirty)
        policy.request()
        policy.confirm("stale-overlay-id")
        assertEquals(3, stacks.active.size)
        policy.confirm()
        policy.confirm()
        assertEquals(1, stacks.active.size)
        assertEquals("Kept locally", model.note)
        assertFalse(model.dirty)
        policy.request()
        assertEquals(1, stacks.active.size)
    }
}
