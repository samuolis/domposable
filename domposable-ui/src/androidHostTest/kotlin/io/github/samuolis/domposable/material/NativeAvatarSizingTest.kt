package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.ui.DomposableAvatar
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.widthIn
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
class NativeAvatarSizingTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun avatarHonorsExplicitSizeAndMaximumWidthConstraints() {
        compose.setContent {
            DomposableMaterialTheme {
                Box {
                    DomposableAvatar(
                        initials = "AB",
                        description = "Alex Brown",
                        modifier = DomposableModifier.size(64.domposableDp),
                    )
                    DomposableAvatar(
                        initials = "CD",
                        description = "Casey Doe",
                        modifier = DomposableModifier.widthIn(max = 20.domposableDp),
                    )
                }
            }
        }

        compose.onNodeWithContentDescription("Alex Brown").assertWidthIsEqualTo(64.dp).assertHeightIsEqualTo(64.dp)
        compose.onNodeWithContentDescription("Casey Doe").assertWidthIsEqualTo(20.dp)
    }
}
