package com.vrddhih.app.core.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateSelectionStateHolderTest {

    private var deviceDate = LocalDate.of(2026, 10, 25)
    private val holder = DateSelectionStateHolder { deviceDate }

    @Test
    fun initialState_selectsToday() {
        val state = holder.state.value
        assertEquals(deviceDate, state.today)
        assertEquals(deviceDate, state.selectedDate)
        assertTrue(state.isTodaySelected)
        assertEquals(0, state.offsetFromToday)
    }

    @Test
    fun select_changesSelectedDateButKeepsToday() {
        val past = LocalDate.of(2026, 10, 22)
        holder.select(past)

        val state = holder.state.value
        assertEquals(past, state.selectedDate)
        assertEquals(deviceDate, state.today)
        assertFalse(state.isTodaySelected)
        assertEquals(-3, state.offsetFromToday)
        assertEquals((19..25).map { LocalDate.of(2026, 10, it) }, state.visibleWeek)
    }

    @Test
    fun selectToday_returnsToTheCurrentDeviceDate() {
        holder.select(LocalDate.of(2025, 1, 1))
        deviceDate = LocalDate.of(2026, 10, 26)

        holder.selectToday()

        assertEquals(DateSelectionState(today = deviceDate, selectedDate = deviceDate), holder.state.value)
    }

    @Test
    fun refreshToday_movesSelectionWhenTodayWasSelected() {
        deviceDate = LocalDate.of(2026, 10, 26)
        holder.refreshToday()

        assertEquals(DateSelectionState(today = deviceDate, selectedDate = deviceDate), holder.state.value)
    }

    @Test
    fun refreshToday_keepsAnExplicitlyChosenDay() {
        val chosen = LocalDate.of(2026, 11, 3)
        holder.select(chosen)
        deviceDate = LocalDate.of(2026, 10, 26)

        holder.refreshToday()

        assertEquals(DateSelectionState(today = deviceDate, selectedDate = chosen), holder.state.value)
    }

    @Test
    fun selectedMonth_followsTheSelectedDate() {
        holder.select(LocalDate.of(2027, 1, 2))
        assertEquals(java.time.YearMonth.of(2027, 1), holder.state.value.selectedMonth)
    }

    @Test
    fun shiftWindow_movesForwardByOneWeek() {
        // Today is 2026-10-25
        holder.shiftWindow(1)
        assertEquals(LocalDate.of(2026, 11, 1), holder.state.value.selectedDate)
        assertEquals(deviceDate, holder.state.value.today)
        assertFalse(holder.state.value.isTodaySelected)
    }

    @Test
    fun shiftWindow_movesBackwardByOneWeekAcrossMonthAndYearBoundaries() {
        holder.select(LocalDate.of(2027, 1, 3))
        holder.shiftWindow(-1)
        assertEquals(LocalDate.of(2026, 12, 27), holder.state.value.selectedDate)
        assertEquals(deviceDate, holder.state.value.today)
    }
}
