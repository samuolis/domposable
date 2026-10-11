package example.metro

import io.github.samuolis.domposable.screens.DomposableEntryLifecycle
import io.github.samuolis.domposable.screens.DomposableEntryScope
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class OwnershipTest {
    @Test fun graphSharesRepositoryWhileEntriesOwnModels() {
        val graph = createAppGraph()
        val first = DomposableEntryScope(ContactsRoute)
        val second = DomposableEntryScope(ContactsRoute)
        val a = first.retain("model", ContactsModel::close, graph.newContactsModel)
        val b = second.retain("model", ContactsModel::close, graph.newContactsModel)
        assertNotSame(a, b)
        assertSame(a.repository, b.repository)
        first.moveTo(DomposableEntryLifecycle.Destroyed)
        assertTrue(a.closed)
        assertFalse(b.closed)
    }
}
