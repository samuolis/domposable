package io.github.samuolis.domposable.material

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import io.github.samuolis.domposable.ui.DomposablePillBackdropHost
import io.github.samuolis.domposable.ui.DomposablePillNavigationBar
import io.github.samuolis.domposable.ui.DomposablePillNavigationItem
import io.github.samuolis.domposable.ui.DomposableText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NativePillNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun portableDestinationsDelegateSelectionToTheComposeLibrary() {
        var selected by mutableStateOf("home")
        var calls = 0
        compose.setContent {
            DomposableMaterialTheme {
                DomposablePillBackdropHost {
                    DomposablePillNavigationBar(
                        items =
                            listOf(
                                DomposablePillNavigationItem("home", "Home", icon = { DomposableText("H") }),
                                DomposablePillNavigationItem("other", "Other", icon = { DomposableText("O") }),
                            ),
                        selectedKey = selected,
                        onSelect = {
                            selected = it
                            calls++
                        },
                        label = "Main navigation",
                    )
                }
            }
        }
        compose.onNodeWithContentDescription("Other").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(1, calls) }
    }
}
