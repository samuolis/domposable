package io.github.samuolis.domposable.demo

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.NavEntry
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.screens.DomposableEntryLifecycle
import io.github.samuolis.domposable.screens.DomposableEntryScope
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RootStackInteropTest {
    @get:Rule val compose = createComposeRule()

    @Test fun inactiveRootDisposesModelsAndRestoresDraftInAFreshScope() {
        val stacks = RootStacks()
        val sessions = mutableListOf<RootSession>()
        compose.setContent {
            DomposableMaterialTheme { RootStackContent(stacks) { _, session -> sessions += session } }
        }
        compose.onNodeWithText("Open Inbox message").performClick()
        compose.onNodeWithText("Inbox home").assertExists()
        val listWidth =
            compose
                .onNodeWithTag("root-0-pane")
                .fetchSemanticsNode()
                .boundsInRoot.width
        val detailWidth =
            compose
                .onNodeWithTag("root-1-pane")
                .fetchSemanticsNode()
                .boundsInRoot.width
        assertTrue(abs(detailWidth - listWidth * 2) <= 2)
        compose.onNodeWithText("Inbox message draft").performTextInput("Keep this draft")
        val old = stacks.active.last()
        compose.runOnIdle { stacks.select("Archive") }
        compose.waitForIdle()
        assertEquals(DomposableEntryLifecycle.Destroyed, old.lifecycle)
        assertTrue(sessions.take(2).all { it.closed })
        compose.runOnIdle { stacks.select("Inbox") }
        compose.waitForIdle()
        assertNotSame(old, stacks.active.last())
        assertEquals(old.id, stacks.active.last().id)
        compose.onNodeWithText("Keep this draft").assertExists()
        val poppedSession = sessions.last()
        compose.onNodeWithText("Root back").performClick()
        compose.waitForIdle()
        assertEquals(1, stacks.active.size)
        assertTrue(poppedSession.closed)
    }

    @Test fun callerSceneChecksRootRolesAndDefinesBackToTheList() {
        fun entry(route: RootRoute) =
            DomposableEntryScope(route).let {
                NavEntry(it, contentKey = it.id, metadata = mapOf(ROOT_ROUTE_METADATA to route)) {}
            }
        val list = entry(RootRoute("Inbox"))
        val detail = entry(RootRoute("Inbox", "message"))
        val entries = listOf(list, detail)
        assertNull(rootListDetailScene(entries, wide = false))
        assertNull(rootListDetailScene(listOf(list), wide = true))
        assertNull(rootListDetailScene(listOf(list, entry(RootRoute("Archive", "message"))), wide = true))
        assertNull(rootListDetailScene(listOf(detail, detail), wide = true))
        val scene = requireNotNull(rootListDetailScene(entries, wide = true))
        assertEquals(entries, scene.entries)
        assertEquals(listOf(list), scene.previousEntries)
        assertEquals(scene, rootListDetailScene(entries, wide = true))
        assertEquals(scene.hashCode(), rootListDetailScene(entries, wide = true)?.hashCode())
    }
}
