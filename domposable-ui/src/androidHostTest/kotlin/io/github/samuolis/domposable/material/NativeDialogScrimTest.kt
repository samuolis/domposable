package io.github.samuolis.domposable.material

import android.view.Window
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.window.DialogWindowProvider
import io.github.samuolis.domposable.ui.DomposableDialog
import io.github.samuolis.domposable.ui.DomposableDialogVariant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NativeDialogScrimTest {
    @get:Rule val compose = createComposeRule()

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
