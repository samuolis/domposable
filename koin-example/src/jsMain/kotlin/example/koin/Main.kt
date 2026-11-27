package example.koin

import kotlinx.browser.window
import org.jetbrains.compose.web.renderComposable

fun main() {
    val initial = ContactsCodec.decode(window.location.hash.removePrefix("#")) ?: ContactsRoute("alex")
    renderComposable(rootElementId = "root") {
        ContactsStarterApp(initial, browserHistory = true)
    }
}
