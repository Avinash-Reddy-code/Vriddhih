package com.vrddhih.app.core.data.habit

import com.vrddhih.app.core.data.habit.HabitCompletionTracker
import com.vrddhih.app.core.data.habit.HabitScheduler
import com.vrddhih.app.core.data.local.entity.HabitChecklistProgress
import com.vrddhih.app.core.data.local.entity.HabitCompletionState
import com.vrddhih.app.core.data.local.entity.HabitDailyProgress
import com.vrddhih.app.core.data.local.entity.HabitEntity
import com.vrddhih.app.core.data.local.entity.HabitLimitConfig
import com.vrddhih.app.core.data.local.entity.HabitNumericConfig
import com.vrddhih.app.core.data.local.entity.HabitTimerConfig
import java.time.LocalDate

data class HabitStreak(
    val current: Int,
    val best: Int,
)

data class HabitStatistics(
    val completionRate: Float,
    val streak: HabitStreak,
    val totalSessions: Int,
    val totalTimeMinutes: Int = 0,
    val numericTotal: Long = 0,
    val numericAverage: Float = 0f,
    val checklistCompletionRate: Float = 0f,
    val dayOfWeekPattern: Map<Int, Int> = emptyMap(),
)

object HabitAnalytics {

    fun calculateStatistics(
        habit: HabitEntity,
        progressList: List<HabitDailyProgress>,
        checklistProgressMap: Map<LocalDate, List<HabitChecklistProgress>> = emptyMap(),
        numericConfig: HabitNumericConfig? = null,
        timerConfig: HabitTimerConfig? = null,
        limitConfig: HabitLimitConfig? = null,
        checklistItemCount: Int = 0,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): HabitStatistics {
        val filteredProgress = progressList.filter { progress ->
            (startDate == null || !progress.date.isBefore(startDate)) &&
                (endDate == null || !progress.date.isAfter(endDate))
        }

        val scheduledDates = HabitScheduler.getScheduledDatesInRange(
            habit,
            startDate ?: habit.startDate,
            endDate ?: LocalDate.now(),
        )

        val completionRate = if (scheduledDates.isEmpty()) {
            0f
        } else {
            val completedCount = filteredProgress.count { progress ->
                progress.date in scheduledDates &&
                    progress.completionState == HabitCompletionState.COMPLETED
            }
            completedCount.toFloat() / scheduledDates.size.toFloat()
        }

        val streak = calculateStreak(habit, progressList, scheduledDates)

        val totalSessions = filteredProgress.size
        val totalTimeMinutes = filteredProgress.sumOf { it.timerSeconds ?: 0 } / 60
        val numericTotal = filteredProgress.sumOf { it.numericValue ?: 0L }
        val numericAverage = if (totalSessions > 0) {
            numericTotal.toFloat() / totalSessions.toFloat()
        } else {
            0f
        }

        val checklistCompletionRate = if (checklistItemCount > 0 && filteredProgress.isNotEmpty()) {
            val totalItems = filteredProgress.size * checklistItemCount
            val completedItems = filteredProgress.sumOf { progress ->
                checklistProgressMap[progress.date]?.count { it.completedAt != null } ?: 0
            }
            completedItems.toFloat() / totalItems.toFloat()
        } else {
            0f
        }

        val dayOfWeekPattern = calculateDayOfWeekPattern(habit, progressList, scheduledDates)

        return HabitStatistics(
            completionRate = completionRate,
            streak = streak,
            totalSessions = totalSessions,
            totalTimeMinutes = totalTimeMinutes,
            numericTotal = numericTotal,
            numericAverage = numericAverage,
            checklistCompletionRate = checklistCompletionRate,
            dayOfWeekPattern = dayOfWeekPattern,
        )
    }

    fun calculateStreak(
        habit: HabitEntity,
        progressList: List<HabitDailyProgress>,
        scheduledDates: List<LocalDate>,
    ): HabitStreak {
        val today = LocalDate.now()
        val scheduledSet = scheduledDates.toSet()

        val sortedProgress = progressList
            .filter { it.date in scheduledSet }
            .sortedByDescending { it.date }

        var currentStreak = 0
        var bestStreak = 0
        var tempStreak = 0
        var previousDate: LocalDate? = null

        for (progress in sortedProgress) {
            if (progress.completionState == HabitCompletionState.COMPLETED) {
                if (previousDate == null) {
                    tempStreak = 1
                } else {
                    val daysDiff = previousDate.until(progress.date).days
                    if (daysDiff == 1) {
                        tempStreak++
                    } else {
                        bestStreak = maxOf(bestStreak, tempStreak)
                        tempStreak = 1
                    }
                }
                if (progress.date == today || progress.date == today.minusDays(1)) {
                    currentStreak = tempStreak
                }
                previousDate = progress.date
            } else {
                bestStreak = maxOf(bestStreak, tempStreak)
                tempStreak = 0
                previousDate = null
            }
        }

        bestStreak = maxOf(bestStreak, tempStreak)

        return HabitStreak(current = currentStreak, best = bestStreak)
    }

    fun calculateDayOfWeekPattern(
        habit: HabitEntity,
        progressList: List<HabitDailyProgress>,
        scheduledDates: List<LocalDate>,
    ): Map<Int, Int> {
        val pattern = mutableMapOf<Int, Int>()

        for (date in scheduledDates) {
            val dayOfWeek = date.dayOfWeek.value
            val progress = progressList.find { it.date == date }

            if (progress != null && progress.completionState == HabitCompletionState.COMPLETED) {
                pattern[dayOfWeek] = pattern.getOrDefault(dayOfWeek, 0) + 1
            } else {
                pattern[dayOfWeek] = pattern.getOrDefault(dayOfWeek, 0)
            }
        }

        return pattern
    }

    fun getWeeklyTrend(
        habit: HabitEntity,
        progressList: List<HabitDailyProgress>,
        weeksBack: Int = 4,
    ): List<Float> {
        val today = LocalDate.now()
        val trends = mutableListOf<Float>()

        for (week in 0 until weeksBack) {
            val weekStart = today.minusWeeks((week + 1).toLong())
            val weekEnd = weekStart.plusDays(6)

            val weekProgress = progressList.filter { progress ->
                !progress.date.isBefore(weekStart) && !progress.date.isAfter(weekEnd)
            }

            val scheduledInWeek = HabitScheduler.getScheduledDatesInRange(habit, weekStart, weekEnd)
            val completedInWeek = weekProgress.count { progress ->
                progress.date in scheduledInWeek &&
                    progress.completionState == HabitCompletionState.COMPLETED
            }

            val rate = if (scheduledInWeek.isEmpty()) {
                0f
            } else {
                completedInWeek.toFloat() / scheduledInWeek.size.toFloat()
            }

            trends.add(rate)
        }

        return trends.reversed()
    }

    fun getMonthlyTrend(
        habit: HabitEntity,
        progressList: List<HabitDailyProgress>,
        monthsBack: Int = 6,
    ): List<Float> {
        val today = LocalDate.now()
        val trends = mutableListOf<Float>()

        for (month in 0 until monthsBack) {
            val monthStart = today.minusMonths((month + 1).toLong()).withDayOfMonth(1)
            val monthEnd = monthStart.plusMonths(1).minusDays(1)

            val monthProgress = progressList.filter { progress ->
                !progress.date.isBefore(monthStart) && !progress.date.isAfter(monthEnd)
            }

            val scheduledInMonth = HabitScheduler.getScheduledDatesInRange(habit, monthStart, monthEnd)
            val completedInMonth = monthProgress.count { progress ->
                progress.date in scheduledInMonth &&
                    progress.completionState == HabitCompletionState.COMPLETED
            }

            val rate = if (scheduledInMonth.isEmpty()) {
                0f
            } else {
                completedInMonth.toFloat() / scheduledInMonth.size.toFloat()
            }

            trends.add(rate)
        }

        return trends.reversed()
    }
}
