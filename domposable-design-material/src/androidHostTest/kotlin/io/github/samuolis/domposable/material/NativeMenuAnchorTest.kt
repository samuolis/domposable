package io.github.samuolis.domposable.material

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.samuolis.domposable.ui.DomposableMenu
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeMenuAnchorTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun closedMenuKeepsAnchorVisibleInItsOwnersSaveableStateScope() {
        val ownerRegistry = SaveableStateRegistry(emptyMap()) { true }
        compose.setContent {
            DomposableMaterialTheme {
                CompositionLocalProvider(LocalSaveableStateRegistry provides ownerRegistry) {
                    DomposableMenu(
                        expanded = false,
                        onDismiss = {},
                        anchor = {
                            val label =
                                if (LocalSaveableStateRegistry.current === ownerRegistry) {
                                    "owner-registry"
                                } else {
                                    "missing-registry"
                                }
                            Text(
                                label,
                                Modifier.testTag("menu-anchor"),
                            )
                        },
                    )
                }
            }
        }
        compose.onNodeWithTag("menu-anchor").assertTextEquals("owner-registry")
    }
}
