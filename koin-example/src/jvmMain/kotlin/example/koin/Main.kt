package example.koin

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.samuolis.domposable.navigation3.rememberDomposableNavigator

fun main() =
    application {
        Window(onCloseRequest = ::exitApplication, title = "Domposable with Koin") {
            ContactsApp(rememberDomposableNavigator(ContactsRoute("alex")))
        }
    }
