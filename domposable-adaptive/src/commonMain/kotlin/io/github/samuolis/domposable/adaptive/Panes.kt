package io.github.samuolis.domposable.adaptive

import androidx.compose.runtime.staticCompositionLocalOf

/** Route relationships; the application owns route identity and navigation mutations. */
sealed interface DomposablePane<out R : Any> {
    data class List(
        val group: String,
    ) : DomposablePane<Nothing>

    data class Detail<R : Any>(
        val group: String,
        val parent: R,
        /** Sibling destinations can represent tabs on the same selected item. */
        val siblingKey: String? = null,
    ) : DomposablePane<R>
}

sealed interface DomposablePaneLayout<out T> {
    data class Single<T>(
        val entry: T,
    ) : DomposablePaneLayout<T>

    data class ListDetail<T>(
        val list: T,
        val detail: T,
    ) : DomposablePaneLayout<T>
}

/** Unrelated tasks remain single destinations; resizing never mutates the entry stack. */
fun <T> resolveDomposablePaneLayout(
    entries: List<T>,
    canShowTwoPanes: Boolean,
    pane: (T) -> DomposablePane<*>?,
): DomposablePaneLayout<T> {
    require(entries.isNotEmpty()) { "Pane layout requires at least one entry" }
    val active = entries.last()
    val detail = pane(active) as? DomposablePane.Detail<*>
    if (canShowTwoPanes && detail != null) {
        val list =
            entries.dropLast(1).lastOrNull {
                (pane(it) as? DomposablePane.List)?.group == detail.group
            }
        if (list != null) return DomposablePaneLayout.ListDetail(list, active)
    }
    return DomposablePaneLayout.Single(active)
}

/** Restore a declared direct-link parent once, retaining all existing entry identities. */
fun <T, R : Any> ensureDomposablePaneParent(
    entries: List<T>,
    route: (T) -> R,
    pane: (R) -> DomposablePane<R>?,
    createEntry: (R) -> T,
): List<T> {
    val active = entries.lastOrNull() ?: return entries
    val detail = pane(route(active)) as? DomposablePane.Detail<R> ?: return entries
    val hasParent =
        entries.dropLast(1).any {
            route(it) == detail.parent && (pane(route(it)) as? DomposablePane.List)?.group == detail.group
        }
    return if (hasParent) entries else entries.dropLast(1) + createEntry(detail.parent) + active
}

/** Wide selections replace the selected detail; compact selections keep ordinary Back history. */
fun domposablePaneShouldReplaceDetail(
    current: DomposablePane<*>?,
    next: DomposablePane<*>?,
    canShowTwoPanes: Boolean,
): Boolean =
    canShowTwoPanes && current is DomposablePane.Detail<*> && next is DomposablePane.Detail<*> &&
        current.group == next.group && current.parent == next.parent

fun domposablePaneShouldReplaceSiblingDetail(
    current: DomposablePane<*>?,
    next: DomposablePane<*>?,
): Boolean =
    current is DomposablePane.Detail<*> && next is DomposablePane.Detail<*> &&
        current.group == next.group && current.parent == next.parent &&
        current.siblingKey != null && current.siblingKey == next.siblingKey

/** Null outside a paired presentation, so screens retain their standalone chrome. */
val LocalDomposablePaneRole = staticCompositionLocalOf<DomposablePaneRole?> { null }
