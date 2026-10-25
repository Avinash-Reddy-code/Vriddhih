package com.vrddhih.app.core.data.repository

import com.vrddhih.app.core.data.local.dao.HabitDao
import com.vrddhih.app.core.data.local.entity.HabitCheckInEntity
import com.vrddhih.app.core.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

class HabitRepository(
    private val dao: HabitDao,
    private val clock: Clock,
) {
    fun observeActiveHabits(): Flow<List<HabitEntity>> = dao.observeActiveHabits()

    fun observeHabitsScheduledOn(date: LocalDate): Flow<List<HabitEntity>> =
        dao.observeActiveHabits().map { habits -> habits.filter { it.isScheduledOn(date) } }

    fun observeCheckInsOn(date: LocalDate): Flow<List<HabitCheckInEntity>> = dao.observeCheckInsOn(date)

    suspend fun addHabit(
        name: String,
        startDate: LocalDate,
        scheduleDaysMask: Int = HabitEntity.EVERY_DAY_MASK,
        xpPerCheckIn: Int = 5,
        notes: String = "",
    ): Long {
        require(name.isNotBlank()) { "Habit name must not be blank" }
        require(scheduleDaysMask in 1..HabitEntity.EVERY_DAY_MASK) { "Habit must be scheduled on at least one day" }
        val now = Instant.now(clock)
        return dao.insertHabit(
            HabitEntity(
                name = name.trim(),
                notes = notes,
                scheduleDaysMask = scheduleDaysMask,
                xpPerCheckIn = xpPerCheckIn,
                startDate = startDate,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun updateHabit(habit: HabitEntity) = dao.updateHabit(habit.copy(updatedAt = Instant.now(clock)))

    suspend fun deleteHabit(habit: HabitEntity) = dao.deleteHabit(habit)

    /** Returns false if the habit was already checked in on [date]. */
    suspend fun checkIn(habitId: Long, date: LocalDate): Boolean =
        dao.insertCheckIn(HabitCheckInEntity(habitId = habitId, date = date, checkedInAt = Instant.now(clock))) != -1L

    /** Returns false if there was no check-in to remove. */
    suspend fun undoCheckIn(habitId: Long, date: LocalDate): Boolean = dao.deleteCheckIn(habitId, date) > 0
}
