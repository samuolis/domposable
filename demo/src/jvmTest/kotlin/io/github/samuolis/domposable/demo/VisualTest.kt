package io.github.samuolis.domposable.demo

import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

class VisualTest {
    @get:Rule val compose = createComposeRule()

    @Test fun tasks() = capture("tasks", DemoRoute.Tasks)

    @Test fun catalog() = capture("catalog", DemoRoute.Catalog)

    @Test fun extensions() = capture("extensions", DemoRoute.Extensions)

    @Test
    fun composition() {
        compose.setContent { DemoApp(initialRoute = DemoRoute.Composition) }
        compose.waitUntil(30000) {
            compose.onAllNodesWithTag("shared-card-loaded").fetchSemanticsNodes().isNotEmpty()
        }
        compose.waitForIdle()
        save("composition")
    }

    @Test fun extendedComponents() = capture("extended-components", DemoRoute.ExtendedCatalog)

    @Test fun recipes() = capture("recipes", DemoRoute.Recipes)

    @Test fun materialActions() = captureMaterial("actions")

    @Test fun materialSelection() = captureMaterial("selection")

    @Test fun materialInputs() = captureMaterial("inputs")

    @Test fun materialSurfaces() = captureMaterial("surfaces")

    @Test fun materialNavigation() = captureMaterial("navigation")

    @Test fun materialFeedback() = captureMaterial("feedback")

    @Test fun materialGestures() = captureMaterial("gestures")

    @Test fun materialScaffold() = captureMaterial("scaffold")

    @Test fun rootStacks() {
        compose.setContent { DemoApp(initialRoute = DemoRoute.Roots) }
        compose.onNodeWithText("Open Inbox message").performClick()
        compose.waitForIdle()
        save("root-stacks")
    }

    @Test fun collections() = capture("collections", DemoRoute.Collections)

    @Test fun inputLab() = capture("input-lab", DemoRoute.Inputs)

    @Test fun inputLabHelp() {
        compose.setContent { DemoApp(initialRoute = DemoRoute.Inputs) }
        compose.onNodeWithText("Open help").performClick()
        compose.waitForIdle()
        val target = File(System.getProperty("domposable.screenshots"), "desktop/input-lab-help.png")
        target.parentFile.mkdirs()
        ImageIO.write(compose.onNode(isDialog()).captureToImage().toAwtImage(), "png", target)
    }

    @Test fun shop() {
        compose.setContent { ShoppingApp() }
        compose.waitUntil(
            30000,
        ) { compose.onAllNodesWithTag("shop-hero.png-loaded").fetchSemanticsNodes().isNotEmpty() }
        save("shop")
    }

    private fun capture(
        name: String,
        route: DemoRoute,
    ) {
        compose.setContent { DemoApp(initialRoute = route) }
        compose.waitForIdle()
        save(name)
    }

    private fun captureMaterial(section: String) {
        compose.setContent { DemoApp(initialRoute = DemoRoute.Material(section)) }
        compose.waitForIdle()
        val heading = compose.onNodeWithTag("material-section-heading").assertExists().performScrollTo()
        val viewport = compose.onNodeWithTag("material-catalog-scroll")
        val distance = heading.fetchSemanticsNode().boundsInRoot.top - viewport.fetchSemanticsNode().boundsInRoot.top
        viewport.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, distance) }
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        save(
            "material-$section",
            compose.onNode(isRoot() and hasAnyDescendant(hasTestTag("material-catalog-scroll"))),
        )
    }

    private fun save(
        name: String,
        root: SemanticsNodeInteraction = compose.onRoot(),
    ) {
        val target = File(System.getProperty("domposable.screenshots"), "desktop/$name.png")
        target.parentFile.mkdirs()
        ImageIO.write(root.captureToImage().toAwtImage(), "png", target)
    }
}
