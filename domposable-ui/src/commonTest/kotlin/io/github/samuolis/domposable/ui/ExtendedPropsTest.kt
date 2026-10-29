package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExtendedPropsTest {
    @Test fun controlledTooltipsRequireADismissHandler() {
        assertEquals(null, DomposableTooltipProps("Action label").visible)
        assertFailsWith<IllegalArgumentException> { DomposableTooltipProps("Help", visible = true) }
        assertEquals(true, DomposableTooltipProps("Help", visible = true, onDismiss = {}).visible)
    }

    @Test fun selectUsesUniqueStableValuesAndRejectsAnUnknownSelection() {
        val options = listOf(DomposableSelectOption("home", "Home"), DomposableSelectOption("work", "Office"))
        val props = DomposableSelectProps("work", {}, "Address", options.reversed())
        assertEquals("work", props.value)
        assertFailsWith<IllegalArgumentException> { props.copy(value = "missing") }
        assertFailsWith<IllegalArgumentException> { props.copy(options = options + options.first()) }
        assertFailsWith<IllegalArgumentException> { props.copy(options = emptyList()) }
    }

    @Test fun segmentSelectionUsesAnExistingChoiceOrExplicitlyHasNoSelection() {
        val props = DomposableSegmentedButtonsProps(listOf("Home", "Office"), 1, {}, "Address")
        assertEquals(1, props.selectedIndex)
        assertEquals(false, props.equalWidth)
        assertEquals(true, props.copy(equalWidth = true).equalWidth)
        assertEquals(-1, props.copy(selectedIndex = -1).selectedIndex)
        assertFailsWith<IllegalArgumentException> { props.copy(selectedIndex = -2) }
        assertFailsWith<IllegalArgumentException> { props.copy(selectedIndex = 2) }
        assertFailsWith<IllegalArgumentException> { props.copy(labels = emptyList()) }
    }

    @Test fun segmentedButtonsKeepTheDefaultIconUnlessACustomSlotIsProvided() {
        val props = DomposableSegmentedButtonsProps(listOf("Home"), 0, {}, "Address")
        assertEquals(null, props.selectedIcon)
        val customIcon: @Composable () -> Unit = {}
        assertEquals(customIcon, props.copy(selectedIcon = customIcon).selectedIcon)
    }

    @Test fun rangeCannotCrossEscapeItsBoundsOrContainNonFiniteValues() {
        val props = DomposableRangeSliderProps(0.2f..0.8f, {}, "Minimum", "Maximum")
        assertEquals(0.2f, props.value.start)
        assertFailsWith<IllegalArgumentException> { props.copy(value = 0.8f..0.2f) }
        assertFailsWith<IllegalArgumentException> { props.copy(value = -0.1f..0.8f) }
        assertFailsWith<IllegalArgumentException> { props.copy(value = 0.2f..1.1f) }
        assertFailsWith<IllegalArgumentException> { props.copy(value = Float.NaN..0.8f) }
        assertFailsWith<IllegalArgumentException> { props.copy(value = 0.2f..Float.POSITIVE_INFINITY) }
        assertFailsWith<IllegalArgumentException> { props.copy(range = 1f..1f) }
        assertFailsWith<IllegalArgumentException> { props.copy(range = Float.NEGATIVE_INFINITY..1f) }
        assertFailsWith<IllegalArgumentException> { props.copy(range = 0f..Float.NaN) }
        assertFailsWith<IllegalArgumentException> { props.copy(steps = -1) }
    }

    @Test fun snackbarActionsNeedBothALabelAndAHandler() {
        val props = DomposableSnackbarProps("Saved", "Undo", {})
        assertEquals("Undo", props.actionLabel)
        assertFailsWith<IllegalArgumentException> { props.copy(onAction = null) }
        assertFailsWith<IllegalArgumentException> { props.copy(actionLabel = null) }
    }
}
