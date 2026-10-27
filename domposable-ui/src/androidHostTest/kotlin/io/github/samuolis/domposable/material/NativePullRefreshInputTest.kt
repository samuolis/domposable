package io.github.samuolis.domposable.material

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTouchInput
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposablePullToRefresh
import io.github.samuolis.domposable.ui.fillMaxSize
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w400dp-h800dp-mdpi")
class NativePullRefreshInputTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun mouseWheelScrollsContentWithoutRequestingRefresh() {
        var refreshes = 0
        var progress = 0f
        lateinit var scrollState: ScrollState
        compose.setContent {
            DomposableMaterialTheme {
                scrollState = rememberScrollState()
                DomposablePullToRefresh(
                    refreshing = false,
                    onRefresh = { refreshes++ },
                    label = "Refresh",
                    modifier = DomposableModifier.fillMaxSize().testId("pull"),
                    indicatorContent = { state -> SideEffect { progress = state.progress } },
                ) {
                    Column(Modifier.fillMaxWidth().verticalScroll(scrollState)) { repeat(80) { Text("Row $it") } }
                }
            }
        }
        compose.onNodeWithTag("pull").performMouseInput { scroll(500f) }
        compose.runOnIdle { assertTrue(scrollState.value > 0) }
        compose.onNodeWithTag("pull").performMouseInput { scroll(-5000f) }
        compose.runOnIdle {
            assertEquals(0, scrollState.value)
            assertEquals(0, refreshes)
            assertEquals(0f, progress)
        }
    }

    @Test
    fun touchPullStillRequestsRefresh() {
        var refreshes = 0
        var progress = 0f
        compose.setContent {
            DomposableMaterialTheme {
                DomposablePullToRefresh(
                    refreshing = false,
                    onRefresh = { refreshes++ },
                    label = "Refresh",
                    modifier = DomposableModifier.fillMaxSize().testId("pull"),
                    indicatorContent = { state -> SideEffect { progress = state.progress } },
                ) {
                    Column(
                        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    ) { repeat(80) { Text("Row $it") } }
                }
            }
        }
        val pull = compose.onNodeWithTag("pull")
        pull.performTouchInput { down(Offset(100f, 50f)) }
        compose.mainClock.advanceTimeByFrame()
        repeat(5) {
            pull.performTouchInput { moveBy(Offset(0f, 100f), 32) }
            compose.mainClock.advanceTimeByFrame()
        }
        pull.performTouchInput { up() }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(1, refreshes) }
        pull.performMouseInput { scroll(-5000f) }
        compose.runOnIdle {
            assertEquals(1, refreshes)
            assertEquals(0f, progress)
        }
    }
}
