package example.consumer

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposablePillBackdropHost
import io.github.samuolis.domposable.ui.DomposablePillBackdropSource
import io.github.samuolis.domposable.ui.DomposablePillNavigationBar
import io.github.samuolis.domposable.ui.DomposablePillNavigationItem
import io.github.samuolis.domposable.ui.DomposableText

@Composable
fun PillConsumer(
    selected: String,
    onSelect: (String) -> Unit,
) {
    DomposablePillBackdropHost {
        DomposableBox {
            DomposablePillBackdropSource { DomposableText("Page content") }
            DomposablePillNavigationBar(
                items =
                    listOf(
                        DomposablePillNavigationItem("home", "Home", icon = { DomposableText("H") }),
                        DomposablePillNavigationItem("inbox", "Inbox", icon = { DomposableText("I") }, href = "/inbox"),
                    ),
                selectedKey = selected,
                onSelect = onSelect,
                label = "Main navigation",
            )
        }
    }
}
