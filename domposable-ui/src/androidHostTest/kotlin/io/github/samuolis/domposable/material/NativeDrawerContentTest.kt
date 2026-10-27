package io.github.samuolis.domposable.material

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import io.github.samuolis.domposable.ui.DomposableNavigationDrawer
import io.github.samuolis.domposable.ui.DomposableNavigationDrawerItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeDrawerContentTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun modalDrawerOnlyComposesItsItemsInsideTheOpenSheet() {
        var open by mutableStateOf(false)
        compose.setContent {
            DomposableMaterialTheme {
                DomposableNavigationDrawer(open, { open = false }) {
                    DomposableNavigationDrawerItem("Home", true, {})
                    DomposableNavigationDrawerItem("Saved", false, {})
                }
            }
        }
        compose.onNodeWithText("Home").assertDoesNotExist()
        compose.runOnIdle { open = true }
        compose.waitForIdle()
        compose.onAllNodesWithText("Home").assertCountEquals(1)
        compose.onAllNodesWithText("Saved").assertCountEquals(1)
        val home = compose.onNodeWithText("Home").fetchSemanticsNode().boundsInRoot
        val saved = compose.onNodeWithText("Saved").fetchSemanticsNode().boundsInRoot
        check(saved.top >= home.bottom) { "Drawer items must not overlap" }
        compose.runOnIdle { open = false }
        compose.onNodeWithText("Home").assertDoesNotExist()
    }
}
