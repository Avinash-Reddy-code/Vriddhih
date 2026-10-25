package com.vrddhih.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vrddhih.app.core.data.local.entity.HabitCheckInEntity
import com.vrddhih.app.core.data.local.entity.HabitEntity
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

    /** Returns -1 when the habit is already checked in for that day; the existing row is kept. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCheckIn(checkIn: HabitCheckInEntity): Long

    @Query("DELETE FROM habit_check_ins WHERE habit_id = :habitId AND date = :date")
    suspend fun deleteCheckIn(habitId: Long, date: LocalDate): Int

    @Query("SELECT * FROM habit_check_ins WHERE date = :date")
    fun observeCheckInsOn(date: LocalDate): Flow<List<HabitCheckInEntity>>

    @Query("SELECT * FROM habit_check_ins WHERE habit_id = :habitId AND date BETWEEN :start AND :end ORDER BY date")
    fun observeCheckInsBetween(habitId: Long, start: LocalDate, end: LocalDate): Flow<List<HabitCheckInEntity>>

    @Query("SELECT COUNT(*) FROM habit_check_ins WHERE date = :date")
    fun observeCheckInCountOn(date: LocalDate): Flow<Int>
}
