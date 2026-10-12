package io.github.samuolis.domposable.demo

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.SdkSuppress
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@SdkSuppress(minSdkVersion = 26)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PolicyVisualTest {
    @get:Rule val compose = createComposeRule()

    @Test fun policies() {
        compose.setContent { DemoApp(initialRoute = DemoRoute.Policies) }
        compose.waitForIdle()
        save("navigation-policies", false)
    }

    @Test fun routeOverlay() {
        compose.setContent { DemoApp(initialRoute = DemoRoute.Policies) }
        compose.onNodeWithText("Open Inbox live message").performScrollTo().performClick()
        compose.onNodeWithText("Inbox message live draft").performScrollTo().performTextInput("A retained draft")
        compose.onNodeWithText("Request back").performScrollTo().performClick()
        compose.waitForIdle()
        save("navigation-overlay", true)
    }

    private fun save(
        name: String,
        dialog: Boolean,
    ) {
        val target = File(System.getProperty("domposable.screenshots"), "android/$name.png")
        target.parentFile.mkdirs()
        val capture = (if (dialog) compose.onNode(isDialog()) else compose.onRoot()).captureToImage()
        target.outputStream().use { capture.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
