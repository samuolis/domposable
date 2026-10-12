package example.kobweb.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableLink
import io.github.samuolis.domposable.ui.DomposableText

@Page("/contacts/{id}")
@Composable
fun Contact(ctx: PageContext) {
    val name = if (ctx.route.params["id"] == "alex") "Alex" else "Unknown contact"
    DomposableColumn {
        DomposableText(name, headingLevel = 1)
        DomposableText("This shared feature lets Kobweb own the URL and browser history.")
        DomposableLink(label = "All contacts", href = "/contacts")
    }
}
