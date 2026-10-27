package io.github.samuolis.domposable.material

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableScaffold
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeScaffoldLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun siblingChromeNodesStackAndReserveContentSpace() {
        compose.setContent {
            DomposableMaterialTheme {
                DomposableScaffold(
                    topBar = {
                        DomposableBox(DomposableModifier.fillMaxWidth().height(20.dp).testId("announcement")) {}
                        DomposableBox(DomposableModifier.fillMaxWidth().height(30.dp).testId("navigation")) {}
                    },
                    bottomBar = {
                        DomposableBox(DomposableModifier.fillMaxWidth().height(10.dp).testId("footer-first")) {}
                        DomposableBox(DomposableModifier.fillMaxWidth().height(15.dp).testId("footer-last")) {}
                    },
                ) {
                    DomposableBox(contentModifier.fillMaxWidth().fillMaxHeight().testId("body")) {}
                }
            }
        }
        val announcement = compose.onNodeWithTag("announcement").fetchSemanticsNode().boundsInRoot
        val navigation = compose.onNodeWithTag("navigation").fetchSemanticsNode().boundsInRoot
        val firstFooter = compose.onNodeWithTag("footer-first").fetchSemanticsNode().boundsInRoot
        val lastFooter = compose.onNodeWithTag("footer-last").fetchSemanticsNode().boundsInRoot
        val body = compose.onNodeWithTag("body").fetchSemanticsNode().boundsInRoot
        assertEquals(announcement.bottom, navigation.top)
        assertEquals(firstFooter.bottom, lastFooter.top)
        assertTrue(body.top >= navigation.bottom)
        assertTrue(body.bottom <= firstFooter.top)
    }
}
