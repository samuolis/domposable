package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MaterialCatalogContractsTest {
    @Test
    fun optionalRadioGroupsRejectBlankNames() {
        assertFailsWith<IllegalArgumentException> {
            DomposableRadioButtonProps(false, {}, "Personal", groupName = " ")
        }
        assertEquals(
            "account-type",
            DomposableRadioButtonProps(true, {}, "Personal", groupName = "account-type").groupName,
        )
        assertEquals(null, DomposableRadioButtonProps(false, {}, "Personal").groupName)
    }

    @Test
    fun interactiveScrimAndTabsRequireAccessibleValidTargets() {
        assertFailsWith<IllegalArgumentException> { DomposableScrimProps(onClick = {}) }
        assertFailsWith<IllegalArgumentException> { DomposableScrimProps(alpha = Float.NaN) }
        assertFailsWith<IllegalArgumentException> { DomposableTabsProps(listOf("A"), 1, {}) }
        assertFailsWith<IllegalArgumentException> { DomposableTabsProps(listOf("A"), 0, {}, icons = emptyList()) }
        assertEquals("Close", DomposableScrimProps(onClick = {}, label = "Close").label)
    }

    @Test
    fun wheelSelectionAndMenuStatesRemainUnambiguous() {
        assertFailsWith<IllegalArgumentException> { DomposableScrollFieldProps(0, 0, {}, "Hours") }
        assertFailsWith<IllegalArgumentException> { DomposableScrollFieldProps(24, 24, {}, "Hours") }
        assertFailsWith<IllegalArgumentException> { DomposableMenuItem("Sort", {}, checked = true, selected = false) }
        assertEquals(23, DomposableScrollFieldProps(24, 23, {}, "Hours").selectedIndex)
    }

    @Test
    fun timeEntryRejectsInvalidClockValues() {
        assertFailsWith<IllegalArgumentException> { DomposableTimePickerProps(-1, 0, { _, _ -> }) }
        assertFailsWith<IllegalArgumentException> { DomposableTimePickerProps(24, 0, { _, _ -> }) }
        assertFailsWith<IllegalArgumentException> { DomposableTimePickerProps(12, 60, { _, _ -> }) }
        assertEquals(23, DomposableTimePickerProps(23, 59, { _, _ -> }).hour)
    }

    @Test
    fun rangePickerRejectsReversedAndOrphanedEndDates() {
        assertFailsWith<IllegalArgumentException> { DomposableDateRangePickerProps(20L, 10L, { _, _ -> }) }
        assertFailsWith<IllegalArgumentException> { DomposableDateRangePickerProps(null, 10L, { _, _ -> }) }
        assertFailsWith<IllegalArgumentException> { DomposableDatePickerProps(null, {}, yearRange = IntRange.EMPTY) }
        assertEquals(null, DomposableDateRangePickerProps(10L, null, { _, _ -> }).endDateMillis)
    }

    @Test
    fun actionCollectionsRequireUniqueStableKeys() {
        val item = DomposableActionItem("save", "Save", {})
        assertFailsWith<IllegalArgumentException> { DomposableButtonGroupProps(listOf(item, item), "Actions") }
        assertFailsWith<IllegalArgumentException> { DomposableAppBarProps(listOf(item, item), "Actions") }
        assertFailsWith<IllegalArgumentException> {
            DomposableFloatingActionButtonMenuProps(false, {}, "Actions", listOf(item, item))
        }
        assertFailsWith<IllegalArgumentException> { DomposableActionItem("", "Save", {}) }
    }

    @Test
    fun appBarCustomSlotsMustMatchUniqueActionKeys() {
        val action = DomposableActionItem("save", "Save", {})
        val content = DomposableAppBarItemContent("save", {})
        assertEquals(
            listOf(content),
            DomposableAppBarProps(listOf(action), "Actions", customItems = listOf(content)).customItems,
        )
        assertFailsWith<IllegalArgumentException> {
            DomposableAppBarProps(listOf(action), "Actions", customItems = listOf(content, content))
        }
        assertFailsWith<IllegalArgumentException> {
            DomposableAppBarProps(
                listOf(action),
                "Actions",
                customItems = listOf(DomposableAppBarItemContent("missing", {})),
            )
        }
    }

    @Test
    fun multiSelectionRejectsMissingIndicesAndMalformedInventory() {
        assertFailsWith<IllegalArgumentException> {
            DomposableMultiChoiceSegmentedButtonsProps(listOf("A"), setOf(1), {}, "Choices")
        }
        assertFailsWith<IllegalArgumentException> {
            DomposableMultiChoiceSegmentedButtonsProps(
                listOf("A"),
                setOf(0),
                {},
                "Choices",
                optionEnabled = emptyList(),
            )
        }
        assertEquals(
            emptySet(),
            DomposableMultiChoiceSegmentedButtonsProps(listOf("A"), emptySet(), {
            }, "Choices").selectedIndices,
        )
    }

    @Test
    fun gestureAndProgressGeometryRejectsUnusableValues() {
        assertFailsWith<IllegalArgumentException> { DomposablePullToRefreshProps(false, {}, threshold = 0.dp) }
        assertFailsWith<IllegalArgumentException> {
            DomposableSwipeToDismissProps({}, allowStartToEnd = false, allowEndToStart = false)
        }
        assertFailsWith<IllegalArgumentException> { DomposableLoadingIndicatorProps(progress = Float.NaN) }
        assertFailsWith<IllegalArgumentException> { DomposableLoadingIndicatorProps(progress = 1.1f) }
        assertFailsWith<IllegalArgumentException> {
            DomposableCarouselProps(emptyList(), "Items", minSmallItemWidth = 60.dp, maxSmallItemWidth = 40.dp)
        }
    }
}
