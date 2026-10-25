package com.vrddhih.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vrddhih.app.core.data.local.entity.QuestCompletionEntity
import com.vrddhih.app.core.data.local.entity.QuestEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

@Dao
interface QuestDao {
    @Insert
    suspend fun insertQuest(quest: QuestEntity): Long

    @Update
    suspend fun updateQuest(quest: QuestEntity)

    @Delete
    suspend fun deleteQuest(quest: QuestEntity)

    @Query("UPDATE quests SET is_archived = 1, updated_at = :now WHERE id = :id")
    suspend fun archiveQuest(id: Long, now: Instant)

    @Query("SELECT * FROM quests WHERE id = :id")
    suspend fun getQuest(id: Long): QuestEntity?

    @Query("SELECT * FROM quests WHERE is_archived = 0 ORDER BY start_date, id")
    fun observeActiveQuests(): Flow<List<QuestEntity>>

    /**
     * Quests that may occur on [date]: one-off quests due that day, plus recurring quests whose date
     * range contains it. The exact recurrence rule is applied in the repository.
     */
    @Query(
        """
        SELECT * FROM quests
        WHERE is_archived = 0
          AND start_date <= :date
          AND (recurrence != 'NONE' OR start_date = :date)
          AND (recurrence = 'NONE' OR end_date IS NULL OR end_date >= :date)
        ORDER BY CASE priority WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 ELSE 2 END, id
        """,
    )
    fun observeQuestCandidatesOn(date: LocalDate): Flow<List<QuestEntity>>

    /** Returns -1 when the occurrence is already completed; the existing row is kept. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: QuestCompletionEntity): Long

    @Query("DELETE FROM quest_completions WHERE quest_id = :questId AND occurrence_date = :date")
    suspend fun deleteCompletion(questId: Long, date: LocalDate): Int

    @Query("SELECT * FROM quest_completions WHERE occurrence_date = :date ORDER BY completed_at")
    fun observeCompletionsOn(date: LocalDate): Flow<List<QuestCompletionEntity>>

    @Query("SELECT COUNT(*) FROM quest_completions WHERE occurrence_date = :date")
    fun observeCompletionCountOn(date: LocalDate): Flow<Int>
}
