package io.github.samuolis.domposable.adaptive

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PaneTest {
    private data class Route(
        val id: String,
    )

    private data class Entry(
        val id: Int,
        val route: Route,
    )

    private val home = Route("home")
    private val detail = Route("detail")
    private val task = Route("task")

    private fun pane(route: Route): DomposablePane<Route>? =
        when (route) {
            home -> DomposablePane.List("catalog")
            detail -> DomposablePane.Detail("catalog", home)
            else -> null
        }

    @Test fun resizingPreservesEntryIdentityAndUnrelatedTasksStaySingle() {
        val entries = listOf(Entry(1, home), Entry(2, detail))
        val compact = resolveDomposablePaneLayout(entries, false) { pane(it.route) }
        assertSame(entries.last(), (compact as DomposablePaneLayout.Single).entry)
        val wide = resolveDomposablePaneLayout(entries, true) { pane(it.route) } as DomposablePaneLayout.ListDetail
        assertSame(entries.first(), wide.list)
        assertSame(entries.last(), wide.detail)
        assertEquals(
            DomposablePaneLayout.Single(task),
            resolveDomposablePaneLayout(listOf(home, detail, task), true, ::pane),
        )
        assertEquals(
            DomposablePaneLayout.Single(detail),
            resolveDomposablePaneLayout(listOf(task, detail), true, ::pane),
        )
        assertEquals(
            DomposablePaneLayout.Single(detail),
            resolveDomposablePaneLayout(listOf(home, detail), true) {
                if (it == home) DomposablePane.List("unrelated") else pane(it)
            },
        )
        assertFailsWith<IllegalArgumentException> { resolveDomposablePaneLayout(emptyList<Route>(), true, ::pane) }
    }

    @Test fun directLinkRestoresItsParentOnceAndRetainsExistingEntries() {
        val active = Entry(8, detail)
        var created = 0
        val restored = ensureDomposablePaneParent(listOf(active), Entry::route, ::pane) { Entry(++created, it) }
        assertEquals(listOf(home, detail), restored.map(Entry::route))
        assertSame(active, restored.last())
        assertSame(restored, ensureDomposablePaneParent(restored, Entry::route, ::pane) { error("Duplicate parent") })
        assertEquals(1, created)
        val empty = emptyList<Entry>()
        assertSame(empty, ensureDomposablePaneParent(empty, Entry::route, ::pane) { error("Empty stack") })
        val ordinary = listOf(Entry(9, task))
        assertSame(ordinary, ensureDomposablePaneParent(ordinary, Entry::route, ::pane) { error("Ordinary task") })
    }

    @Test fun wideSelectionAndSiblingTabsOnlyReplaceRelatedDetails() {
        val current = DomposablePane.Detail("catalog", home, siblingKey = "item-1")
        assertTrue(domposablePaneShouldReplaceDetail(current, current, true))
        assertFalse(domposablePaneShouldReplaceDetail(current, current, false))
        assertFalse(domposablePaneShouldReplaceDetail(current, DomposablePane.Detail("catalog", task), true))
        assertFalse(domposablePaneShouldReplaceDetail(current, DomposablePane.Detail("other", home), true))
        assertFalse(domposablePaneShouldReplaceDetail(current, null, true))
        assertTrue(domposablePaneShouldReplaceSiblingDetail(current, current.copy()))
        assertFalse(domposablePaneShouldReplaceSiblingDetail(current, current.copy(siblingKey = "item-2")))
        assertFalse(domposablePaneShouldReplaceSiblingDetail(current, current.copy(parent = task)))
        assertFalse(domposablePaneShouldReplaceSiblingDetail(current.copy(siblingKey = null), current))
    }
}
