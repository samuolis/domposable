package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableComponentStyles
import io.github.samuolis.domposable.foundation.DomposableIconAppearance
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeContentInteropTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun annotatedTextRetainsSpansAndReportsLayoutThroughSharedAppearance() {
        val text =
            AnnotatedString("Styled text", listOf(AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold), 0, 6)))
        var measured: TextLayoutResult? = null
        compose.setContent {
            DomposableMaterialTheme {
                DomposableText(
                    text = text,
                    modifier = DomposableModifier.testId("text"),
                    appearance = DomposableTextAppearance(fontSize = 24f, minLines = 2),
                    onTextLayout = { measured = it },
                )
            }
        }
        compose.runOnIdle {
            val layout = assertNotNull(measured)
            assertEquals(text, layout.layoutInput.text)
            assertEquals(24f, layout.layoutInput.style.fontSize.value)
        }
        val layouts = mutableListOf<TextLayoutResult>()
        compose.onNodeWithTag("text").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertTrue(layouts.isNotEmpty())
    }

    @Test
    @androidx.test.filters.SdkSuppress(minSdkVersion = 26)
    fun painterIconUsesSharedModifierAndAccessibleDescription() {
        compose.setContent {
            DomposableMaterialTheme(
                tokens =
                    DomposableTokens(
                        components =
                            DomposableComponentStyles(
                                icon = DomposableIconAppearance(color = DomposableColor(0xFFFF0000)),
                            ),
                    ),
            ) {
                Column {
                    DomposableIcon(
                        painter = ColorPainter(Color.White),
                        description = "Team artwork",
                        modifier = DomposableModifier.size(32.dp).testId("icon"),
                    )
                }
            }
        }
        compose.onNodeWithTag("icon").assertContentDescriptionEquals("Team artwork")
        val node = compose.onNodeWithTag("icon").fetchSemanticsNode()
        assertEquals(node.size.width, node.size.height)
        assertTrue(node.size.width > 0)
        val pixels = compose.onNodeWithTag("icon").captureToImage().toPixelMap()
        assertEquals(Color.Red, pixels[pixels.width / 2, pixels.height / 2])
    }

    @Test
    fun passiveSelectionKeepsStateWithoutAnAction() {
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    io.github.samuolis.domposable.ui.DomposableCheckbox(
                        checked = true,
                        onCheckedChange = null,
                        showLabel = false,
                        modifier = DomposableModifier.testId("passive"),
                    )
                }
            }
        }
        compose
            .onNodeWithTag("passive")
            .assertIsOn()
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
    }

    @Test
    fun nativeFieldPreservesTransformationAndImeAction() {
        var submitted = 0
        compose.setContent {
            DomposableMaterialTheme {
                DomposableTextField(
                    props =
                        io.github.samuolis.domposable.ui
                            .DomposableTextFieldProps("abc", {}, "Code"),
                    nativeOptions =
                        DomposableNativeTextFieldOptions(
                            keyboardOptions =
                                androidx.compose.foundation.text.KeyboardOptions(
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                                ),
                            keyboardActions =
                                androidx.compose.foundation.text.KeyboardActions(
                                    onDone = { submitted++ },
                                ),
                            visualTransformation =
                                androidx.compose.ui.text.input.VisualTransformation { value ->
                                    androidx.compose.ui.text.input.TransformedText(
                                        AnnotatedString(value.text.uppercase()),
                                        androidx.compose.ui.text.input.OffsetMapping.Identity,
                                    )
                                },
                        ),
                    modifier = DomposableModifier.testId("field"),
                )
            }
        }
        val layouts = mutableListOf<TextLayoutResult>()
        compose.onNodeWithTag("field").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertEquals(
            "ABC",
            layouts
                .single()
                .layoutInput.text.text,
        )
        compose.onNodeWithTag("field").performSemanticsAction(SemanticsActions.OnImeAction) { it() }
        assertEquals(1, submitted)
    }
}
