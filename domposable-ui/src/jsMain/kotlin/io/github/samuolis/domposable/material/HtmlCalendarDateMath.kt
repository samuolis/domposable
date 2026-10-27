package io.github.samuolis.domposable.material

import kotlin.js.Date

internal const val MILLIS_PER_DAY = 86_400_000L

private fun utcDate(
    year: Int,
    month: Int,
    day: Int,
): Double = Date(0).asDynamic().setUTCFullYear(year, month, day) as Double

internal fun monthStart(millis: Long): Long {
    val date = Date(millis.toDouble())
    return utcDate(date.getUTCFullYear(), date.getUTCMonth(), 1).toLong()
}

internal fun currentDayMillis(): Long {
    val date = Date()
    return utcDate(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate()).toLong()
}

internal fun shiftMonth(
    millis: Long,
    offset: Int,
): Long {
    val date = Date(millis.toDouble())
    return utcDate(date.getUTCFullYear(), date.getUTCMonth() + offset, 1).toLong()
}

internal fun shiftDay(
    millis: Long,
    offset: Int,
): Long {
    val date = Date(millis.toDouble())
    return utcDate(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate() + offset).toLong()
}

internal fun shiftMonthDay(
    millis: Long,
    offset: Int,
): Long {
    val currentMonth = monthStart(millis)
    val day = ((millis - currentMonth) / MILLIS_PER_DAY + 1).toInt()
    val nextMonth = shiftMonth(millis, offset)
    return nextMonth + (minOf(day, daysInMonth(nextMonth)) - 1) * MILLIS_PER_DAY
}

internal fun daysInMonth(millis: Long): Int {
    val date = Date(millis.toDouble())
    return Date(utcDate(date.getUTCFullYear(), date.getUTCMonth() + 1, 0)).getUTCDate()
}

internal fun utcWeekday(millis: Long): Int = Date(millis.toDouble()).getUTCDay()

internal fun utcYear(millis: Long): Int = Date(millis.toDouble()).getUTCFullYear()
