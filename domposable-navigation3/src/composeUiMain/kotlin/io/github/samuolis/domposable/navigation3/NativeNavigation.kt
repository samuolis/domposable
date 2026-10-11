package io.github.samuolis.domposable.navigation3

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.*
import androidx.navigation3.ui.NavDisplay
import io.github.samuolis.domposable.screens.DomposableEntryScope
import io.github.samuolis.domposable.ui.*

private data class DomposablePaneScene<T : Any>(
    override val key: Any,
    override val entries: List<NavEntry<T>>,
    override val previousEntries: List<NavEntry<T>>,
) : Scene<T> {
    override val content: @Composable () -> Unit = {
        Row(Modifier.fillMaxSize()) { entries.forEach { entry -> key(entry.contentKey) { Box(Modifier.weight(1f).fillMaxHeight()) { entry.Content() } } } }
    }
    override val metadata: Map<String, Any> = emptyMap()
}

@Composable
internal actual fun <R : Any> PlatformNavigation3Display(entries: List<NavEntry<DomposableEntryScope<R>>>, modifier: DomposableModifier, visibleEntries: Int, onBack: () -> Unit) {
    val strategy = remember(visibleEntries) {
        SceneStrategy<DomposableEntryScope<R>> { all ->
            DomposablePaneScene(all.last().contentKey, all.takeLast(visibleEntries), all.dropLast(1))
        }
    }
    NavDisplay(entries = entries, modifier = modifier.toComposeModifier(), sceneStrategies = listOf(strategy), onBack = onBack)
}

@Composable
actual fun <R : Any> DomposableBrowserHistory(backStack: SnapshotStateList<DomposableEntryScope<R>>, codec: DomposableRouteCodec<R>, urls: DomposableBrowserUrls) = Unit
