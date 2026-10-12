package example.koin

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import example.koin.generated.domposableEntries
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.navigation3.DomposableNavHost
import io.github.samuolis.domposable.navigation3.DomposableNavigator
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableScaffold
import io.github.samuolis.domposable.ui.DomposableText
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
) : ViewModel() {
    val name get() = repository.name(id)
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

@Composable
@DomposableScreen(ContactsRoute::class, viewModel = true)
fun ContactsScreen(
    route: ContactsRoute,
    model: ContactsViewModel = koinViewModel(parameters = { parametersOf(route.id) }),
) {
    DomposableText(model.name, headingLevel = 1)
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
