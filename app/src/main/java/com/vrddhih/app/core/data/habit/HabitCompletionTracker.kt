package com.vrddhih.app.core.data.habit

import com.vrddhih.app.core.data.local.entity.HabitChecklistProgress
import com.vrddhih.app.core.data.local.entity.HabitCompletionState
import com.vrddhih.app.core.data.local.entity.HabitDailyProgress
import com.vrddhih.app.core.data.local.entity.HabitEntity
import com.vrddhih.app.core.data.local.entity.HabitLimitConfig
import com.vrddhih.app.core.data.local.entity.HabitNumericConfig
import com.vrddhih.app.core.data.local.entity.HabitTimerConfig
import com.vrddhih.app.core.data.local.entity.HabitType
import com.vrddhih.app.core.data.local.entity.NumericDirection
import java.time.LocalDate

object HabitCompletionTracker {

    fun determineCompletionState(
        habit: HabitEntity,
        progress: HabitDailyProgress?,
        checklistProgress: List<HabitChecklistProgress> = emptyList(),
        numericConfig: HabitNumericConfig? = null,
        timerConfig: HabitTimerConfig? = null,
        limitConfig: HabitLimitConfig? = null,
        checklistItemCount: Int = 0,
    ): HabitCompletionState {
        if (progress == null) return HabitCompletionState.NOT_SCHEDULED

        when (habit.type) {
            HabitType.YES_NO -> {
                return if (progress.completionState == HabitCompletionState.COMPLETED) {
                    HabitCompletionState.COMPLETED
                } else {
                    HabitCompletionState.MISSED
                }
            }

            HabitType.NUMERIC -> {
                if (numericConfig == null) return HabitCompletionState.NOT_SCHEDULED
                val value = progress.numericValue ?: 0L

                return when (numericConfig.direction) {
                    NumericDirection.AT_LEAST -> {
                        if (value >= numericConfig.targetValue) HabitCompletionState.COMPLETED
                        else if (value > 0) HabitCompletionState.PARTIALLY_COMPLETED
                        else HabitCompletionState.MISSED
                    }
                    NumericDirection.AT_MOST -> {
                        if (value <= numericConfig.targetValue) HabitCompletionState.COMPLETED
                        else HabitCompletionState.MISSED
                    }
                }
            }

            HabitType.TIMER -> {
                if (timerConfig == null) return HabitCompletionState.NOT_SCHEDULED
                val seconds = progress.timerSeconds ?: 0

                return if (seconds >= timerConfig.dailyTargetSeconds) {
                    HabitCompletionState.COMPLETED
                } else if (seconds > 0) {
                    HabitCompletionState.PARTIALLY_COMPLETED
                } else {
                    HabitCompletionState.MISSED
                }
            }

            HabitType.CHECKLIST -> {
                if (checklistItemCount == 0) return HabitCompletionState.NOT_SCHEDULED
                val completedCount = checklistProgress.count { it.completedAt != null }

                return if (completedCount == checklistItemCount) {
                    HabitCompletionState.COMPLETED
                } else if (completedCount > 0) {
                    HabitCompletionState.PARTIALLY_COMPLETED
                } else {
                    HabitCompletionState.MISSED
                }
            }

            HabitType.LIMIT -> {
                if (limitConfig == null) return HabitCompletionState.NOT_SCHEDULED
                val value = progress.numericValue ?: 0L

                return if (value <= limitConfig.dailyLimit) {
                    HabitCompletionState.COMPLETED
                } else {
                    HabitCompletionState.MISSED
                }
            }
        }
    }

    fun calculateProgressPercentage(
        habit: HabitEntity,
        progress: HabitDailyProgress?,
        checklistProgress: List<HabitChecklistProgress> = emptyList(),
        numericConfig: HabitNumericConfig? = null,
        timerConfig: HabitTimerConfig? = null,
        limitConfig: HabitLimitConfig? = null,
        checklistItemCount: Int = 0,
    ): Float {
        if (progress == null) return 0f

        return when (habit.type) {
            HabitType.YES_NO -> {
                if (progress.completionState == HabitCompletionState.COMPLETED) 1f else 0f
            }

            HabitType.NUMERIC -> {
                if (numericConfig == null) return 0f
                val value = progress.numericValue ?: 0L

                when (numericConfig.direction) {
                    NumericDirection.AT_LEAST -> {
                        (value.toFloat() / numericConfig.targetValue.toFloat()).coerceAtMost(1f)
                    }
                    NumericDirection.AT_MOST -> {
                        if (value <= numericConfig.targetValue) 1f else 0f
                    }
                }
            }

            HabitType.TIMER -> {
                if (timerConfig == null) return 0f
                val seconds = progress.timerSeconds ?: 0
                (seconds.toFloat() / timerConfig.dailyTargetSeconds.toFloat()).coerceAtMost(1f)
            }

            HabitType.CHECKLIST -> {
                if (checklistItemCount == 0) return 0f
                val completedCount = checklistProgress.count { it.completedAt != null }
                completedCount.toFloat() / checklistItemCount.toFloat()
            }

            HabitType.LIMIT -> {
                if (limitConfig == null) return 0f
                val value = progress.numericValue ?: 0L
                if (value <= limitConfig.dailyLimit) 1f else 0f
            }
        }
    }

    fun shouldAllowMultipleCheckIns(habit: HabitEntity): Boolean {
        return when (habit.type) {
            HabitType.YES_NO -> false
            HabitType.NUMERIC -> true
            HabitType.TIMER -> true
            HabitType.CHECKLIST -> false
            HabitType.LIMIT -> true
        }
    }

    fun canCompleteToday(
        habit: HabitEntity,
        progress: HabitDailyProgress?,
        checklistProgress: List<HabitChecklistProgress> = emptyList(),
        numericConfig: HabitNumericConfig? = null,
        timerConfig: HabitTimerConfig? = null,
        limitConfig: HabitLimitConfig? = null,
        checklistItemCount: Int = 0,
    ): Boolean {
        if (progress == null) return true

        val currentState = determineCompletionState(
            habit, progress, checklistProgress, numericConfig, timerConfig, limitConfig, checklistItemCount
        )

        return when (habit.type) {
            HabitType.YES_NO -> currentState != HabitCompletionState.COMPLETED
            HabitType.NUMERIC -> true
            HabitType.TIMER -> true
            HabitType.CHECKLIST -> currentState != HabitCompletionState.COMPLETED
            HabitType.LIMIT -> true
        }
    }

    fun updateCompletionState(
        habit: HabitEntity,
        progress: HabitDailyProgress,
        checklistProgress: List<HabitChecklistProgress> = emptyList(),
        numericConfig: HabitNumericConfig? = null,
        timerConfig: HabitTimerConfig? = null,
        limitConfig: HabitLimitConfig? = null,
        checklistItemCount: Int = 0,
    ): HabitCompletionState {
        return determineCompletionState(
            habit, progress, checklistProgress, numericConfig, timerConfig, limitConfig, checklistItemCount
        )
    }
}
