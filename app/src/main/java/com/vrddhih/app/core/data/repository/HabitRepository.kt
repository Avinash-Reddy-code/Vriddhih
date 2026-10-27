package com.vrddhih.app.core.data.repository

import androidx.room.withTransaction
import com.vrddhih.app.core.data.habit.HabitCompletionTracker
import com.vrddhih.app.core.data.habit.HabitScheduleEvaluation
import com.vrddhih.app.core.data.habit.HabitScheduler
import com.vrddhih.app.core.data.local.VrddhihDatabase
import com.vrddhih.app.core.data.local.dao.HabitDao
import com.vrddhih.app.core.data.local.dao.HabitWithConfigs
import com.vrddhih.app.core.data.local.entity.HabitChecklistItem
import com.vrddhih.app.core.data.local.entity.HabitChecklistProgress
import com.vrddhih.app.core.data.local.entity.HabitCompletionState
import com.vrddhih.app.core.data.local.entity.HabitDailyProgress
import com.vrddhih.app.core.data.local.entity.HabitEntity
import com.vrddhih.app.core.data.local.entity.HabitExcludedDate
import com.vrddhih.app.core.data.local.entity.HabitGoalLink
import com.vrddhih.app.core.data.local.entity.HabitLimitConfig
import com.vrddhih.app.core.data.local.entity.HabitNumericConfig
import com.vrddhih.app.core.data.local.entity.HabitScheduleType
import com.vrddhih.app.core.data.local.entity.HabitTimerConfig
import com.vrddhih.app.core.data.local.entity.HabitTimerSession
import com.vrddhih.app.core.data.local.entity.HabitType
import com.vrddhih.app.core.data.local.entity.NumericDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

class HabitRepository(
    private val db: VrddhihDatabase,
    private val clock: Clock,
) {
    private val dao = db.habitDao()

    private fun now(): Instant = Instant.now(clock)

    fun observeActiveHabits(): Flow<List<HabitEntity>> = dao.observeActiveHabits()

    fun observeArchivedHabits(): Flow<List<HabitEntity>> = dao.observeArchivedHabits()

    fun observeHabitsByCategory(categoryId: String): Flow<List<HabitEntity>> = dao.observeHabitsByCategory(categoryId)

    suspend fun getHabit(id: Long): HabitEntity? = dao.getHabit(id)

    suspend fun getHabitWithConfigs(id: Long) = dao.getHabitWithConfigs(id)

    fun observeHabitWithConfigs(id: Long): Flow<HabitWithConfigs?> = dao.observeHabitWithConfigs(id)

    fun observeActiveHabitsWithConfigs(): Flow<List<HabitWithConfigs>> = dao.observeActiveHabitsWithConfigs()

    fun observeDailyProgressOn(date: LocalDate): Flow<List<HabitDailyProgress>> = dao.observeDailyProgressOn(date)

    fun observeChecklistProgressOn(date: LocalDate): Flow<List<HabitChecklistProgress>> = dao.observeChecklistProgressOn(date)

    fun observeHabitsScheduledOn(date: LocalDate): Flow<List<HabitEntity>> = combine(
        dao.observeActiveHabits(),
        dao.observeDailyProgressOn(date),
    ) { habits, progressList ->
        val progressMap = progressList.associateBy { it.habitId }
        habits.filter { habit ->
            val excludedDates = runCatching { dao.getExcludedDates(habit.id).map { it.date } }.getOrDefault(emptyList())
            HabitScheduler.isScheduledOn(habit, date, excludedDates)
        }
    }

    fun observeHabitProgress(habitId: Long, date: LocalDate): Flow<HabitDailyProgress?> = dao.observeDailyProgress(habitId, date)

    suspend fun createHabit(
        name: String,
        type: HabitType,
        scheduleType: HabitScheduleType = HabitScheduleType.DAILY,
        scheduleInterval: Int = 1,
        scheduleDaysMask: Int = 127,
        startDate: LocalDate,
        endDate: LocalDate? = null,
        categoryId: String? = null,
        iconKey: String? = null,
        colorArgb: Int? = null,
        xpPerCompletion: Int = 5,
        description: String = "",
    ): Long = db.withTransaction {
        require(name.isNotBlank()) { "Habit name must not be blank" }
        val now = now()
        val habitId = dao.insertHabit(
            HabitEntity(
                name = name.trim(),
                type = type,
                scheduleType = scheduleType,
                scheduleInterval = scheduleInterval,
                scheduleDaysMask = scheduleDaysMask,
                startDate = startDate,
                endDate = endDate,
                categoryId = categoryId,
                iconKey = iconKey,
                colorArgb = colorArgb,
                xpPerCompletion = xpPerCompletion,
                description = description,
                createdAt = now,
                updatedAt = now,
            ),
        )
        habitId
    }

    suspend fun updateHabit(habit: HabitEntity) = dao.updateHabit(habit.copy(updatedAt = now()))

    suspend fun archiveHabit(id: Long) {
        val habit = dao.getHabit(id) ?: return
        dao.updateHabit(habit.copy(isArchived = true, updatedAt = now()))
    }

    suspend fun unarchiveHabit(id: Long) {
        val habit = dao.getHabit(id) ?: return
        dao.updateHabit(habit.copy(isArchived = false, updatedAt = now()))
    }

    suspend fun deleteHabit(habit: HabitEntity) = dao.deleteHabit(habit)

    suspend fun setNumericConfig(
        habitId: Long,
        targetValue: Long,
        unit: String,
        direction: NumericDirection,
        incrementValue: Long? = null,
    ) = db.withTransaction {
        val existing = dao.getNumericConfig(habitId)
        if (existing != null) {
            dao.updateNumericConfig(
                existing.copy(
                    targetValue = targetValue,
                    unit = unit,
                    direction = direction,
                    incrementValue = incrementValue,
                ),
            )
        } else {
            dao.insertNumericConfig(
                HabitNumericConfig(
                    habitId = habitId,
                    targetValue = targetValue,
                    unit = unit,
                    direction = direction,
                    incrementValue = incrementValue,
                ),
            )
        }
    }

    suspend fun setTimerConfig(
        habitId: Long,
        targetDurationSeconds: Int,
        dailyTargetSeconds: Int,
    ) = db.withTransaction {
        val existing = dao.getTimerConfig(habitId)
        if (existing != null) {
            dao.updateTimerConfig(
                existing.copy(
                    targetDurationSeconds = targetDurationSeconds,
                    dailyTargetSeconds = dailyTargetSeconds,
                ),
            )
        } else {
            dao.insertTimerConfig(
                HabitTimerConfig(
                    habitId = habitId,
                    targetDurationSeconds = targetDurationSeconds,
                    dailyTargetSeconds = dailyTargetSeconds,
                ),
            )
        }
    }

    suspend fun setLimitConfig(
        habitId: Long,
        dailyLimit: Long,
        unit: String,
        allowYesNo: Boolean = false,
    ) = db.withTransaction {
        val existing = dao.getLimitConfig(habitId)
        if (existing != null) {
            dao.updateLimitConfig(
                existing.copy(
                    dailyLimit = dailyLimit,
                    unit = unit,
                    allowYesNo = allowYesNo,
                ),
            )
        } else {
            dao.insertLimitConfig(
                HabitLimitConfig(
                    habitId = habitId,
                    dailyLimit = dailyLimit,
                    unit = unit,
                    allowYesNo = allowYesNo,
                ),
            )
        }
    }

    suspend fun addChecklistItem(habitId: Long, title: String, position: Int): Long {
        require(title.isNotBlank()) { "Checklist item title must not be blank" }
        return dao.insertChecklistItem(
            HabitChecklistItem(
                habitId = habitId,
                title = title.trim(),
                position = position,
                createdAt = now(),
            ),
        )
    }

    suspend fun updateChecklistItem(item: HabitChecklistItem) = dao.updateChecklistItem(item)

    suspend fun deleteChecklistItem(item: HabitChecklistItem) = dao.deleteChecklistItem(item)

    suspend fun setChecklistItemProgress(habitId: Long, date: LocalDate, itemId: Long, completed: Boolean) = db.withTransaction {
        val existing = dao.getChecklistProgress(habitId, date).find { it.itemId == itemId }
        if (completed) {
            if (existing == null) {
                dao.insertChecklistProgress(
                    HabitChecklistProgress(
                        habitId = habitId,
                        date = date,
                        itemId = itemId,
                        completedAt = now(),
                    ),
                )
            } else {
                dao.insertChecklistProgress(existing.copy(completedAt = now()))
            }
        } else {
            if (existing != null) {
                dao.insertChecklistProgress(existing.copy(completedAt = null))
            }
        }
    }

    suspend fun addExcludedDate(habitId: Long, date: LocalDate): Long = dao.insertExcludedDate(
        HabitExcludedDate(habitId = habitId, date = date),
    )

    suspend fun removeExcludedDate(habitId: Long, date: LocalDate) = dao.deleteExcludedDate(habitId, date)

    suspend fun completeHabit(
        habitId: Long,
        date: LocalDate,
        numericValue: Long? = null,
        timerSeconds: Int? = null,
        note: String? = null,
    ): Boolean = db.withTransaction {
        val habit = dao.getHabit(habitId) ?: return@withTransaction false
        val excludedDates = dao.getExcludedDates(habitId).map { it.date }

        if (!HabitScheduler.isScheduledOn(habit, date, excludedDates)) return@withTransaction false

        val existing = dao.getDailyProgress(habitId, date)
        val now = now()

        if (existing != null) {
            dao.updateDailyProgress(
                existing.copy(
                    completionState = HabitCompletionState.COMPLETED,
                    numericValue = numericValue ?: existing.numericValue,
                    timerSeconds = timerSeconds ?: existing.timerSeconds,
                    note = note ?: existing.note,
                    completedAt = now,
                    updatedAt = now,
                ),
            )
        } else {
            dao.insertDailyProgress(
                HabitDailyProgress(
                    habitId = habitId,
                    date = date,
                    completionState = HabitCompletionState.COMPLETED,
                    numericValue = numericValue,
                    timerSeconds = timerSeconds,
                    note = note,
                    completedAt = now,
                    updatedAt = now,
                ),
            )
        }
        true
    }

    suspend fun undoCompletion(habitId: Long, date: LocalDate): Boolean = db.withTransaction {
        val existing = dao.getDailyProgress(habitId, date) ?: return@withTransaction false
        if (existing.completionState != HabitCompletionState.COMPLETED) return@withTransaction false

        dao.updateDailyProgress(
            existing.copy(
                completionState = HabitCompletionState.MISSED,
                completedAt = null,
                updatedAt = now(),
            ),
        )
        true
    }

    suspend fun updateNumericProgress(habitId: Long, date: LocalDate, value: Long): Boolean = db.withTransaction {
        val habit = dao.getHabit(habitId) ?: return@withTransaction false
        if (habit.type != HabitType.NUMERIC) return@withTransaction false

        val config = dao.getNumericConfig(habitId)
        val target = config?.targetValue ?: 0L
        val isTargetReached = if (config?.direction == NumericDirection.AT_MOST) {
            value <= target && value > 0
        } else {
            value >= target
        }
        val completionState = when {
            isTargetReached -> HabitCompletionState.COMPLETED
            value > 0 -> HabitCompletionState.PARTIALLY_COMPLETED
            else -> HabitCompletionState.MISSED
        }
        val now = now()
        val existing = dao.getDailyProgress(habitId, date)

        if (existing != null) {
            dao.updateDailyProgress(
                existing.copy(
                    numericValue = value,
                    completionState = completionState,
                    completedAt = if (isTargetReached) (existing.completedAt ?: now) else null,
                    updatedAt = now,
                ),
            )
        } else {
            dao.insertDailyProgress(
                HabitDailyProgress(
                    habitId = habitId,
                    date = date,
                    completionState = completionState,
                    numericValue = value,
                    completedAt = if (isTargetReached) now else null,
                    updatedAt = now,
                ),
            )
        }
        true
    }

    suspend fun logTimerSeconds(habitId: Long, date: LocalDate, additionalSeconds: Int): Boolean = db.withTransaction {
        val habit = dao.getHabit(habitId) ?: return@withTransaction false
        if (habit.type != HabitType.TIMER) return@withTransaction false

        val config = dao.getTimerConfig(habitId)
        val dailyTarget = config?.dailyTargetSeconds ?: (config?.targetDurationSeconds ?: 0)

        val existing = dao.getDailyProgress(habitId, date)
        val currentSeconds = existing?.timerSeconds ?: 0
        val newSeconds = maxOf(0, currentSeconds + additionalSeconds)

        val isCompleted = dailyTarget > 0 && newSeconds >= dailyTarget
        val completionState = when {
            isCompleted -> HabitCompletionState.COMPLETED
            newSeconds > 0 -> HabitCompletionState.PARTIALLY_COMPLETED
            else -> HabitCompletionState.MISSED
        }
        val now = now()

        if (existing != null) {
            dao.updateDailyProgress(
                existing.copy(
                    timerSeconds = newSeconds,
                    completionState = completionState,
                    completedAt = if (isCompleted) (existing.completedAt ?: now) else null,
                    updatedAt = now,
                ),
            )
        } else {
            dao.insertDailyProgress(
                HabitDailyProgress(
                    habitId = habitId,
                    date = date,
                    completionState = completionState,
                    timerSeconds = newSeconds,
                    completedAt = if (isCompleted) now else null,
                    updatedAt = now,
                ),
            )
        }
        true
    }

    suspend fun updateLimitProgress(habitId: Long, date: LocalDate, value: Long): Boolean = db.withTransaction {
        val habit = dao.getHabit(habitId) ?: return@withTransaction false
        if (habit.type != HabitType.LIMIT) return@withTransaction false

        val config = dao.getLimitConfig(habitId)
        val limit = config?.dailyLimit ?: Long.MAX_VALUE
        val isWithinLimit = value <= limit
        val completionState = if (isWithinLimit) HabitCompletionState.COMPLETED else HabitCompletionState.MISSED
        val now = now()
        val existing = dao.getDailyProgress(habitId, date)

        if (existing != null) {
            dao.updateDailyProgress(
                existing.copy(
                    numericValue = value,
                    completionState = completionState,
                    completedAt = if (isWithinLimit) (existing.completedAt ?: now) else null,
                    updatedAt = now,
                ),
            )
        } else {
            dao.insertDailyProgress(
                HabitDailyProgress(
                    habitId = habitId,
                    date = date,
                    completionState = completionState,
                    numericValue = value,
                    completedAt = if (isWithinLimit) now else null,
                    updatedAt = now,
                ),
            )
        }
        true
    }

    suspend fun toggleChecklistItem(habitId: Long, date: LocalDate, itemId: Long, completed: Boolean) = db.withTransaction {
        setChecklistItemProgress(habitId, date, itemId, completed)

        val totalItems = dao.getChecklistItems(habitId)
        val progressList = dao.getChecklistProgress(habitId, date)
        val completedCount = totalItems.count { item ->
            if (item.id == itemId) completed
            else progressList.any { it.itemId == item.id && it.completedAt != null }
        }

        val allDone = totalItems.isNotEmpty() && completedCount == totalItems.size
        val anyDone = completedCount > 0
        val completionState = when {
            allDone -> HabitCompletionState.COMPLETED
            anyDone -> HabitCompletionState.PARTIALLY_COMPLETED
            else -> HabitCompletionState.MISSED
        }
        val now = now()
        val existing = dao.getDailyProgress(habitId, date)

        if (existing != null) {
            dao.updateDailyProgress(
                existing.copy(
                    completionState = completionState,
                    completedAt = if (allDone) (existing.completedAt ?: now) else null,
                    updatedAt = now,
                ),
            )
        } else {
            dao.insertDailyProgress(
                HabitDailyProgress(
                    habitId = habitId,
                    date = date,
                    completionState = completionState,
                    completedAt = if (allDone) now else null,
                    updatedAt = now,
                ),
            )
        }
    }

    suspend fun saveHabitWithConfigs(
        habitId: Long? = null,
        name: String,
        type: HabitType,
        description: String = "",
        scheduleType: HabitScheduleType = HabitScheduleType.DAILY,
        scheduleInterval: Int = 1,
        scheduleDaysMask: Int = 127,
        startDate: LocalDate,
        endDate: LocalDate? = null,
        categoryId: String? = null,
        xpPerCompletion: Int = 5,
        numericTargetValue: Long = 1,
        numericUnit: String = "",
        numericDirection: NumericDirection = NumericDirection.AT_LEAST,
        numericIncrement: Long = 1,
        timerTargetMinutes: Int = 15,
        limitDailyTarget: Long = 1,
        limitUnit: String = "",
        checklistItemTitles: List<String> = emptyList(),
    ): Long = db.withTransaction {
        require(name.isNotBlank()) { "Habit name must not be blank" }
        val now = now()
        val id = if (habitId == null || habitId == 0L) {
            dao.insertHabit(
                HabitEntity(
                    name = name.trim(),
                    type = type,
                    description = description.trim(),
                    scheduleType = scheduleType,
                    scheduleInterval = scheduleInterval,
                    scheduleDaysMask = scheduleDaysMask,
                    startDate = startDate,
                    endDate = endDate,
                    categoryId = categoryId,
                    xpPerCompletion = xpPerCompletion,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        } else {
            val existing = checkNotNull(dao.getHabit(habitId))
            dao.updateHabit(
                existing.copy(
                    name = name.trim(),
                    type = type,
                    description = description.trim(),
                    scheduleType = scheduleType,
                    scheduleInterval = scheduleInterval,
                    scheduleDaysMask = scheduleDaysMask,
                    startDate = startDate,
                    endDate = endDate,
                    categoryId = categoryId,
                    xpPerCompletion = xpPerCompletion,
                    updatedAt = now,
                ),
            )
            habitId
        }

        when (type) {
            HabitType.YES_NO -> { }
            HabitType.NUMERIC -> {
                setNumericConfig(
                    habitId = id,
                    targetValue = maxOf(1L, numericTargetValue),
                    unit = numericUnit.trim(),
                    direction = numericDirection,
                    incrementValue = maxOf(1L, numericIncrement),
                )
            }
            HabitType.TIMER -> {
                val targetSeconds = maxOf(60, timerTargetMinutes * 60)
                setTimerConfig(
                    habitId = id,
                    targetDurationSeconds = targetSeconds,
                    dailyTargetSeconds = targetSeconds,
                )
            }
            HabitType.LIMIT -> {
                setLimitConfig(
                    habitId = id,
                    dailyLimit = maxOf(1L, limitDailyTarget),
                    unit = limitUnit.trim(),
                )
            }
            HabitType.CHECKLIST -> {
                dao.deleteChecklistItemsForHabit(id)
                checklistItemTitles.filter { it.isNotBlank() }.forEachIndexed { idx, title ->
                    addChecklistItem(habitId = id, title = title.trim(), position = idx)
                }
            }
        }
        id
    }

    suspend fun linkToGoal(habitId: Long, goalId: Long, contributionRule: String? = null): Long = dao.insertHabitGoalLink(
        HabitGoalLink(habitId = habitId, goalId = goalId, contributionRule = contributionRule),
    )

    suspend fun unlinkFromGoal(habitId: Long, goalId: Long) = dao.deleteHabitGoalLink(habitId, goalId)

    fun observeHabitGoalLinks(habitId: Long): Flow<List<HabitGoalLink>> = dao.observeHabitGoalLinks(habitId)
}
