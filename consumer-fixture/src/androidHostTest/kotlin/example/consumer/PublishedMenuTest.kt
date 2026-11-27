package example.consumer

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.ui.DomposableMenu
import io.github.samuolis.domposable.ui.DomposableMenuItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PublishedMenuTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun publishedMenuOpensWithConsumerMaterialVersion() {
        compose.setContent {
            DomposableMaterialTheme {
                DomposableMenu(expanded = true, onDismiss = {}, items = listOf(DomposableMenuItem("Edit", {})))
            }
        }
        compose.onNodeWithText("Edit").assertExists()
    }
}
