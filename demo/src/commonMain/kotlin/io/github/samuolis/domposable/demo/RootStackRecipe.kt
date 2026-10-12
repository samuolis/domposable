package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavEntry
import io.github.samuolis.domposable.adaptive.DomposablePaneRecipe
import io.github.samuolis.domposable.adaptive.rememberDomposableAdaptiveInfo
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.navigation3.rememberDomposableNavEntries
import io.github.samuolis.domposable.screens.DomposableEntryScope
import io.github.samuolis.domposable.screens.domposableScreenRegistry
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableMeasure
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTabs
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.padding

internal const val ROOT_ROUTE_METADATA = "root.recipe.route"

internal typealias RootNavEntry = NavEntry<DomposableEntryScope<RootRoute>>

@Composable
@DomposableScreen(DemoRoute.Roots::class)
internal fun RootStackRecipe() {
    val stacks = rememberSaveable(saver = RootStacks.saver) { RootStacks() }
    RootStackContent(stacks)
}

@Composable
internal fun RootStackContent(
    stacks: RootStacks,
    onSession: (RootRoute, RootSession) -> Unit = { _, _ -> },
) {
    val screens =
        remember(stacks, onSession) {
            domposableScreenRegistry {
                register(RootRoute::class) {
                    val session =
                        retain("session", dispose = { it.close() }) {
                            RootSession().also { onSession(route, it) }
                        }
                    var note by rememberSaveable { mutableStateOf("") }
                    DomposableColumn(DomposableModifier.padding(16.dp), gap = 12.dp) {
                        val label = route.detail.ifEmpty { "home" }
                        DomposableText("${route.root} $label", style = DomposableTextStyle.Title, headingLevel = 2)
                        DomposableText("Entry: $id")
                        DomposableText("Session: ${session.instance}")
                        DomposableTextField(note, { note = it }, "${route.root} $label draft")
                        if (route.detail.isEmpty()) {
                            DomposableButton("Open ${route.root} message", { stacks.open("message") })
                        } else {
                            DomposableButton("Root back", stacks::back)
                        }
                    }
                }
            }
        }
    DomposableColumn(DomposableModifier.padding(16.dp).fillMaxWidth(), gap = 12.dp) {
        DomposableText("Independent root stacks", style = DomposableTextStyle.Title, headingLevel = 1)
        DomposableText("Switch roots: entry IDs and drafts return; disposed sessions are recreated.")
        DomposableTabs(recipeRoots, recipeRoots.indexOf(stacks.selected), { stacks.select(recipeRoots[it]) })
        DomposableMeasure(DomposableModifier.fillMaxWidth().height(480.dp)) { size ->
            val wide = DomposablePaneRecipe().isDualPane(rememberDomposableAdaptiveInfo(size))
            // Root identity keeps automatic saveable keys stable across a host's recreation.
            key(stacks.selected) {
                val entries = rememberDomposableNavEntries(stacks.active, screens)
                val routed =
                    entries.mapIndexed { index, entry ->
                        val scope = stacks.active[index]
                        NavEntry(
                            scope,
                            contentKey = entry.contentKey,
                            metadata = mapOf(ROOT_ROUTE_METADATA to scope.route),
                        ) {
                            entry.Content()
                        }
                    }
                RootRecipeDisplay(routed, wide, stacks::back)
            }
        }
    }
}

@Composable
internal expect fun RootRecipeDisplay(
    entries: List<NavEntry<DomposableEntryScope<RootRoute>>>,
    wide: Boolean,
    onBack: () -> Unit,
)

internal fun rootPaneEntries(
    entries: List<RootNavEntry>,
    wide: Boolean,
): List<RootNavEntry>? {
    val panes = entries.takeLast(2)
    if (!wide || panes.size != 2) return null
    val list = panes[0].metadata[ROOT_ROUTE_METADATA] as? RootRoute ?: return null
    val detail = panes[1].metadata[ROOT_ROUTE_METADATA] as? RootRoute ?: return null
    if (list.root != detail.root || list.detail.isNotEmpty() || detail.detail.isEmpty()) return null
    return panes
}
