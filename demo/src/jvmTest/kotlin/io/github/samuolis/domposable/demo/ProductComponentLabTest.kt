package io.github.samuolis.domposable.demo

import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

class ProductComponentLabTest {
    @get:Rule val compose = createComposeRule()

    @Test fun formChoiceDisclosureChartAndTableActionsReachTheirFeatureState() {
        compose.setContent { DomposableMaterialTheme { ProductComponentLab() } }

        compose.onNodeWithText("Save preferences").assertIsNotEnabled()
        compose.onNodeWithTag("product-email").performTextInput("buyer@example.com")
        compose.onNodeWithText("Same day").assertIsNotEnabled()
        compose.onNodeWithText("Standard").assertIsSelected()
        compose
            .onNodeWithText("Save preferences")
            .assertIsEnabled()
            .performScrollTo()
            .performClick()
        compose.onNodeWithTag("product-form-status").assertTextEquals("Saved preferences for buyer@example.com")

        compose.onNodeWithText("Shipping details").performScrollTo().performClick()
        compose.onNodeWithText("Free shipping", substring = true).assertExists()
        compose.onNodeWithText("Shipping details").performClick()
        compose.onNodeWithText("Free shipping", substring = true).assertDoesNotExist()

        val chart =
            compose
                .onNodeWithContentDescription(
                    "Orders this week. Select a day with touch, pointer or arrow keys",
                ).performScrollTo()
        chart.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        chart.performKeyInput {
            keyDown(Key.DirectionRight)
            keyUp(Key.DirectionRight)
        }
        compose.onNodeWithTag("product-chart-status").assertTextEquals("Selected day 1")

        compose.onNodeWithTag("product-orders").performScrollTo()
        compose.onNodeWithText("A-101").assertIsDisplayed().performClick()
        compose.onNodeWithTag("product-row-status").assertTextEquals("Opened order A-101")
    }

    @Test fun capturesProductComponentsCandidate() {
        compose.setContent { DemoApp(initialRoute = DemoRoute.ProductComponents) }
        compose.waitForIdle()
        val target = File(System.getProperty("domposable.screenshots"), "desktop/product-components.png")
        target.parentFile.mkdirs()
        ImageIO.write(compose.onRoot().captureToImage().toAwtImage(), "png", target)
    }
}
