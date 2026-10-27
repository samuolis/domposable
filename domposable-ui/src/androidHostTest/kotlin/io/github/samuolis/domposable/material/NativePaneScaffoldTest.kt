package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.adaptive.DomposablePaneLayout
import io.github.samuolis.domposable.adaptive.DomposablePaneScaffold
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
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
import io.github.samuolis.domposable.foundation.dp as portableDp

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1200dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativePaneScaffoldTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun listWidthClampsTheOriginalWindowFractionAndCompactDetailRetainsItsList() {
        var width by mutableStateOf(600)
        var expanded by mutableStateOf(true)
        var listCompositions = 0
        var density = 1f
        compose.setContent {
            density = LocalDensity.current.density
            DomposableMaterialTheme {
                Box(Modifier.width(width.dp).height(600.dp)) {
                    val layout =
                        if (expanded) {
                            DomposablePaneLayout.ListDetail("list", "detail")
                        } else {
                            DomposablePaneLayout.Single("detail")
                        }
                    DomposablePaneScaffold(
                        layout = layout,
                        listLabel = "Directory",
                        detailLabel = "Details",
                        listModifier = DomposableModifier.testId("list-pane"),
                        detailModifier = DomposableModifier.testId("detail-pane"),
                        listFraction = 0.32f,
                        minListWidth = 260.portableDp,
                        maxListWidth = 360.portableDp,
                        retainedList = "list",
                    ) { entry ->
                        val instance = remember { if (entry == "list") ++listCompositions else 0 }
                        if (entry == "list") {
                            BoxWithConstraints(Modifier.fillMaxWidth()) {
                                assertTrue(constraints.hasBoundedWidth)
                                DomposableBox(
                                    DomposableModifier.fillMaxWidth().height(40.portableDp).testId(entry),
                                ) { DomposableText("$entry $instance") }
                            }
                        } else {
                            DomposableBox(
                                DomposableModifier.fillMaxWidth().height(80.portableDp).testId(entry),
                            ) { DomposableText("$entry $instance") }
                        }
                    }
                }
            }
        }

        fun listWidth() =
            compose
                .onNodeWithTag("list", useUnmergedTree = true)
                .fetchSemanticsNode()
                .boundsInRoot.width / density

        fun paneHeight(tag: String) =
            compose
                .onNodeWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNode()
                .boundsInRoot.height / density

        assertEquals(260f, listWidth(), 1f)
        assertEquals(600f, paneHeight("detail-pane"), 1f)
        assertEquals(paneHeight("detail-pane"), paneHeight("list-pane"), 1f)
        compose.runOnIdle { width = 1_000 }
        assertEquals(320f, listWidth(), 1f)
        compose.runOnIdle { width = 1_200 }
        assertEquals(360f, listWidth(), 1f)
        compose.runOnIdle { expanded = false }
        compose.onNodeWithText("list 1").assertDoesNotExist()
        compose.onNodeWithText("detail 0").assertExists()
        compose.runOnIdle { expanded = true }
        compose.onNodeWithText("list 1").assertExists()
        compose.runOnIdle { assertEquals(1, listCompositions) }
    }
}
