package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.maxViewportHeight
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.toComposeModifier
import io.github.samuolis.domposable.ui.viewportHeight
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.github.samuolis.domposable.foundation.dp as portableDp

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NativeViewportModifierTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun fixedViewportHeightUsesWindowUnitsAndReservesChrome() {
        var expected = 0f
        compose.setContent {
            val density = LocalDensity.current
            val window = LocalWindowInfo.current.containerSize
            val requested = with(density) { window.height.toDp().value } / 4 - 8
            SideEffect { expected = requested.coerceAtLeast(0f) }
            Box(DomposableModifier.viewportHeight(0.25f, subtract = 8.portableDp).testId("fixed").toComposeModifier()) {
                Box(Modifier.size(12.dp))
            }
        }
        val bounds = compose.onNodeWithTag("fixed").getUnclippedBoundsInRoot()
        assertTrue(expected > 0f, "The host window must expose a measured viewport")
        assertEquals(expected, (bounds.bottom - bounds.top).value, 1f)
    }

    @Test
    fun viewportMaximumDoesNotExpandShortContent() {
        compose.setContent {
            Box(DomposableModifier.maxViewportHeight().testId("short").toComposeModifier()) {
                Box(Modifier.size(12.dp))
            }
        }
        val bounds = compose.onNodeWithTag("short").getUnclippedBoundsInRoot()
        assertEquals(12f, (bounds.bottom - bounds.top).value, 1f)
    }
}
