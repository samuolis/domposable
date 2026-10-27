package example.kobweb

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.core.KobwebApp
import io.github.samuolis.domposable.ui.DomposableTheme
import org.jetbrains.compose.web.dom.Main

@App
@Composable
fun App(content: @Composable () -> Unit) {
    KobwebApp {
        DomposableTheme {
            Main { content() }
        }
    }
}
