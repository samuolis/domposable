package io.github.samuolis.domposable.demo

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
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
class VisualTest : VisualCapture() {
    @Test fun tasks() = capture("tasks", DemoRoute.Tasks)

    @Test fun catalog() = capture("catalog", DemoRoute.Catalog)

    @Test fun extendedComponents() = capture("extended-components", DemoRoute.ExtendedCatalog)

    @Test fun recipes() = capture("recipes", DemoRoute.Recipes)

    @OptIn(org.jetbrains.compose.resources.ExperimentalResourceApi::class)
    @Test
    fun shop() {
        org.jetbrains.compose.resources.setResourceReaderAndroidContext(
            androidx.test.core.app.ApplicationProvider
                .getApplicationContext(),
        )

        compose.setContent { ShoppingApp() }
        compose.waitUntil(
            30000,
        ) { compose.onAllNodesWithTag("shop-hero.png-loaded").fetchSemanticsNodes().isNotEmpty() }
        compose.waitForIdle()
        compose.mainClock.advanceTimeByFrame()
        save("shop")
    }
}

@SdkSuppress(minSdkVersion = 26)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MaterialVisualTest : VisualCapture() {
    @Test fun materialActions() = captureMaterial("actions")

    @Test fun materialSelection() = captureMaterial("selection")

    @Test fun materialInputs() = captureMaterial("inputs")

    @Test fun materialSurfaces() = captureMaterial("surfaces")

    @Test fun materialNavigation() = captureMaterial("navigation")

    @Test fun materialFeedback() = captureMaterial("feedback")

    @Test fun materialGestures() = captureMaterial("gestures")

    @Test fun materialScaffold() = captureMaterial("scaffold")

    private fun captureMaterial(section: String) {
        compose.setContent { DemoApp(initialRoute = DemoRoute.Material(section)) }
        compose.waitForIdle()
        val heading = compose.onNodeWithTag("material-section-heading").assertExists().performScrollTo()
        val viewport = compose.onNodeWithTag("material-catalog-scroll")
        val distance = heading.fetchSemanticsNode().boundsInRoot.top - viewport.fetchSemanticsNode().boundsInRoot.top
        viewport.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, distance) }
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        save("material-$section")
    }
}

@androidx.annotation.RequiresApi(26)
abstract class VisualCapture {
    @get:Rule val compose = createComposeRule()

    protected fun capture(
        name: String,
        route: DemoRoute,
    ) {
        compose.setContent { DemoApp(initialRoute = route) }
        compose.waitForIdle()
        save(name)
    }

    protected fun save(name: String) {
        val target = File(System.getProperty("domposable.screenshots"), "android/$name.png")
        target.parentFile!!.mkdirs()
        target.outputStream().use {
            compose
                .onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
