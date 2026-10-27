package io.github.samuolis.domposable.material

import kotlinx.browser.window
import kotlin.js.Date
import kotlin.js.dateLocaleOptions
import kotlin.js.jsTypeOf

private const val REFERENCE_SUNDAY_MILLIS = 1672531200000L
private const val DAYS_PER_WEEK = 7
private const val CALENDAR_DAY_MILLIS = 86_400_000L

internal fun calendarFirstWeekday(locale: String = window.navigator.language): Int {
    val info = js("(tag) => new Intl.Locale(tag)")(locale)
    val week = if (jsTypeOf(info.getWeekInfo) == "function") info.getWeekInfo() else info.weekInfo
    // shortcut: use ISO Monday on older hosts without locale week metadata; add a polyfill if required.
    return if (week == null) 1 else (week.firstDay as Int) % DAYS_PER_WEEK
}

internal fun calendarWeekdayLabel(
    index: Int,
    narrow: Boolean,
): String =
    Date((REFERENCE_SUNDAY_MILLIS + index * CALENDAR_DAY_MILLIS).toDouble()).toLocaleDateString(
        options =
            dateLocaleOptions {
                weekday = if (narrow) "narrow" else "long"
                timeZone = "UTC"
            },
    )
