package io.github.samuolis.domposable.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.NavDisplay
import io.github.samuolis.domposable.screens.DomposableEntryScope

/** Caller-owned scene policy: a one-third list and two-thirds detail on wide content. */
internal data class RootListDetailScene(
    override val entries: List<RootNavEntry>,
    override val previousEntries: List<RootNavEntry>,
) : Scene<DomposableEntryScope<RootRoute>> {
    override val key: Any = entries.map { it.contentKey }
    override val metadata: Map<String, Any> = emptyMap()
    override val content: @Composable () -> Unit = {
        Row(Modifier.fillMaxSize()) {
            entries.forEachIndexed { index, entry ->
                key(entry.contentKey) {
                    Box(Modifier.weight(if (index == 0) 1f else 2f).fillMaxHeight().testTag("root-$index-pane")) {
                        entry.Content()
                    }
                }
            }
        }
    }
}

internal fun rootListDetailScene(
    entries: List<RootNavEntry>,
    wide: Boolean,
): RootListDetailScene? {
    val panes = rootPaneEntries(entries, wide) ?: return null
    return RootListDetailScene(panes, entries.dropLast(1))
}

@Composable
internal actual fun RootRecipeDisplay(
    entries: List<RootNavEntry>,
    wide: Boolean,
    onBack: () -> Unit,
) {
    val strategy = remember(wide) { SceneStrategy<DomposableEntryScope<RootRoute>> { rootListDetailScene(it, wide) } }
    // Use Navigation 3 directly. It supplies the single-pane fallback when the strategy declines.
    NavDisplay(
        entries = entries,
        modifier = Modifier.fillMaxSize(),
        sceneStrategies = listOf(strategy),
        onBack = onBack,
    )
}
