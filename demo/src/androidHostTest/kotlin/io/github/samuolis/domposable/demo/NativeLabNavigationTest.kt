package io.github.samuolis.domposable.demo

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NativeLabNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun componentMenuReachesTheSemanticTableThroughNativeControls() {
        compose.setContent { DemoApp(initialRoute = DemoRoute.ExtendedCatalog) }
        compose.onNodeWithText("Large collections lab").performScrollTo().performClick()
        compose.onNodeWithText("More collection recipes").performScrollTo().performClick()
        compose.onNodeWithText("Collection recipes").assertExists()
        compose.onNodeWithText("Collection layout: Measured column").performScrollTo().performClick()
        compose.onNodeWithText("Semantic table").performClick()
        compose.onNodeWithText("Inventory drafts").assertExists()
    }
}
