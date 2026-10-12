package io.github.samuolis.domposable.demo

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.NavigationEventInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.screens.DomposableEntryLifecycle
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class NavigationPolicyInteropTest {
    @get:Rule val compose = createComposeRule()

    @Test fun actualNavigationEventsCancelWithoutPopAndCompleteOnlyOnce() {
        val stacks = RootStacks().apply { open("message") }
        val old = stacks.active.last()
        val models = RootModels()
        val model = models.get(old.route)
        val policy = RootBackPolicy(stacks, models)
        val dispatcher = NavigationEventDispatcher()
        val input = GestureInput()
        dispatcher.addInput(input)
        val owner =
            object : NavigationEventDispatcherOwner {
                override val navigationEventDispatcher = dispatcher
            }
        try {
            compose.setContent {
                CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides owner) {
                    DomposableMaterialTheme { NavigationPolicyContent(stacks, models, policy) }
                }
            }
            compose.runOnIdle {
                input.start()
                input.progress()
            }
            assertEquals(old.id, stacks.active.last().id)
            compose.runOnIdle { input.cancel() }
            compose.waitForIdle()
            assertEquals(old.id, stacks.active.last().id)
            assertFalse(model.closed)
            compose.runOnIdle {
                input.start()
                input.progress()
                input.complete()
            }
            compose.waitForIdle()
            assertEquals(1, stacks.active.size)
            assertEquals(DomposableEntryLifecycle.Destroyed, old.lifecycle)
            assertFalse(model.closed)
        } finally {
            dispatcher.dispose()
            models.close()
        }
    }

    @Test fun dialogStrategyKeepsTheUnderlyingDraftUntilConfirmed() {
        val stacks = RootStacks().apply { open("message") }
        val old = stacks.active.last()
        val models = RootModels()
        val model = models.get(old.route).apply { edit("Keep my live draft") }
        val policy = RootBackPolicy(stacks, models)
        compose.setContent { DomposableMaterialTheme { NavigationPolicyContent(stacks, models, policy) } }
        compose.onNodeWithText("Request back").performClick()
        compose.onNodeWithText("Keep editing").performClick()
        compose.onNodeWithText("Keep my live draft").assertExists()
        assertEquals(2, stacks.active.size)
        compose.onNodeWithText("Request back").performClick()
        compose.onNodeWithText("Leave entry").performClick()
        compose.waitForIdle()
        assertEquals(1, stacks.active.size)
        assertEquals(DomposableEntryLifecycle.Destroyed, old.lifecycle)
        assertFalse(model.closed)
        assertEquals("Keep my live draft", model.note)
        models.close()
    }
}

private class GestureInput : NavigationEventInput() {
    fun start() = dispatchOnBackStarted(NavigationEvent(swipeEdge = NavigationEvent.EDGE_LEFT))

    fun progress() = dispatchOnBackProgressed(NavigationEvent(swipeEdge = NavigationEvent.EDGE_LEFT, progress = 0.65f))

    fun cancel() = dispatchOnBackCancelled()

    fun complete() = dispatchOnBackCompleted()
}
