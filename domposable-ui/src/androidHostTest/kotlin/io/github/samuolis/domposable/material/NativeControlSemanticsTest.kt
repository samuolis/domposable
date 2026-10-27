package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableActionItem
import io.github.samuolis.domposable.ui.DomposableAppBar
import io.github.samuolis.domposable.ui.DomposableAppBarItemContent
import io.github.samuolis.domposable.ui.DomposableCheckbox
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRadioButton
import io.github.samuolis.domposable.ui.DomposableRangeSlider
import io.github.samuolis.domposable.ui.DomposableSlider
import io.github.samuolis.domposable.ui.DomposableSwitch
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import androidx.compose.ui.unit.dp as composeDp

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NativeControlSemanticsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun passiveCheckboxMergesIntoItsLabeledToggle() {
        compose.setContent {
            DomposableMaterialTheme {
                Row(Modifier.toggleable(false, role = Role.Checkbox, onValueChange = {})) {
                    DomposableCheckbox(checked = false, onCheckedChange = null, showLabel = false)
                }
            }
        }
        compose.onAllNodes(isToggleable()).assertCountEquals(1)
    }

    @Test
    fun disabledSlidersRejectAccessibilityUpdatesAfterBeingEnabled() {
        val enabled = mutableStateOf(true)
        val changes = mutableListOf<Float>()
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    DomposableOrientation.entries.forEach { orientation ->
                        DomposableSlider(
                            value = 10f,
                            onValueChange = { changes += it },
                            label = orientation.name,
                            range = 10f..30f,
                            steps = 3,
                            enabled = enabled.value,
                            orientation = orientation,
                            modifier = DomposableModifier.testId(orientation.name),
                        )
                    }
                }
            }
        }
        DomposableOrientation.entries.forEach { orientation ->
            compose.onNodeWithTag(orientation.name).performSemanticsAction(SemanticsActions.SetProgress) { it(19f) }
        }
        compose.runOnIdle {
            assertEquals(listOf(20f, 20f), changes)
            enabled.value = false
        }
        DomposableOrientation.entries.forEach { orientation ->
            compose
                .onNodeWithTag(orientation.name)
                .assertIsNotEnabled()
                .performSemanticsAction(SemanticsActions.SetProgress) { it(26f) }
        }
        compose.runOnIdle { assertEquals(listOf(20f, 20f), changes) }
    }

    @Test
    fun disabledSelectionControlsRejectAccessibilityClicks() {
        var changes = 0
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    DomposableCheckbox(
                        false,
                        { changes++ },
                        enabled = false,
                        modifier = DomposableModifier.testId("check"),
                    )
                    DomposableSwitch(
                        false,
                        { changes++ },
                        enabled = false,
                        modifier = DomposableModifier.testId("switch"),
                    )
                    DomposableCheckbox(
                        false,
                        { changes++ },
                        enabled = false,
                        indeterminate = true,
                        modifier = DomposableModifier.testId("mixed"),
                    )
                    DomposableRadioButton(
                        false,
                        { changes++ },
                        enabled = false,
                        modifier = DomposableModifier.testId("radio"),
                    )
                }
            }
        }
        listOf("check", "switch", "mixed", "radio").forEach { tag ->
            compose
                .onNodeWithTag(tag)
                .assertIsNotEnabled()
                .performSemanticsAction(SemanticsActions.OnClick) { it() }
        }
        compose.runOnIdle { assertEquals(0, changes) }
    }

    @Test
    fun disabledRangeSliderRejectsAccessibilityUpdatesOnBothThumbs() {
        var changes = 0
        compose.setContent {
            DomposableMaterialTheme {
                DomposableRangeSlider(2f..6f, { changes++ }, "Start", "End", range = 0f..10f, enabled = false)
            }
        }
        val thumbs = compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
        thumbs.assertCountEquals(2)
        thumbs[0].performSemanticsAction(SemanticsActions.SetProgress) { it(3f) }
        thumbs[1].performSemanticsAction(SemanticsActions.SetProgress) { it(8f) }
        compose.runOnIdle { assertEquals(0, changes) }
    }

    @Test
    fun appBarSpacingSeparatesItemsWithoutATrailingGap() {
        compose.setContent {
            DomposableMaterialTheme {
                Column {
                    DomposableOrientation.entries.forEach { orientation ->
                        DomposableAppBar(
                            items =
                                listOf(
                                    DomposableActionItem("first", "First", {}),
                                    DomposableActionItem("last", "Last", {}),
                                ),
                            label = "Actions",
                            orientation = orientation,
                            spacing = 8.dp,
                            modifier = DomposableModifier.testId("bar-$orientation"),
                            customItems =
                                listOf("first", "last").map { key ->
                                    DomposableAppBarItemContent(key, {
                                        Box(Modifier.size(24.composeDp).testTag("$orientation-$key"))
                                    })
                                },
                        )
                    }
                }
            }
        }
        DomposableOrientation.entries.forEach { orientation ->
            val bar = compose.onNodeWithTag("bar-$orientation").getUnclippedBoundsInRoot()
            val first = compose.onNodeWithTag("$orientation-first").getUnclippedBoundsInRoot()
            val last = compose.onNodeWithTag("$orientation-last").getUnclippedBoundsInRoot()
            if (orientation == DomposableOrientation.Horizontal) {
                assertEquals(bar.left, first.left)
                assertEquals(8.composeDp, last.left - first.right)
                assertEquals(bar.right, last.right)
            } else {
                assertEquals(bar.top, first.top)
                assertEquals(8.composeDp, last.top - first.bottom)
                assertEquals(bar.bottom, last.bottom)
            }
        }
    }
}
