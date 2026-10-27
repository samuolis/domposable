package io.github.samuolis.domposable.material

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlCalendarLocaleTest {
    @Test
    fun weekStartsFollowTheCalendarLocale() {
        assertEquals(0, calendarFirstWeekday("en-US"))
        assertEquals(1, calendarFirstWeekday("lt-LT"))
        assertEquals(1, calendarFirstWeekday("en-GB"))
        assertEquals(6, calendarFirstWeekday("ar-AF"))
        for (day in 0..6) {
            assertTrue(calendarWeekdayLabel(day, narrow = true).isNotBlank())
            assertTrue(calendarWeekdayLabel(day, narrow = false).isNotBlank())
        }
    }
}
