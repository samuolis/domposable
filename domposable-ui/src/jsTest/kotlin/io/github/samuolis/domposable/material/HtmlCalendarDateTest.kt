package io.github.samuolis.domposable.material

import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlCalendarDateTest {
    @Test
    fun earlyYearsStayInTheirCenturyWhenMovingBetweenMonthsAndDays() {
        val date = dateToEpoch("0001-02-01")
        assertEquals("0001-02-01", epochToDate(monthStart(date)))
        assertEquals("0001-03-01", epochToDate(shiftMonth(date, 1)))
        assertEquals("0001-01-31", epochToDate(shiftDay(date, -1)))
        assertEquals(1, utcYear(date))
    }

    @Test
    fun movingAMonthClampsLeapDaysAndKeepsUtcDates() {
        val leapDay = dateToEpoch("2000-02-29")
        assertEquals(29, daysInMonth(leapDay))
        assertEquals("2000-03-29", epochToDate(shiftMonthDay(leapDay, 1)))
        assertEquals("2000-02-29", epochToDate(shiftMonthDay(dateToEpoch("2000-01-31"), 1)))
        assertEquals("1970-01-01", epochToDate(0L))
        assertEquals(0L, dateToEpoch("1970-01-01"))
    }
}
