package example.kobweb.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.material.rememberDomposableMaterialComponents
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.DomposableTheme

/** A compile-checked page boundary. Kobweb owns URL navigation; Domposable owns the feature UI. */
@Page("/contacts")
@Composable
fun ContactsPage(ctx: PageContext) {
    val model = remember { ContactsPageModel() }
    DomposableTheme(components = rememberDomposableMaterialComponents()) {
        ContactsFeature(model, onOpen = { ctx.router.navigateTo("/contacts/$it") })
    }
}

class ContactsPageModel {
    var search by mutableStateOf("")
}

@Composable
fun ContactsFeature(
    model: ContactsPageModel,
    onOpen: (String) -> Unit,
) {
    DomposableColumn(gap = 12.dp) {
        DomposableText("Contacts", style = DomposableTextStyle.Title)
        DomposableTextField(model.search, { model.search = it }, "Search contacts")
        DomposableButton("Open Alex", { onOpen("alex") })
    }
}
