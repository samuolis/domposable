package example.kobweb.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import io.github.samuolis.domposable.foundation.*
import io.github.samuolis.domposable.material.*
import io.github.samuolis.domposable.ui.*

/** A compile-checked page boundary. Kobweb owns URL navigation; Domposable owns the feature UI. */
@Page("/contacts")
@Composable
fun ContactsPage(ctx: PageContext) {
    val model = remember { ContactsPageModel() }
    DomposableTheme(components = rememberDomposableMaterialComponents()) {
        ContactsFeature(model, onOpen = { ctx.router.navigateTo("/contacts/$it") })
    }
}
class ContactsPageModel { var search by mutableStateOf("") }
@Composable
fun ContactsFeature(model: ContactsPageModel, onOpen: (String) -> Unit) {
    DomposableColumn(gap = 12.dp) {
        DomposableText("Contacts", style = DomposableTextStyle.Title)
        DomposableTextField(model.search, { model.search = it }, "Search contacts")
        DomposableButton("Open Alex", { onOpen("alex") })
    }
}
