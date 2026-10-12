package io.github.samuolis.domposable.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import io.github.samuolis.domposable.screens.DomposableEntryScope

@Composable
internal actual fun <R : Any> PlatformBrowserHistory(
    backStack: SnapshotStateList<DomposableEntryScope<R>>,
    codec: DomposableRouteCodec<R>,
    urls: DomposableBrowserUrls,
) = Unit
