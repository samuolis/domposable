package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.ui.DomposableLink
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
class NativeLinkContentTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun customContentKeepsCallbackAndUriNavigation() {
        val opened = mutableListOf<String>()
        var callbacks = 0
        val handler =
            object : UriHandler {
                override fun openUri(uri: String) {
                    opened += uri
                }
            }
        compose.setContent {
            CompositionLocalProvider(LocalUriHandler provides handler) {
                DomposableMaterialTheme {
                    Column {
                        DomposableLink(
                            label = "Open URI",
                            href = "https://example.com/uri",
                            content = { Text("Custom URI content") },
                        )
                        DomposableLink(
                            label = "Callback action",
                            href = "https://example.com/callback",
                            onClick = { callbacks++ },
                            buttonAppearance = DomposableButtonAppearance(),
                            buttonStyle = DomposableButtonStyle.Outlined,
                            content = { Text("Custom action content") },
                        )
                    }
                }
            }
        }
        compose.onNodeWithText("Custom URI content").performClick()
        compose.onNodeWithText("Custom action content").performClick()
        compose.runOnIdle {
            assertEquals(listOf("https://example.com/uri"), opened)
            assertEquals(1, callbacks)
        }
    }
}
