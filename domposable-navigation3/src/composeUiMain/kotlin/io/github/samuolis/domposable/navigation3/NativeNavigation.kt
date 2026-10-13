package io.github.samuolis.domposable.navigation3

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.NavDisplay
import io.github.samuolis.domposable.screens.DomposableEntryScope
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.toComposeModifier

private data class DomposablePaneScene<T : Any>(
    override val key: Any,
    override val entries: List<NavEntry<T>>,
    override val previousEntries: List<NavEntry<T>>,
) : Scene<T> {
    override val content: @Composable () -> Unit = {
        Row(Modifier.fillMaxSize()) {
            entries.forEach { entry ->
                key(entry.contentKey) { Box(Modifier.weight(1f).fillMaxHeight()) { entry.Content() } }
            }
        }
    }
    override val metadata: Map<String, Any> = emptyMap()
}

@Composable
actual fun <T : Any> DomposableNavDisplay(
    entries: List<NavEntry<T>>,
    modifier: DomposableModifier,
    visibleEntries: Int,
    onBack: () -> Unit,
) {
    require(entries.isNotEmpty()) { "Navigation requires at least one entry" }
    require(visibleEntries > 0)
    require(entries.map { it.contentKey }.distinct().size == entries.size) { "Navigation content keys must be unique" }
    val strategy =
        remember(visibleEntries) {
            SceneStrategy<T> { all ->
                DomposablePaneScene(all.last().contentKey, all.takeLast(visibleEntries), all.dropLast(1))
            }
        }
    NavDisplay(
        entries = entries,
        modifier = modifier.toComposeModifier(),
        sceneStrategies = listOf(strategy),
        onBack = onBack,
    )
}

/** Native Nav3 integration with caller-owned scenes, chrome, entry decorators and back-stack policy. */
@Composable
fun <T : Any> DomposableNavDisplay(
    entries: List<NavEntry<T>>,
    sceneStrategies: List<SceneStrategy<T>>,
    modifier: DomposableModifier = DomposableModifier,
    sceneDecoratorStrategies: List<SceneDecoratorStrategy<T>> = emptyList(),
    onBack: () -> Unit,
) {
    require(entries.isNotEmpty()) { "Navigation requires at least one entry" }
    require(entries.map { it.contentKey }.distinct().size == entries.size) { "Navigation content keys must be unique" }
    NavDisplay(
        entries = entries,
        modifier = modifier.toComposeModifier(),
        sceneStrategies = sceneStrategies,
        sceneDecoratorStrategies = sceneDecoratorStrategies,
        onBack = onBack,
    )
}

@Composable
actual fun <R : Any> DomposableBrowserHistory(
    backStack: SnapshotStateList<DomposableEntryScope<R>>,
    codec: DomposableRouteCodec<R>,
    urls: DomposableBrowserUrls,
) = PlatformBrowserHistory(backStack, codec, urls)

@Composable
internal expect fun <R : Any> PlatformBrowserHistory(
    backStack: SnapshotStateList<DomposableEntryScope<R>>,
    codec: DomposableRouteCodec<R>,
    urls: DomposableBrowserUrls,
)
