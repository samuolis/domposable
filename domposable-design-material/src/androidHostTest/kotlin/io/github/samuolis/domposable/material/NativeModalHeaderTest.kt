package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.samuolis.domposable.ui.DomposableDialog
import io.github.samuolis.domposable.ui.DomposableDialogVariant
import io.github.samuolis.domposable.ui.DomposableModalLayout
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTheme
import io.github.samuolis.domposable.ui.testId
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
class NativeModalHeaderTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun customDialogHeaderReplacesDefaultTitleAndCloseControl() {
        val dismissed = mutableIntStateOf(0)
        compose.setContent {
            DomposableTheme(components = rememberDomposableMaterialComponents()) {
                DomposableDialog(
                    title = "Accessible title",
                    onDismiss = { dismissed.intValue++ },
                    headerContent = { dismiss ->
                        Row {
                            Text("Localized title")
                            TextButton(onClick = dismiss) { Text("Localized dismiss") }
                        }
                    },
                ) { Text("Dialog body") }
            }
        }

        compose.onNodeWithText("Localized title").assertExists()
        compose.onNodeWithText("Close").assertDoesNotExist()
        compose.onNodeWithText("Localized dismiss").performClick()
        compose.runOnIdle { assertEquals(1, dismissed.intValue) }
    }

    @Test
    fun viewportMaximumDoesNotExpandShortDialogContent() {
        compose.setContent {
            DomposableTheme(components = rememberDomposableMaterialComponents()) {
                DomposableDialog(
                    title = "Short dialog",
                    onDismiss = {},
                    modifier = DomposableModifier.testId("bounded-dialog"),
                    variant = DomposableDialogVariant.Basic,
                    layout = DomposableModalLayout(maxViewportFraction = 1f),
                    headerContent = { Text("Short heading") },
                ) { Text("Short body") }
            }
        }
        compose.onNodeWithText("Short body").assertExists()
        val bounds = compose.onNodeWithTag("bounded-dialog").getUnclippedBoundsInRoot()
        val height = (bounds.bottom - bounds.top).value
        assertTrue(height > 0f && height < 300f, "A viewport maximum must not force full-height content: $height")
    }
}
