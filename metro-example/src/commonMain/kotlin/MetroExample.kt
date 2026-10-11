package example.metro

import androidx.compose.runtime.*
import dev.zacsweers.metro.*
import io.github.samuolis.domposable.foundation.*
import io.github.samuolis.domposable.screens.*
import io.github.samuolis.domposable.ui.*

abstract class AppScope private constructor()
@Inject @SingleIn(AppScope::class)
class ContactsRepository { val names = listOf("Alex", "Sam", "Taylor") }
@Inject class ContactsModel(val repository: ContactsRepository) {
    var selected by mutableStateOf("")
    var closed = false
        private set
    fun close() { closed = true }
}
@DependencyGraph(AppScope::class)
abstract class AppGraph { abstract val newContactsModel: () -> ContactsModel }

fun createAppGraph(): AppGraph = createGraph<AppGraph>()
data object ContactsRoute

/** The application owns both the graph and the entry lifetime. No DI dependency in the UI API. */
fun contactsDestination(graph: AppGraph) = DomposableDestination(ContactsRoute::class) {
    val model = remember(this, graph) {
        retain("contacts.model", dispose = ContactsModel::close, factory = graph.newContactsModel)
    }
    ContactsScreen(model)
}
@Composable
fun ContactsScreen(model: ContactsModel) {
    DomposableColumn(gap = 12.dp) {
        DomposableText("Contacts", style = DomposableTextStyle.Title)
        model.repository.names.forEach { name -> DomposableButton(name, { model.selected = name }) }
        DomposableText(model.selected)
    }
}
