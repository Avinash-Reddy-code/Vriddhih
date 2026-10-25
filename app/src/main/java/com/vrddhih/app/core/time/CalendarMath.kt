package com.vrddhih.app.core.time

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/** Pure calendar-day calculations on [LocalDate]/[YearMonth]. No time zones are involved. */
object CalendarMath {
    const val ROLLING_WINDOW_RADIUS = 3
    const val DAYS_PER_WEEK = 7

    /** Days from `center - radius` to `center + radius`, centred on [center]. */
    fun rollingWindow(center: LocalDate, radius: Int = ROLLING_WINDOW_RADIUS): List<LocalDate> =
        (-radius..radius).map { offset -> center.plusDays(offset.toLong()) }

    /** The seven weekdays in display order, starting at [firstDayOfWeek]. */
    fun weekdays(firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY): List<DayOfWeek> =
        (0 until DAYS_PER_WEEK).map { firstDayOfWeek.plus(it.toLong()) }

    /** Number of empty cells before day 1 of [month] in a grid that starts on [firstDayOfWeek]. */
    fun leadingBlankDays(month: YearMonth, firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY): Int =
        Math.floorMod(month.atDay(1).dayOfWeek.value - firstDayOfWeek.value, DAYS_PER_WEEK)

    /**
     * Week rows for [month]. Cells outside the month are null, so only real days of the month can be
     * selected. Produces four to six rows depending on the month.
     */
    fun monthGrid(month: YearMonth, firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY): List<List<LocalDate?>> {
        val leading = List(leadingBlankDays(month, firstDayOfWeek)) { null }
        val days = (1..month.lengthOfMonth()).map(month::atDay)
        val cells: List<LocalDate?> = leading + days
        val trailing = List(Math.floorMod(-cells.size, DAYS_PER_WEEK)) { null }
        return (cells + trailing).chunked(DAYS_PER_WEEK)
    }

    /** Calendar days from [from] to [to]; negative when [to] is earlier. */
    fun daysBetween(from: LocalDate, to: LocalDate): Long = ChronoUnit.DAYS.between(from, to)
}
