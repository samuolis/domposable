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
import io.github.samuolis.domposable.ui.DomposableMenu
import io.github.samuolis.domposable.ui.DomposableMenuItem
import io.github.samuolis.domposable.ui.DomposableSelect
import io.github.samuolis.domposable.ui.DomposableSelectOption
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.DomposableTooltip
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

    @Test fun selectPopupWorksInsideAPortableEntry() {
        val entry = DomposableEntryScope(ModalRoute)
        val screens =
            domposableScreenRegistry {
                register(ModalRoute::class) {
                    var choice by rememberSaveable { mutableStateOf("one") }
                    DomposableSelect(
                        choice,
                        { choice = it },
                        "Choice",
                        listOf(DomposableSelectOption("one", "One"), DomposableSelectOption("two", "Two")),
                    )
                }
            }
        compose.setContent { DomposableMaterialTheme { screens.Render(entry) } }
        compose.onNodeWithText("Choice: One").performClick()
        compose.onNodeWithText("Two").performClick()
        compose.onNodeWithText("Choice: Two").assertExists()
        compose.runOnIdle { entry.saveableStateRegistry.performSave() }
    }

    @Test fun menuPopupUpdatesTheOwningFeatureState() {
        val entry = DomposableEntryScope(ModalRoute)
        val screens =
            domposableScreenRegistry {
                register(ModalRoute::class) {
                    var open by rememberSaveable { mutableStateOf(false) }
                    var selected by rememberSaveable { mutableStateOf(false) }
                    DomposableColumn {
                        DomposableButton("Open menu", { open = true })
                        val message = if (selected) "Menu action received" else "No action yet"
                        DomposableText(message)
                        DomposableMenu(
                            open,
                            { open = false },
                            listOf(DomposableMenuItem("Choose", { selected = true })),
                        )
                    }
                }
            }
        compose.setContent { DomposableMaterialTheme { screens.Render(entry) } }
        compose.onNodeWithText("Open menu").performClick()
        compose.onNodeWithText("Choose").performClick()
        compose.onNodeWithText("Menu action received").assertExists()
        compose.runOnIdle { entry.saveableStateRegistry.performSave() }
    }

    @Test fun tooltipContentKeepsItsFeatureSaveables() {
        val entry = DomposableEntryScope(ModalRoute)
        val screens =
            domposableScreenRegistry {
                register(ModalRoute::class) {
                    DomposableTooltip("Supplemental help") {
                        var draft by rememberSaveable { mutableStateOf("") }
                        DomposableTextField(draft, { draft = it }, "Tooltip draft")
                    }
                }
            }
        compose.setContent { DomposableMaterialTheme { screens.Render(entry) } }
        compose.onNodeWithText("Tooltip draft").performTextInput("Tooltip feature draft")
        compose.runOnIdle {
            val values =
                entry.saveableStateRegistry
                    .performSave()
                    .values
                    .flatten()
            assertTrue(values.map { if (it is State<*>) it.value else it }.contains("Tooltip feature draft"))
        }
    }

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
