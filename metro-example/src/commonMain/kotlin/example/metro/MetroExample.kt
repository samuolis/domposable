package example.metro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.screens.DomposableDestination
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableText

abstract class AppScope private constructor()

@Inject
@SingleIn(AppScope::class)
class ContactsRepository {
    val names = listOf("Alex", "Sam", "Taylor")
}

@Inject class ContactsModel(
    val repository: ContactsRepository,
) {
    var selected by mutableStateOf("")
    var closed = false
        private set

    fun close() {
        closed = true
    }
}

@DependencyGraph(AppScope::class)
abstract class AppGraph {
    abstract val newContactsModel: () -> ContactsModel
}

fun createAppGraph(): AppGraph = createGraph<AppGraph>()

data object ContactsRoute

/** The application owns both the graph and the entry lifetime. No DI dependency in the UI API. */
fun contactsDestination(graph: AppGraph) =
    DomposableDestination(ContactsRoute::class) {
        val model =
            remember(this, graph) {
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
