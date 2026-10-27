package com.vrddhih.app.core.data.habit

import com.vrddhih.app.core.data.local.entity.HabitEntity
import com.vrddhih.app.core.data.local.entity.HabitScheduleType
import java.time.DayOfWeek
import java.time.LocalDate

data class HabitScheduleEvaluation(
    val isScheduled: Boolean,
    val isExcluded: Boolean,
    val isWithinPeriod: Boolean,
    val daysUntilNext: Int? = null,
)

object HabitScheduler {

    fun isScheduledOn(habit: HabitEntity, date: LocalDate, excludedDates: List<LocalDate> = emptyList()): Boolean {
        if (!isWithinPeriod(habit, date)) return false
        if (date in excludedDates) return false

        return when (habit.scheduleType) {
            HabitScheduleType.DAILY -> true
            HabitScheduleType.EVERY_N_DAYS -> isEveryNDaysScheduled(habit, date)
            HabitScheduleType.WEEKLY_FREQUENCY -> isWeeklyFrequencyScheduled(habit, date)
            HabitScheduleType.SELECTED_WEEKDAYS -> isSelectedWeekdayScheduled(habit, date)
        }
    }

    fun evaluateSchedule(habit: HabitEntity, date: LocalDate, excludedDates: List<LocalDate> = emptyList()): HabitScheduleEvaluation {
        val isWithinPeriod = isWithinPeriod(habit, date)
        val isExcluded = date in excludedDates
        val isScheduled = isWithinPeriod && !isExcluded && when (habit.scheduleType) {
            HabitScheduleType.DAILY -> true
            HabitScheduleType.EVERY_N_DAYS -> isEveryNDaysScheduled(habit, date)
            HabitScheduleType.WEEKLY_FREQUENCY -> isWeeklyFrequencyScheduled(habit, date)
            HabitScheduleType.SELECTED_WEEKDAYS -> isSelectedWeekdayScheduled(habit, date)
        }

        val daysUntilNext = if (isScheduled || !isWithinPeriod) null else calculateDaysUntilNext(habit, date)

        return HabitScheduleEvaluation(
            isScheduled = isScheduled,
            isExcluded = isExcluded,
            isWithinPeriod = isWithinPeriod,
            daysUntilNext = daysUntilNext,
        )
    }

    private fun isWithinPeriod(habit: HabitEntity, date: LocalDate): Boolean {
        if (date.isBefore(habit.startDate)) return false
        if (habit.endDate != null && date.isAfter(habit.endDate)) return false
        return true
    }

    private fun isEveryNDaysScheduled(habit: HabitEntity, date: LocalDate): Boolean {
        val interval = habit.scheduleInterval
        if (interval <= 0) return false

        val daysSinceStart = habit.startDate.until(date).days
        return daysSinceStart % interval == 0
    }

    private fun isWeeklyFrequencyScheduled(habit: HabitEntity, date: LocalDate): Boolean {
        val weeklyTarget = habit.scheduleInterval
        if (weeklyTarget <= 0 || weeklyTarget > 7) return false

        val weekStart = getWeekStart(date)
        val weekEnd = weekStart.plusDays(6)

        if (!isWithinPeriod(habit, date)) return false

        val habitStartInWeek = maxOf(habit.startDate, weekStart)
        val habitEndInWeek = habit.endDate?.let { minOf(it, weekEnd) } ?: weekEnd

        val daysInPeriod = habitStartInWeek.until(habitEndInWeek).days + 1
        if (daysInPeriod <= 0) return false

        val dayOfWeek = date.dayOfWeek
        val dayIndex = (dayOfWeek.value - 1)

        val progress = habit.startDate.until(date).days
        val weekNumber = progress / 7

        val previousCompletionsInWeek = (0 until dayIndex).count { day ->
            val checkDate = date.minusDays(dayIndex - day.toLong())
            isWithinPeriod(habit, checkDate)
        }

        return previousCompletionsInWeek < weeklyTarget
    }

    private fun isSelectedWeekdayScheduled(habit: HabitEntity, date: LocalDate): Boolean {
        val dayOfWeek = date.dayOfWeek
        val maskBit = 1 shl (dayOfWeek.value - 1)
        return (habit.scheduleDaysMask and maskBit) != 0
    }

    private fun calculateDaysUntilNext(habit: HabitEntity, date: LocalDate): Int? {
        if (!isWithinPeriod(habit, date)) return null

        return when (habit.scheduleType) {
            HabitScheduleType.DAILY -> 1
            HabitScheduleType.EVERY_N_DAYS -> {
                val interval = habit.scheduleInterval
                if (interval <= 0) return null
                val daysSinceStart = habit.startDate.until(date).days
                val remainder = daysSinceStart % interval
                if (remainder == 0) interval else interval - remainder
            }
            HabitScheduleType.SELECTED_WEEKDAYS -> {
                val currentDay = date.dayOfWeek
                val remainingDays = (currentDay.value..6).map { DayOfWeek.of(it) }
                remainingDays.firstOrNull { day ->
                    val maskBit = 1 shl (day.value - 1)
                    (habit.scheduleDaysMask and maskBit) != 0
                }?.let { it.value - currentDay.value } ?: 7
            }
            HabitScheduleType.WEEKLY_FREQUENCY -> {
                1
            }
        }
    }

    fun getWeekStart(date: LocalDate): LocalDate {
        return date.minusDays((date.dayOfWeek.value - 1).toLong())
    }

    fun getWeekDates(date: LocalDate): List<LocalDate> {
        val weekStart = getWeekStart(date)
        return (0..6).map { weekStart.plusDays(it.toLong()) }
    }

    fun getScheduledDatesInRange(habit: HabitEntity, start: LocalDate, end: LocalDate, excludedDates: List<LocalDate> = emptyList()): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        var current = maxOf(start, habit.startDate)

        while (!current.isAfter(end) && (habit.endDate == null || !current.isAfter(habit.endDate))) {
            if (current !in excludedDates && isScheduledOn(habit, current, excludedDates)) {
                result.add(current)
            }
            current = current.plusDays(1)
        }

        return result
    }
}
