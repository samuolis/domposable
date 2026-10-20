package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.ui.DomposableIcon
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableVectorIcon
import io.github.samuolis.domposable.ui.size
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import io.github.samuolis.domposable.foundation.dp as domposableDp

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeVectorIconTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun vectorIconKeepsAccessibleDescriptionAndModifierSize() {
        compose.setContent {
            DomposableMaterialTheme {
                Box {
                    DomposableIcon(
                        vector = DomposableVectorIcon("M0 0h24v24H0z"),
                        description = "Map marker",
                        modifier = DomposableModifier.size(36.domposableDp),
                    )
                }
            }
        }

        compose.onNodeWithContentDescription("Map marker").assertWidthIsEqualTo(36.dp)
    }
}
