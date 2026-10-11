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
    require(backStack.isNotEmpty()) { "Navigation requires at least one entry" }
    require(visibleEntries > 0)
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
    val decorated = rememberDecoratedNavEntries(raw, listOf(decorator))
    PlatformNavigation3Display(decorated, modifier, visibleEntries, onBack)
}

@Composable
internal expect fun <R : Any> PlatformNavigation3Display(
    entries: List<NavEntry<DomposableEntryScope<R>>>, modifier: DomposableModifier,
    visibleEntries: Int, onBack: () -> Unit,
)

/** Application-owned URL codec; hash URLs support GitHub Pages project hosting. */
@Composable
expect fun <R : Any> DomposableBrowserHistory(backStack: SnapshotStateList<DomposableEntryScope<R>>, codec: DomposableRouteCodec<R>)
