package com.vrddhih.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.vrddhih.app.core.data.local.entity.HabitCheckInEntity
import com.vrddhih.app.core.data.local.entity.HabitDailyProgress
import com.vrddhih.app.core.data.local.entity.HabitEntity
import com.vrddhih.app.core.data.local.entity.HabitExcludedDate
import com.vrddhih.app.core.data.local.entity.HabitGoalLink
import com.vrddhih.app.core.data.local.entity.HabitChecklistItem
import com.vrddhih.app.core.data.local.entity.HabitChecklistProgress
import com.vrddhih.app.core.data.local.entity.HabitLimitConfig
import com.vrddhih.app.core.data.local.entity.HabitNumericConfig
import com.vrddhih.app.core.data.local.entity.HabitTimerConfig
import com.vrddhih.app.core.data.local.entity.HabitTimerSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitDao {
    @Insert
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabit(id: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE is_archived = 0 ORDER BY name COLLATE NOCASE, id")
    fun observeActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE is_archived = 1 ORDER BY name COLLATE NOCASE, id")
    fun observeArchivedHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE category_id = :categoryId AND is_archived = 0 ORDER BY name COLLATE NOCASE, id")
    fun observeHabitsByCategory(categoryId: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id IN (:ids)")
    suspend fun getHabitsByIds(ids: List<Long>): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCheckIn(checkIn: HabitCheckInEntity): Long

    @Query("DELETE FROM habit_check_ins WHERE habit_id = :habitId AND date = :date")
    suspend fun deleteCheckIn(habitId: Long, date: LocalDate): Int

    @Query("SELECT * FROM habit_check_ins WHERE date = :date")
    fun observeCheckInsOn(date: LocalDate): Flow<List<HabitCheckInEntity>>

    @Query("SELECT * FROM habit_check_ins WHERE habit_id = :habitId AND date BETWEEN :start AND :end ORDER BY date")
    fun observeCheckInsBetween(habitId: Long, start: LocalDate, end: LocalDate): Flow<List<HabitCheckInEntity>>

    @Query("SELECT COUNT(*) FROM habit_daily_progress WHERE date = :date AND completion_state = 'COMPLETED'")
    fun observeCheckInCountOn(date: LocalDate): Flow<Int>

    @Insert
    suspend fun insertDailyProgress(progress: HabitDailyProgress): Long

    @Update
    suspend fun updateDailyProgress(progress: HabitDailyProgress)

    @Query("SELECT * FROM habit_daily_progress WHERE habit_id = :habitId AND date = :date")
    suspend fun getDailyProgress(habitId: Long, date: LocalDate): HabitDailyProgress?

    @Query("SELECT * FROM habit_daily_progress WHERE habit_id = :habitId AND date = :date")
    fun observeDailyProgress(habitId: Long, date: LocalDate): Flow<HabitDailyProgress?>

    @Query("SELECT * FROM habit_daily_progress WHERE date = :date")
    fun observeDailyProgressOn(date: LocalDate): Flow<List<HabitDailyProgress>>

    @Query("SELECT * FROM habit_daily_progress WHERE habit_id = :habitId AND date BETWEEN :start AND :end ORDER BY date")
    fun observeDailyProgressBetween(habitId: Long, start: LocalDate, end: LocalDate): Flow<List<HabitDailyProgress>>

    @Query("DELETE FROM habit_daily_progress WHERE habit_id = :habitId AND date = :date")
    suspend fun deleteDailyProgress(habitId: Long, date: LocalDate): Int

    @Insert
    suspend fun insertNumericConfig(config: HabitNumericConfig): Long

    @Update
    suspend fun updateNumericConfig(config: HabitNumericConfig)

    @Query("SELECT * FROM habit_numeric_config WHERE habit_id = :habitId")
    suspend fun getNumericConfig(habitId: Long): HabitNumericConfig?

    @Query("SELECT * FROM habit_numeric_config WHERE habit_id = :habitId")
    fun observeNumericConfig(habitId: Long): Flow<HabitNumericConfig?>

    @Delete
    suspend fun deleteNumericConfig(config: HabitNumericConfig)

    @Insert
    suspend fun insertTimerConfig(config: HabitTimerConfig): Long

    @Update
    suspend fun updateTimerConfig(config: HabitTimerConfig)

    @Query("SELECT * FROM habit_timer_config WHERE habit_id = :habitId")
    suspend fun getTimerConfig(habitId: Long): HabitTimerConfig?

    @Query("SELECT * FROM habit_timer_config WHERE habit_id = :habitId")
    fun observeTimerConfig(habitId: Long): Flow<HabitTimerConfig?>

    @Delete
    suspend fun deleteTimerConfig(config: HabitTimerConfig)

    @Insert
    suspend fun insertTimerSession(session: HabitTimerSession): Long

    @Update
    suspend fun updateTimerSession(session: HabitTimerSession)

    @Query("SELECT * FROM habit_timer_sessions WHERE habit_id = :habitId AND date = :date")
    suspend fun getTimerSession(habitId: Long, date: LocalDate): HabitTimerSession?

    @Query("SELECT * FROM habit_timer_sessions WHERE habit_id = :habitId AND date = :date")
    fun observeTimerSession(habitId: Long, date: LocalDate): Flow<HabitTimerSession?>

    @Query("SELECT * FROM habit_timer_sessions WHERE habit_id = :habitId AND date BETWEEN :start AND :end ORDER BY date")
    fun observeTimerSessionsBetween(habitId: Long, start: LocalDate, end: LocalDate): Flow<List<HabitTimerSession>>

    @Query("DELETE FROM habit_timer_sessions WHERE habit_id = :habitId AND date = :date")
    suspend fun deleteTimerSession(habitId: Long, date: LocalDate): Int

    @Insert
    suspend fun insertChecklistItem(item: HabitChecklistItem): Long

    @Update
    suspend fun updateChecklistItem(item: HabitChecklistItem)

    @Delete
    suspend fun deleteChecklistItem(item: HabitChecklistItem)

    @Query("SELECT * FROM habit_checklist_items WHERE habit_id = :habitId ORDER BY position, id")
    suspend fun getChecklistItems(habitId: Long): List<HabitChecklistItem>

    @Query("SELECT * FROM habit_checklist_items WHERE habit_id = :habitId ORDER BY position, id")
    fun observeChecklistItems(habitId: Long): Flow<List<HabitChecklistItem>>

    @Query("DELETE FROM habit_checklist_items WHERE habit_id = :habitId")
    suspend fun deleteChecklistItemsForHabit(habitId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistProgress(progress: HabitChecklistProgress): Long

    @Query("SELECT * FROM habit_checklist_progress WHERE habit_id = :habitId AND date = :date")
    suspend fun getChecklistProgress(habitId: Long, date: LocalDate): List<HabitChecklistProgress>

    @Query("SELECT * FROM habit_checklist_progress WHERE habit_id = :habitId AND date = :date")
    fun observeChecklistProgress(habitId: Long, date: LocalDate): Flow<List<HabitChecklistProgress>>

    @Query("DELETE FROM habit_checklist_progress WHERE habit_id = :habitId AND date = :date")
    suspend fun deleteChecklistProgress(habitId: Long, date: LocalDate): Int

    @Insert
    suspend fun insertLimitConfig(config: HabitLimitConfig): Long

    @Update
    suspend fun updateLimitConfig(config: HabitLimitConfig)

    @Query("SELECT * FROM habit_limit_config WHERE habit_id = :habitId")
    suspend fun getLimitConfig(habitId: Long): HabitLimitConfig?

    @Query("SELECT * FROM habit_limit_config WHERE habit_id = :habitId")
    fun observeLimitConfig(habitId: Long): Flow<HabitLimitConfig?>

    @Delete
    suspend fun deleteLimitConfig(config: HabitLimitConfig)

    @Insert
    suspend fun insertExcludedDate(excludedDate: HabitExcludedDate): Long

    @Query("DELETE FROM habit_excluded_dates WHERE habit_id = :habitId AND date = :date")
    suspend fun deleteExcludedDate(habitId: Long, date: LocalDate): Int

    @Query("SELECT * FROM habit_excluded_dates WHERE habit_id = :habitId")
    suspend fun getExcludedDates(habitId: Long): List<HabitExcludedDate>

    @Query("SELECT * FROM habit_excluded_dates WHERE habit_id = :habitId")
    fun observeExcludedDates(habitId: Long): Flow<List<HabitExcludedDate>>

    @Query("DELETE FROM habit_excluded_dates WHERE habit_id = :habitId")
    suspend fun deleteExcludedDatesForHabit(habitId: Long): Int

    @Insert
    suspend fun insertHabitGoalLink(link: HabitGoalLink): Long

    @Query("DELETE FROM habit_goal_links WHERE habit_id = :habitId AND goal_id = :goalId")
    suspend fun deleteHabitGoalLink(habitId: Long, goalId: Long): Int

    @Query("SELECT * FROM habit_goal_links WHERE habit_id = :habitId")
    suspend fun getHabitGoalLinks(habitId: Long): List<HabitGoalLink>

    @Query("SELECT * FROM habit_goal_links WHERE habit_id = :habitId")
    fun observeHabitGoalLinks(habitId: Long): Flow<List<HabitGoalLink>>

    @Query("SELECT * FROM habit_goal_links WHERE goal_id = :goalId")
    suspend fun getHabitsForGoal(goalId: Long): List<HabitGoalLink>

    @Query("DELETE FROM habit_goal_links WHERE habit_id = :habitId")
    suspend fun deleteHabitGoalLinksForHabit(habitId: Long): Int

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitWithConfigs(id: Long): HabitWithConfigs?

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :id")
    fun observeHabitWithConfigs(id: Long): Flow<HabitWithConfigs?>

    @Transaction
    @Query("SELECT * FROM habits WHERE is_archived = 0 ORDER BY name COLLATE NOCASE, id")
    fun observeActiveHabitsWithConfigs(): Flow<List<HabitWithConfigs>>

    @Query("SELECT * FROM habit_checklist_progress WHERE date = :date")
    fun observeChecklistProgressOn(date: LocalDate): Flow<List<HabitChecklistProgress>>
}

data class HabitWithConfigs(
    @Embedded val habit: HabitEntity,
    @Relation(parentColumn = "id", entityColumn = "habit_id")
    val numericConfig: HabitNumericConfig?,
    @Relation(parentColumn = "id", entityColumn = "habit_id")
    val timerConfig: HabitTimerConfig?,
    @Relation(parentColumn = "id", entityColumn = "habit_id")
    val limitConfig: HabitLimitConfig?,
    @Relation(parentColumn = "id", entityColumn = "habit_id")
    val checklistItems: List<HabitChecklistItem> = emptyList(),
)
