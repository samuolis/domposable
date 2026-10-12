package io.github.samuolis.domposable.demo

import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

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
        val target = File(System.getProperty("domposable.screenshots"), "desktop/$name.png")
        target.parentFile.mkdirs()
        val capture = (if (dialog) compose.onNode(isDialog()) else compose.onRoot()).captureToImage()
        ImageIO.write(capture.toAwtImage(), "png", target)
    }
}
