package example.metro
import io.github.samuolis.domposable.screens.*
import kotlin.test.*
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
