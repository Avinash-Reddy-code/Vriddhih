package com.vrddhih.app.core.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarMathTest {

    @Test
    fun october2026_startsOnThursday_withFourLeadingBlanksInSundayFirstGrid() {
        val october = YearMonth.of(2026, 10)
        assertEquals(DayOfWeek.THURSDAY, october.atDay(1).dayOfWeek)
        assertEquals(4, CalendarMath.leadingBlankDays(october))

        val firstWeek = CalendarMath.monthGrid(october).first()
        assertEquals(listOf(null, null, null, null), firstWeek.take(4))
        assertEquals(LocalDate.of(2026, 10, 1), firstWeek[4])
        assertEquals(LocalDate.of(2026, 10, 3), firstWeek[6])
    }

    @Test
    fun monthGrid_containsEveryDayOfTheMonthOnceInOrder() {
        listOf(YearMonth.of(2026, 10) to 31, YearMonth.of(2026, 4) to 30, YearMonth.of(2026, 12) to 31).forEach { (month, length) ->
            val days = CalendarMath.monthGrid(month).flatten().filterNotNull()
            assertEquals(length, days.size)
            assertEquals((1..length).map(month::atDay), days)
        }
    }

    @Test
    fun monthGrid_placesEachDayUnderItsWeekdayColumn() {
        val weekdays = CalendarMath.weekdays()
        CalendarMath.monthGrid(YearMonth.of(2026, 10)).forEach { week ->
            assertEquals(7, week.size)
            week.forEachIndexed { column, date -> if (date != null) assertEquals(weekdays[column], date.dayOfWeek) }
        }
    }

    @Test
    fun february_hasTwentyNineDaysOnlyInLeapYears() {
        assertEquals(29, februaryDays(2024))
        assertEquals(28, februaryDays(2026))
        assertEquals(29, februaryDays(2000))
        assertEquals(28, februaryDays(1900))
    }

    @Test
    fun monthGrid_usesFourToSixRowsAsNeeded() {
        assertEquals(4, CalendarMath.monthGrid(YearMonth.of(2015, 2)).size) // Feb 2015 starts on Sunday, 28 days
        assertEquals(5, CalendarMath.monthGrid(YearMonth.of(2026, 10)).size)
        assertEquals(6, CalendarMath.monthGrid(YearMonth.of(2026, 8)).size) // Aug 2026 starts on Saturday, 31 days
    }

    @Test
    fun monthGrid_padsTrailingCellsWithNull() {
        val lastWeek = CalendarMath.monthGrid(YearMonth.of(2026, 10)).last()
        assertEquals(LocalDate.of(2026, 10, 31), lastWeek[6])
        val november = CalendarMath.monthGrid(YearMonth.of(2026, 11)).last()
        assertEquals(LocalDate.of(2026, 11, 30), november[1])
        assertNull(november[2])
    }

    @Test
    fun monthNavigation_crossesDecemberAndJanuary() {
        val december = YearMonth.of(2026, 12)
        assertEquals(YearMonth.of(2027, 1), december.plusMonths(1))
        assertEquals(december, YearMonth.of(2027, 1).minusMonths(1))
        assertEquals(5, CalendarMath.leadingBlankDays(YearMonth.of(2027, 1))) // Jan 1 2027 is a Friday
    }

    @Test
    fun weekdays_areSundayFirst() {
        assertEquals(
            listOf(
                DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
            ),
            CalendarMath.weekdays(),
        )
    }

    @Test
    fun rollingWindow_isCentredOnTheSelectedDate() {
        val window = CalendarMath.rollingWindow(LocalDate.of(2026, 10, 25))
        assertEquals((22..28).map { LocalDate.of(2026, 10, it) }, window)
        assertEquals(DayOfWeek.THURSDAY, window.first().dayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, window[3].dayOfWeek)
        assertEquals(DayOfWeek.WEDNESDAY, window.last().dayOfWeek)
    }

    @Test
    fun rollingWindow_crossesMonthYearAndLeapDayBoundaries() {
        assertEquals(
            listOf(27, 28, 29, 30, 31).map { LocalDate.of(2026, 10, it) } + listOf(1, 2).map { LocalDate.of(2026, 11, it) },
            CalendarMath.rollingWindow(LocalDate.of(2026, 10, 30)),
        )
        assertEquals(
            listOf(28, 29, 30, 31).map { LocalDate.of(2026, 12, it) } + listOf(1, 2, 3).map { LocalDate.of(2027, 1, it) },
            CalendarMath.rollingWindow(LocalDate.of(2026, 12, 31)),
        )
        assertEquals(LocalDate.of(2024, 2, 29), CalendarMath.rollingWindow(LocalDate.of(2024, 3, 1))[2])
        assertEquals(LocalDate.of(2023, 3, 1), CalendarMath.rollingWindow(LocalDate.of(2023, 2, 28))[4])
    }

    @Test
    fun daysBetween_isSignedCalendarDays() {
        assertEquals(3, CalendarMath.daysBetween(LocalDate.of(2026, 12, 30), LocalDate.of(2027, 1, 2)))
        assertEquals(-366, CalendarMath.daysBetween(LocalDate.of(2024, 3, 1), LocalDate.of(2023, 3, 1))) // spans Feb 29, 2024
        assertEquals(-365, CalendarMath.daysBetween(LocalDate.of(2025, 3, 1), LocalDate.of(2024, 3, 1)))
    }

    private fun februaryDays(year: Int) = CalendarMath.monthGrid(YearMonth.of(year, 2)).flatten().count { it != null }
}
