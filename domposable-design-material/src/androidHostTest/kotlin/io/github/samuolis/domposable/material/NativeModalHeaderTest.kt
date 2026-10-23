package io.github.samuolis.domposable.material

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.window.DialogWindowProvider
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBottomSheet
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableDialog
import io.github.samuolis.domposable.ui.DomposableDialogVariant
import io.github.samuolis.domposable.ui.DomposableModalLayout
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTheme
import io.github.samuolis.domposable.ui.fillMaxSize
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
    fun modalSheetContentRemainsVisibleAfterMaterialPositionsIt() = assertPositionedSheet(modal = true)

    @Test
    fun persistentSheetContentRemainsVisibleAfterMaterialPositionsIt() = assertPositionedSheet(modal = false)

    private fun assertPositionedSheet(modal: Boolean) {
        val clicks = mutableIntStateOf(0)
        lateinit var sheetView: View
        compose.setContent {
            DomposableMaterialTheme {
                DomposableBox(DomposableModifier.fillMaxSize()) {
                    DomposableBottomSheet(
                        title = "Sheet heading",
                        onDismiss = {},
                        modal = modal,
                        modifier = DomposableModifier.testId("styled-sheet"),
                        skipPartiallyExpanded = true,
                        appearance =
                            DomposableSurfaceAppearance(
                                border = DomposableColor(0xFFFF0000),
                                borderWidth = 4.dp,
                                shape = DomposableShape.Rectangle,
                                shadowElevation = 8.dp,
                            ),
                    ) {
                        val view = LocalView.current
                        SideEffect { sheetView = view.rootView }
                        TextButton(onClick = { clicks.intValue++ }) { Text("Sheet action") }
                    }
                }
            }
        }
        compose.onNodeWithText("Sheet heading").assertIsDisplayed()
        compose.onNodeWithText("Sheet action").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, clicks.intValue) }
        val heading = compose.onNodeWithText("Sheet heading").fetchSemanticsNode().boundsInRoot
        compose.runOnIdle {
            val pixels = Bitmap.createBitmap(sheetView.width, sheetView.height, Bitmap.Config.ARGB_8888)
            sheetView.draw(Canvas(pixels))
            val redRows =
                (0 until pixels.height).filter { y ->
                    (0 until pixels.width).any { x -> pixels.getPixel(x, y) == 0xFFFF0000.toInt() }
                }
            assertTrue(heading.center.y.toInt() in redRows, "The border must follow the visible sheet content")
            assertTrue(redRows.first() > pixels.height / 2, "The border must not stay at the unshifted origin")
        }
    }

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

    @Test
    fun viewportWidthUsesTheFullWindowWhileKeepingTheConfiguredContentMaximum() {
        val fillViewport = mutableStateOf(false)
        var dialogWindow: Window? = null
        compose.setContent {
            DomposableTheme(components = rememberDomposableMaterialComponents()) {
                DomposableDialog(
                    title = "Viewport dialog",
                    onDismiss = {},
                    modifier = DomposableModifier.testId("viewport-dialog"),
                    variant = DomposableDialogVariant.Basic,
                    layout = DomposableModalLayout(maxWidth = 360.dp, fillViewportWidth = fillViewport.value),
                    headerContent = {},
                ) {
                    val view = LocalView.current
                    SideEffect { dialogWindow = (view.parent as DialogWindowProvider).window }
                    Text("Viewport body")
                }
            }
        }

        compose.onNodeWithText("Viewport body").assertExists()
        compose.runOnIdle {
            assertEquals(WRAP_CONTENT, requireNotNull(dialogWindow).attributes.width)
            fillViewport.value = true
        }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(MATCH_PARENT, requireNotNull(dialogWindow).attributes.width) }
        val bounds = compose.onNodeWithTag("viewport-dialog").getUnclippedBoundsInRoot()
        assertTrue((bounds.right - bounds.left).value <= 361f, "The content must retain its configured maximum")
        assertTrue((bounds.bottom - bounds.top).value < 300f, "A full-width window must not force full-height content")
    }

    @Test
    fun basicDialogUsesConfiguredSpacingBetweenHeaderBodyAndActions() {
        compose.setContent {
            DomposableTheme(components = rememberDomposableMaterialComponents()) {
                DomposableDialog(
                    title = "Accessible title",
                    onDismiss = {},
                    actions = { Text("Spacing action", Modifier.testTag("spacing-action")) },
                    variant = DomposableDialogVariant.Basic,
                    layout = DomposableModalLayout(sectionSpacing = 12.dp),
                    headerContent = { Text("Spacing header", Modifier.testTag("spacing-header")) },
                ) { Text("Spacing body", Modifier.testTag("spacing-body")) }
            }
        }

        val header = compose.onNodeWithTag("spacing-header").getUnclippedBoundsInRoot()
        val body = compose.onNodeWithTag("spacing-body").getUnclippedBoundsInRoot()
        val actions = compose.onNodeWithTag("spacing-action").getUnclippedBoundsInRoot()
        assertEquals(12f, (body.top - header.bottom).value, 1f)
        assertEquals(12f, (actions.top - body.bottom).value, 1f)
    }
}
