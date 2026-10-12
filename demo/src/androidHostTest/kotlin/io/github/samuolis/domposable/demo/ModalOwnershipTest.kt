package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.screens.DomposableEntryScope
import io.github.samuolis.domposable.screens.domposableScreenRegistry
import io.github.samuolis.domposable.ui.DomposableBottomSheet
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableDialog
import io.github.samuolis.domposable.ui.DomposableTextField
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ModalOwnershipTest {
    @get:Rule val compose = createComposeRule()

    @Test fun dialogWindowStateDoesNotPollutePortableEntrySaveables() = checkModal(sheet = false)

    @Test fun sheetWindowStateDoesNotPollutePortableEntrySaveables() = checkModal(sheet = true)

    private fun checkModal(sheet: Boolean) {
        val entry = DomposableEntryScope(ModalRoute)
        val screens =
            domposableScreenRegistry {
                register(ModalRoute::class) {
                    var open by rememberSaveable { mutableStateOf(true) }
                    if (open) {
                        val editor: @Composable () -> Unit = {
                            var text by rememberSaveable { mutableStateOf("") }
                            DomposableTextField(text, { text = it }, "Modal draft")
                            DomposableButton("Finish", { open = false })
                        }
                        if (sheet) {
                            DomposableBottomSheet("Editor", { open = false }, content = editor)
                        } else {
                            DomposableDialog("Editor", { open = false }, content = editor)
                        }
                    }
                }
            }
        compose.setContent { DomposableMaterialTheme { screens.Render(entry) } }
        compose.onNodeWithText("Modal draft").performTextInput("Feature-owned draft")
        compose.runOnIdle {
            assertTrue(
                entry.saveableStateRegistry
                    .performSave()
                    .values
                    .flatten()
                    .map { if (it is State<*>) it.value else it }
                    .contains("Feature-owned draft"),
            )
            assertFalse(entry.saveableStateRegistry.canBeSaved(Any()))
        }
        compose.onNodeWithText("Finish").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Modal draft").assertDoesNotExist()
    }
}

private data object ModalRoute
