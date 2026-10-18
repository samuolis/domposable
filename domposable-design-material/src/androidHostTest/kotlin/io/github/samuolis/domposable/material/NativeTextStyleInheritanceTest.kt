package io.github.samuolis.domposable.material

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.ui.DomposableText
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
class NativeTextStyleInheritanceTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun inheritedTextUsesTheAmbientMaterialTextStyle() {
        val ambient = TextStyle(fontSize = 31.sp, lineHeight = 37.sp, fontWeight = FontWeight.Bold)
        val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        compose.setContent {
            DomposableMaterialTheme {
                CompositionLocalProvider(LocalTextStyle provides ambient) {
                    DomposableText("Inherited", style = DomposableTextStyle.Inherit)
                }
            }
        }

        compose.onNodeWithText("Inherited").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            assertTrue(action(layouts))
        }

        val inheritedStyle = layouts.single().layoutInput.style
        assertEquals(ambient.fontSize, inheritedStyle.fontSize)
        assertEquals(ambient.lineHeight, inheritedStyle.lineHeight)
        assertEquals(ambient.fontWeight, inheritedStyle.fontWeight)
    }
}
