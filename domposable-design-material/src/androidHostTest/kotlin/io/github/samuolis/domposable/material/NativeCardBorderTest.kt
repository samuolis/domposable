package io.github.samuolis.domposable.material

import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.samuolis.domposable.foundation.DomposableCardVariant
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableCard
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.fillMaxSize
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeCardBorderTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun elevatedCardsHonorAnExplicitBorderWithoutShrinkingContent() {
        compose.setContent {
            DomposableMaterialTheme {
                DomposableCard(
                    modifier = DomposableModifier.size(64.dp).testId("bordered-card"),
                    variant = DomposableCardVariant.Elevated,
                    appearance =
                        DomposableSurfaceAppearance(
                            container = DomposableColor(0xFFFFFFFF),
                            border = DomposableColor(0xFFFF0000),
                            borderWidth = 4.dp,
                            shape = DomposableShape.Rectangle,
                            shadowElevation = 0.dp,
                        ),
                    contentPadding = DomposableInsets(),
                ) {
                    DomposableBox(DomposableModifier.fillMaxSize().testId("card-content")) {}
                }
            }
        }
        val card = compose.onNodeWithTag("bordered-card")
        val content = compose.onNodeWithTag("card-content", useUnmergedTree = true)
        assertEquals(card.fetchSemanticsNode().boundsInRoot, content.fetchSemanticsNode().boundsInRoot)
        val image = card.captureToImage().toPixelMap()
        assertEquals(0xFFFF0000.toInt(), image[1, image.height / 2].toArgb())
        assertEquals(0xFFFFFFFF.toInt(), image[image.width / 2, image.height / 2].toArgb())
    }
}
