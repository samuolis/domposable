@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableTopAppBarVariant
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTopAppBar
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
@Config(sdk = [36], qualifiers = "w390dp-h1000dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeTopAppBarContractTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun flexibleBarsKeepNavigationAndActionsOutOfTheSubtitleSlot() {
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    DomposableTopAppBar(
                        "Medium",
                        modifier = DomposableModifier.testId("medium"),
                        variant = DomposableTopAppBarVariant.MediumFlexible,
                        navigationIcon = { DomposableText("NAV") },
                        actions = { DomposableText("ACT") },
                    )
                    DomposableTopAppBar(
                        "Large",
                        modifier = DomposableModifier.testId("large"),
                        variant = DomposableTopAppBarVariant.LargeFlexible,
                    )
                    DomposableTopAppBar(
                        "Two rows",
                        modifier = DomposableModifier.testId("two"),
                        variant = DomposableTopAppBarVariant.TwoRows,
                    )
                }
            }
        }
        compose.onNodeWithTag("medium").assertHeightIsEqualTo(112.dp)
        compose.onNodeWithTag("large").assertHeightIsEqualTo(120.dp)
        compose.onNodeWithTag("two").assertHeightIsEqualTo(112.dp)
        val nav = compose.onNodeWithText("NAV").fetchSemanticsNode().boundsInRoot
        val action = compose.onNodeWithText("ACT").fetchSemanticsNode().boundsInRoot
        assertTrue(nav.top < 64f && action.top < 64f)
        assertTrue(action.left > nav.right)
        assertEquals(28f, largestFont("Medium"))
        assertEquals(36f, largestFont("Large"))
        assertEquals(28f, largestFont("Two rows"))
    }

    @Test
    fun subtitlesUseTheMaterialMeasurementAndTypography() {
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    DomposableTopAppBar(
                        "With subtitle",
                        subtitle = "Supporting text",
                        modifier = DomposableModifier.testId("with-subtitle"),
                        variant = DomposableTopAppBarVariant.MediumFlexible,
                    )
                    DomposableTopAppBar(
                        "Small subtitle",
                        subtitle = "Legacy supporting text",
                        modifier = DomposableModifier.testId("small-subtitle"),
                    )
                    androidx.compose.material3.MediumFlexibleTopAppBar(
                        title = { androidx.compose.material3.Text("Reference") },
                        subtitle = { androidx.compose.material3.Text("Reference subtitle") },
                        modifier =
                            androidx.compose.ui.Modifier
                                .testTag("reference"),
                    )
                }
            }
        }
        val actual =
            compose
                .onNodeWithTag("with-subtitle")
                .fetchSemanticsNode()
                .boundsInRoot.height
        val expected =
            compose
                .onNodeWithTag("reference")
                .fetchSemanticsNode()
                .boundsInRoot.height
        assertEquals(expected, actual)
        compose.onNodeWithTag("with-subtitle").assertHeightIsEqualTo(136.dp)
        compose.onNodeWithTag("small-subtitle").assertHeightIsEqualTo(64.dp)
        assertEquals(14f, largestFont("Supporting text"))
        assertEquals(12f, largestFont("Legacy supporting text"))
    }

    private fun largestFont(text: String): Float {
        val results = mutableListOf<TextLayoutResult>()
        compose.onAllNodesWithText(text).fetchSemanticsNodes().forEach { node ->
            node.config
                .getOrNull(SemanticsActions.GetTextLayoutResult)
                ?.action
                ?.invoke(results)
        }
        return results.maxOf { it.layoutInput.style.fontSize.value }
    }
}
