package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import io.github.samuolis.domposable.navigation3.DomposableNavDisplay
import io.github.samuolis.domposable.screens.DomposableEntryScope
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth

@Composable
internal actual fun RootRecipeDisplay(
    entries: List<NavEntry<DomposableEntryScope<RootRoute>>>,
    wide: Boolean,
    onBack: () -> Unit,
) = DomposableNavDisplay(
    entries,
    DomposableModifier.fillMaxWidth().fillMaxHeight(),
    visibleEntries = rootPaneEntries(entries, wide)?.size ?: 1,
    onBack,
)
