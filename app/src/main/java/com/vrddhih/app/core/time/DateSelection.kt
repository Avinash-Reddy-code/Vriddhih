package com.vrddhih.app.core.time

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

/** Immutable date context for a screen: the device's current day and the day the user is viewing. */
data class DateSelectionState(
    val today: LocalDate,
    val selectedDate: LocalDate,
) {
    val isTodaySelected: Boolean get() = selectedDate == today
    val visibleWeek: List<LocalDate> get() = CalendarMath.rollingWindow(selectedDate)
    val selectedMonth: YearMonth get() = YearMonth.from(selectedDate)

    /** Days from today to the selected date (negative for past days). */
    val offsetFromToday: Long get() = CalendarMath.daysBetween(today, selectedDate)
}

/**
 * Single source of truth for a selected calendar day. Each ViewModel that needs a date filter owns its
 * own instance, so screens can share the pattern without being forced to share one date.
 *
 * [currentDate] is read each time "today" is needed. The default, [LocalDate.now], uses the device's
 * current time zone, so a time-zone or DST change is picked up on the next [refreshToday].
 */
class DateSelectionStateHolder(
    private val currentDate: () -> LocalDate = { LocalDate.now() },
) {
    private val _state = MutableStateFlow(currentDate().let { DateSelectionState(today = it, selectedDate = it) })
    val state: StateFlow<DateSelectionState> = _state.asStateFlow()

    fun select(date: LocalDate) {
        _state.update { it.copy(selectedDate = date) }
    }

    fun shiftWindow(direction: Int) {
        _state.update { it.copy(selectedDate = it.selectedDate.plusDays(direction * 7L)) }
    }

    fun selectToday() {
        val today = currentDate()
        _state.value = DateSelectionState(today = today, selectedDate = today)
    }

    /**
     * Re-reads the current date, for example after the app resumes past midnight. If the user was
     * viewing today, the selection moves to the new today; otherwise their chosen day is kept.
     */
    fun refreshToday() {
        val today = currentDate()
        _state.update { state ->
            when {
                state.today == today -> state
                state.isTodaySelected -> DateSelectionState(today = today, selectedDate = today)
                else -> state.copy(today = today)
            }
        }
    }
}
