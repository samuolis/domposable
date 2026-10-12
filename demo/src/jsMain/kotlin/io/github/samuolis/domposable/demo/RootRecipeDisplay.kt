package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.NavEntry
import io.github.samuolis.domposable.navigation3.DomposableNavDisplay
import io.github.samuolis.domposable.screens.DomposableEntryScope
import io.github.samuolis.domposable.ui.DomposableDialog
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth

@Composable
internal actual fun RootRecipeDisplay(
    entries: List<NavEntry<DomposableEntryScope<RootRoute>>>,
    wide: Boolean,
    onBack: () -> Unit,
) {
    val overlay = entries.last().metadata[ROOT_ROUTE_METADATA] as? RootRoute
    val underneath = if (overlay?.overlay == true) entries.dropLast(1) else entries
    DomposableNavDisplay(
        underneath,
        DomposableModifier.fillMaxWidth().fillMaxHeight(),
        visibleEntries = rootPaneEntries(underneath, wide)?.size ?: 1,
        onBack,
    )
    if (overlay?.overlay == true) {
        DomposableDialog("Leave this entry?", onDismiss = onBack) {
            CompositionLocalProvider(LocalRootOverlayTitleProvided provides true) { entries.last().Content() }
        }
    }
}

internal actual fun rootPresentationMetadata(route: RootRoute): Map<String, Any> = emptyMap()
