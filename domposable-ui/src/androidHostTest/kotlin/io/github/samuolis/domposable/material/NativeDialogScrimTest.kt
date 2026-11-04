package io.github.samuolis.domposable.material

import android.view.Window
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import io.github.samuolis.domposable.ui.DomposableDialog
import io.github.samuolis.domposable.ui.DomposableDialogVariant
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
class NativeDialogScrimTest {
    @get:Rule val compose = createComposeRule()

    @Test
    @Config(qualifiers = "w360dp-h640dp-mdpi")
    fun shortDialogRemainsContentSized() {
        compose.setContent {
            DomposableMaterialTheme {
                DomposableDialog(
                    title = "Confirm",
                    onDismiss = {},
                    variant = DomposableDialogVariant.Basic,
                    actions = { Button(onClick = {}) { Text("Done") } },
                ) { Text("Short message") }
            }
        }
        compose.onNodeWithText("Done").assertIsDisplayed()
        val bounds = compose.onNode(isDialog()).getUnclippedBoundsInRoot()
        assertTrue(bounds.bottom - bounds.top < 400.dp, "Short dialogs should not fill the viewport")
    }

    @Test
    @Config(qualifiers = "w360dp-h640dp-mdpi")
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun longScrollableBodyKeepsConfirmationVisibleAndClickable() {
        var confirmed = false
        compose.setContent {
            DomposableMaterialTheme {
                DomposableDialog(
                    title = "Confirm payment",
                    onDismiss = {},
                    variant = DomposableDialogVariant.Basic,
                    actions = { Button(onClick = { confirmed = true }) { Text("Confirm") } },
                ) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Text("Payment details", modifier = Modifier.height(1200.dp))
                        Text("Last detail")
                    }
                }
            }
        }
        compose.onNodeWithText("Last detail").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Confirm").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(true, confirmed) }
    }

    @Test
    fun overrideUpdatesWindowDimmingAndRestoresThePlatformDefault() {
        val opacity = mutableStateOf<Float?>(null)
        var window: Window? = null
        compose.setContent {
            DomposableMaterialTheme {
                DomposableDialog("Saving", {}, variant = DomposableDialogVariant.Basic, scrimOpacity = opacity.value) {
                    val view = LocalView.current
                    SideEffect { window = (view.parent as DialogWindowProvider).window }
                    Text("Loading")
                }
            }
        }
        var platformOpacity = 0f
        compose.runOnIdle {
            platformOpacity = requireNotNull(window).attributes.dimAmount
            opacity.value = 0.5f
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(0.5f, requireNotNull(window).attributes.dimAmount)
            opacity.value = 0.7f
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(0.7f, requireNotNull(window).attributes.dimAmount)
            opacity.value = null
        }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(platformOpacity, requireNotNull(window).attributes.dimAmount) }
    }
}
