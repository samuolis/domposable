package io.github.samuolis.domposable.demo

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation3.runtime.*
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.navigation3.*
import io.github.samuolis.domposable.screens.*
import io.github.samuolis.domposable.ui.DomposableText
import org.junit.Rule
import org.junit.Test
import kotlin.test.*

class NavigationInteropTest {
    @get:Rule val compose = createComposeRule()

    @Test fun directNavigation3PreservesCallerKeysAndDecorators() {
        val stack = mutableStateListOf("home")
        val popped = mutableListOf<Any>()
        val decorator = NavEntryDecorator<String>(onPop = { popped += it }) { it.Content() }
        compose.setContent {
            DomposableMaterialTheme {
                val entries = rememberDecoratedNavEntries(stack, listOf(decorator)) { route ->
                    NavEntry(route, contentKey = route) { DomposableText(it) }
                }
                DomposableNavDisplay(entries, onBack = { if (stack.size > 1) stack.removeLast() })
            }
        }
        compose.onNodeWithText("home").assertExists()
        compose.runOnIdle { stack.add("detail") }
        compose.onNodeWithText("detail").assertExists()
        compose.runOnIdle { stack.removeLast() }
        compose.waitForIdle()
        compose.onNodeWithText("home").assertExists()
        assertEquals(listOf<Any>("detail"), popped)
    }

    @Test fun manualRegistryAdapterRetainsCoveredEntriesAndDisposesPoppedOnes() {
        val home = DomposableEntryScope("home")
        val detail = DomposableEntryScope("detail")
        val stack = mutableStateListOf(home)
        val disposed = mutableListOf<String>()
        val registry = domposableScreenRegistry {
            register(String::class) {
                retain("resource", dispose = { disposed += route }) { Any() }
                DomposableText(route)
            }
        }
        compose.setContent {
            DomposableMaterialTheme {
                // No navigator or annotation; the application owns stack and display.
                val entries = rememberDomposableNavEntries(stack, registry)
                DomposableNavDisplay(entries, onBack = { if (stack.size > 1) stack.removeLast() })
            }
        }
        compose.runOnIdle { stack.add(detail) }
        compose.waitForIdle()
        assertEquals(DomposableEntryLifecycle.Created, home.lifecycle)
        assertTrue(disposed.isEmpty())
        compose.runOnIdle { stack.removeLast() }
        compose.waitForIdle()
        assertEquals(DomposableEntryLifecycle.Destroyed, detail.lifecycle)
        assertEquals(DomposableEntryLifecycle.Resumed, home.lifecycle)
        assertEquals(listOf("detail"), disposed)
    }
}
