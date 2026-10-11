package io.github.samuolis.domposable.demo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.graphics.toAwtImage
import java.io.File
import javax.imageio.ImageIO
import org.junit.Rule
import org.junit.Test

class VisualTest {
    @get:Rule val compose = createComposeRule()
    @Test fun tasks() = capture("tasks", DemoRoute.Tasks)
    @Test fun catalog() = capture("catalog", DemoRoute.Catalog)
    @Test fun recipes() = capture("recipes", DemoRoute.Recipes)
    @Test fun shop() {
        compose.setContent { ShoppingApp() }
        compose.waitUntil(30000) { compose.onAllNodesWithTag("shop-hero.png-loaded").fetchSemanticsNodes().isNotEmpty() }
        save("shop")
    }
    private fun capture(name: String, route: DemoRoute) {
        compose.setContent { DemoApp(initialRoute = route) }
        compose.waitForIdle()
        save(name)
    }
    private fun save(name: String) {
        val target = File(System.getProperty("domposable.screenshots"), "desktop/$name.png")
        target.parentFile.mkdirs()
        ImageIO.write(compose.onRoot().captureToImage().toAwtImage(), "png", target)
    }
}
