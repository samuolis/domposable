package io.github.samuolis.domposable.material

import kotlin.js.Date
import kotlin.js.dateLocaleOptions

private const val ISO_DATE_LENGTH = 10

internal fun monthLabel(millis: Long): String =
    Date(millis.toDouble()).toLocaleDateString(
        options =
            dateLocaleOptions {
                month = "long"
                year = "numeric"
                timeZone = "UTC"
            },
    )

internal fun dateLabel(millis: Long): String =
    Date(millis.toDouble()).toLocaleDateString(
        options =
            dateLocaleOptions {
                weekday = "long"
                year = "numeric"
                month = "long"
                day = "numeric"
                timeZone = "UTC"
            },
    )

internal fun epochToDate(millis: Long): String = Date(millis.toDouble()).toISOString().substring(0, ISO_DATE_LENGTH)

internal fun dateToEpoch(value: String): Long = Date.parse("${value}T00:00:00Z").toLong()

internal fun shortDateLabel(millis: Long): String =
    Date(millis.toDouble()).toLocaleDateString(
        options =
            dateLocaleOptions {
                month = "short"
                day = "numeric"
                year = "numeric"
                timeZone = "UTC"
            },
    )
