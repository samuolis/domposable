package example.koin

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import example.koin.generated.domposableEntries
import io.github.samuolis.domposable.foundation.DomposableMviDestination
import io.github.samuolis.domposable.lifecycle.DomposableMvi
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.navigation3.DomposableNavHost
import io.github.samuolis.domposable.navigation3.DomposableNavigator
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableScaffold
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

data class ContactsRoute(
    val id: String,
)

class ContactsRepository {
    fun name(id: String): String = if (id == "alex") "Alex" else "Sam"
}

class ContactsViewModel(
    val id: String,
    val repository: ContactsRepository,
) : ViewModel(),
    DomposableMvi<ContactsState, String> {
    val name get() = repository.name(id)
    private val mutableState = MutableStateFlow(ContactsState(name))
    override val state: StateFlow<ContactsState> = mutableState

    override fun dispatch(action: String) {
        mutableState.value = mutableState.value.copy(note = action)
    }

    var cleared = false
        private set

    override fun onCleared() {
        cleared = true
    }
}

val contactsModule =
    module {
        single { ContactsRepository() }
        viewModel { (id: String) -> ContactsViewModel(id, get()) }
    }

data class ContactsState(
    val name: String,
    val note: String = "",
)

object ContactsBinding {
    @Composable
    fun model(route: ContactsRoute): ContactsViewModel = koinViewModel(parameters = { parametersOf(route.id) })
}

@Composable
@DomposableMviDestination(ContactsRoute::class, ContactsBinding::class)
fun ContactsScreen(
    state: ContactsState,
    onAction: (String) -> Unit,
) {
    DomposableText(state.name, headingLevel = 1)
    DomposableTextField(value = state.note, onValueChange = onAction, label = "Note")
}

@Composable
fun ContactsApp(navigator: DomposableNavigator<ContactsRoute>) {
    KoinApplication(koinConfiguration { modules(contactsModule) }) {
        DomposableMaterialTheme {
            DomposableScaffold(topBar = {
                DomposableRow {
                    DomposableButton("Open Alex", { navigator.navigate(ContactsRoute("alex")) })
                    DomposableButton("Open Sam", { navigator.navigate(ContactsRoute("sam")) })
                }
            }) {
                DomposableNavHost(navigator, domposableEntries(), contentModifier)
            }
        }
    }
}
