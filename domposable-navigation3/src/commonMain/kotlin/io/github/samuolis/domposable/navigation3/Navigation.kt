package io.github.samuolis.domposable.navigation3

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.*
import io.github.samuolis.domposable.screens.*
import io.github.samuolis.domposable.ui.DomposableModifier

interface DomposableRouteCodec<R : Any> {
    fun encode(route: R): String
    fun decode(path: String): R?
}

@Composable
fun <R : Any> rememberDomposableBackStack(initialRoute: R): SnapshotStateList<DomposableEntryScope<R>> =
    remember { mutableStateListOf(DomposableEntryScope(initialRoute)) }

@Composable
fun <R : Any> DomposableNavigation3Host(
    backStack: List<DomposableEntryScope<R>>,
    screens: DomposableScreenRegistry,
    modifier: DomposableModifier = DomposableModifier,
    visibleEntries: Int = 1,
    onBack: () -> Unit,
) {
    val decorated = rememberDomposableNavEntries(backStack, screens)
    DomposableNavDisplay(decorated, modifier, visibleEntries, onBack)
}

/** Optional entry ownership adapter. Pass the result to native NavDisplay or DomposableNavDisplay. */
@Composable
fun <R : Any> rememberDomposableNavEntries(
    backStack: List<DomposableEntryScope<R>>,
    screens: DomposableScreenRegistry,
    entryDecorators: List<NavEntryDecorator<DomposableEntryScope<R>>> = emptyList(),
): List<NavEntry<DomposableEntryScope<R>>> {
    require(backStack.isNotEmpty()) { "Navigation requires at least one entry" }
    require(backStack.map { it.id }.distinct().size == backStack.size) { "Navigation entry IDs must be unique" }
    val retained = remember { mutableSetOf<DomposableEntryScope<R>>() }
    val currentStack by rememberUpdatedState(backStack.toList())
    retained.addAll(backStack)
    val decorator = remember {
        NavEntryDecorator<DomposableEntryScope<R>>(
            onPop = { id ->
                val removed = retained.filter { it.id == id && it !in currentStack }
                removed.forEach { it.moveTo(DomposableEntryLifecycle.Destroyed); retained.remove(it) }
            },
        ) { it.Content() }
    }
    DisposableEffect(Unit) {
        onDispose { retained.toList().forEach { it.moveTo(DomposableEntryLifecycle.Destroyed) }; retained.clear() }
    }
    val raw = backStack.map { scope -> NavEntry(scope, contentKey = scope.id) { screens.Render(it) } }
    return rememberDecoratedNavEntries(raw, listOf(decorator) + entryDecorators)
}

/**
 * Displays ordinary Navigation 3 entries. The caller owns the stack, entry provider and decorators.
 * Native targets delegate to NavDisplay; Kotlin/JS composes directly into semantic HTML.
 * Entries must already be decorated when ownership/saveable adapters are required.
 */
@Composable
expect fun <T : Any> DomposableNavDisplay(
    entries: List<NavEntry<T>>, modifier: DomposableModifier = DomposableModifier,
    visibleEntries: Int = 1, onBack: () -> Unit,
)

sealed interface DomposableBrowserUrls {
    data object Hash : DomposableBrowserUrls
    /** Host must serve the application entry point for every path beneath this base. */
    data class Path(val basePath: String = "/", val trailingSlash: Boolean = false) : DomposableBrowserUrls {
        init {
            require(basePath.startsWith("/") && !basePath.contains("//"))
            require(basePath.all { it.isLetterOrDigit() || it in "/_-" })
        }
    }
}

/** Application-owned URL codec; hash URLs support GitHub Pages project hosting. */
@Composable
expect fun <R : Any> DomposableBrowserHistory(backStack: SnapshotStateList<DomposableEntryScope<R>>, codec: DomposableRouteCodec<R>, urls: DomposableBrowserUrls = DomposableBrowserUrls.Hash)
