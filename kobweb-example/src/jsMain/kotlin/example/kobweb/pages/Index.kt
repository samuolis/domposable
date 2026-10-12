package example.kobweb.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableLink
import io.github.samuolis.domposable.ui.DomposableText

@Page
@Composable
fun Index() {
    DomposableColumn {
        DomposableText("Domposable with Kobweb", headingLevel = 1)
        DomposableLink(label = "Browse contacts", href = "/contacts")
    }
}
