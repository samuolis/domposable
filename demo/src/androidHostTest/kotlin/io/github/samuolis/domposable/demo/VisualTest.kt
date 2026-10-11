package io.github.samuolis.domposable.demo

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import android.graphics.Bitmap
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class VisualTest {
    @get:Rule val compose = createComposeRule()
    @Test fun tasks() = capture("tasks", DemoRoute.Tasks)
    @Test fun catalog() = capture("catalog", DemoRoute.Catalog)
    @Test fun recipes() = capture("recipes", DemoRoute.Recipes)
    private fun capture(name: String, route: DemoRoute) {
        compose.setContent { DemoApp(initialRoute = route) }
        compose.waitForIdle()
        val target = File(System.getProperty("domposable.screenshots"), "android/$name.png")
        target.parentFile!!.mkdirs()
        target.outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
