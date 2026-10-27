package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.sp
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeTextMinLinesTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun appearanceReservesMinimumLinesAndDefaultsToOne() {
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    DomposableText("Single line", modifier = DomposableModifier.testId("default"))
                    DomposableText(
                        "Single line",
                        modifier = DomposableModifier.testId("one-line"),
                        appearance = DomposableTextAppearance(),
                    )
                    DomposableText(
                        "Single line",
                        modifier = DomposableModifier.testId("fixed-one-line"),
                        appearance = DomposableTextAppearance(fontSize = 20f, lineHeight = 20f),
                    )
                    DomposableText(
                        "Single line",
                        modifier = DomposableModifier.testId("three-lines"),
                        appearance = DomposableTextAppearance(fontSize = 20f, lineHeight = 20f, minLines = 3),
                    )
                }
            }
        }

        val defaultHeight =
            compose
                .onNodeWithTag("default")
                .fetchSemanticsNode()
                .size.height
        val oneLineHeight =
            compose
                .onNodeWithTag("one-line")
                .fetchSemanticsNode()
                .size.height
        val fixedOneLineHeight =
            compose
                .onNodeWithTag("fixed-one-line")
                .fetchSemanticsNode()
                .size.height
        val threeLinesHeight =
            compose
                .onNodeWithTag("three-lines")
                .fetchSemanticsNode()
                .size.height

        assertEquals(defaultHeight, oneLineHeight)
        val expectedHeight = fixedOneLineHeight + compose.density.run { 40.sp.roundToPx() }
        assertEquals(expectedHeight, threeLinesHeight)
    }
}
