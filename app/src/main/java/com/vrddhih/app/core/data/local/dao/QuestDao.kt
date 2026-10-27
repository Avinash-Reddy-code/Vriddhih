package com.vrddhih.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.vrddhih.app.core.data.local.entity.ActivityEventEntity
import com.vrddhih.app.core.data.local.entity.QuestCompletionEntity
import com.vrddhih.app.core.data.local.entity.QuestEntity
import com.vrddhih.app.core.data.local.entity.QuestOccurrenceOverrideEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

@Dao
interface QuestDao {
    // region Definitions

    @Insert
    suspend fun insertQuest(quest: QuestEntity): Long

    @Update
    suspend fun updateQuest(quest: QuestEntity)

    @Delete
    suspend fun deleteQuest(quest: QuestEntity)

    @Query("DELETE FROM quests WHERE id = :id")
    suspend fun deleteQuestById(id: Long): Int

    @Query("UPDATE quests SET is_archived = 1, updated_at = :now WHERE id = :id")
    suspend fun archiveQuest(id: Long, now: Instant)

    @Query("SELECT * FROM quests WHERE id = :id")
    suspend fun getQuest(id: Long): QuestEntity?

    @Query("SELECT * FROM quests WHERE id = :id")
    fun observeQuest(id: Long): Flow<QuestEntity?>

    @Query("SELECT * FROM quests WHERE is_archived = 0 ORDER BY start_date, id")
    fun observeActiveQuests(): Flow<List<QuestEntity>>

    /**
     * Quests that may occur on [date]: one-off quests scheduled that day, plus recurring quests whose date
     * range contains it. The exact recurrence rule is applied in Kotlin ([com.vrddhih.app.core.data.quest.occursOn]).
     */
    @Query(
        """
        SELECT * FROM quests
        WHERE is_archived = 0
          AND start_date <= :date
          AND (recurrence != 'NONE' OR start_date = :date)
          AND (recurrence = 'NONE' OR end_date IS NULL OR end_date >= :date)
        ORDER BY id
        """,
    )
    fun observeQuestCandidatesOn(date: LocalDate): Flow<List<QuestEntity>>

    // endregion

    // region Completions

    /** Returns -1 when the occurrence is already completed; the existing row is kept. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: QuestCompletionEntity): Long

    @Query("DELETE FROM quest_completions WHERE quest_id = :questId AND occurrence_date = :date")
    suspend fun deleteCompletion(questId: Long, date: LocalDate): Int

    @Query("DELETE FROM quest_completions WHERE quest_id = :questId AND occurrence_date >= :from")
    suspend fun deleteCompletionsFrom(questId: Long, from: LocalDate): Int

    @Query("UPDATE quest_completions SET quest_id = :newQuestId WHERE quest_id = :questId AND occurrence_date >= :from")
    suspend fun moveCompletionsFrom(questId: Long, from: LocalDate, newQuestId: Long)

    @Query("SELECT * FROM quest_completions WHERE occurrence_date = :date ORDER BY completed_at")
    fun observeCompletionsOn(date: LocalDate): Flow<List<QuestCompletionEntity>>

    @Query("SELECT * FROM quest_completions ORDER BY completed_at DESC")
    fun observeAllCompletions(): Flow<List<QuestCompletionEntity>>

    @Query("SELECT * FROM quest_completions WHERE quest_id = :questId ORDER BY occurrence_date")
    suspend fun getCompletionsFor(questId: Long): List<QuestCompletionEntity>

    @Query("SELECT COUNT(*) FROM quest_completions WHERE occurrence_date = :date")
    fun observeCompletionCountOn(date: LocalDate): Flow<Int>

    // endregion

    // region Occurrence overrides

    @Upsert
    suspend fun upsertOverride(override: QuestOccurrenceOverrideEntity)

    @Query("SELECT * FROM quest_occurrence_overrides WHERE quest_id = :questId AND occurrence_date = :date")
    suspend fun getOverride(questId: Long, date: LocalDate): QuestOccurrenceOverrideEntity?

    @Query("SELECT * FROM quest_occurrence_overrides WHERE occurrence_date = :date")
    fun observeOverridesOn(date: LocalDate): Flow<List<QuestOccurrenceOverrideEntity>>

    @Query("SELECT * FROM quest_occurrence_overrides")
    fun observeAllOverrides(): Flow<List<QuestOccurrenceOverrideEntity>>

    @Query("DELETE FROM quest_occurrence_overrides WHERE quest_id = :questId AND occurrence_date >= :from")
    suspend fun deleteOverridesFrom(questId: Long, from: LocalDate)

    @Query("UPDATE quest_occurrence_overrides SET quest_id = :newQuestId WHERE quest_id = :questId AND occurrence_date >= :from")
    suspend fun moveOverridesFrom(questId: Long, from: LocalDate, newQuestId: Long)

    // endregion

    // region Activity log

    @Insert
    suspend fun insertActivity(event: ActivityEventEntity): Long

    @Query("SELECT * FROM activity_events WHERE occurrence_date = :date ORDER BY occurred_at, id")
    fun observeActivityOn(date: LocalDate): Flow<List<ActivityEventEntity>>

    @Query("SELECT * FROM activity_events WHERE quest_id = :questId ORDER BY occurred_at, id")
    suspend fun getActivityFor(questId: Long): List<ActivityEventEntity>

    @Query("DELETE FROM activity_events WHERE quest_id = :questId")
    suspend fun deleteActivityFor(questId: Long)

    @Query("DELETE FROM activity_events WHERE quest_id = :questId AND occurrence_date = :date")
    suspend fun deleteActivityOn(questId: Long, date: LocalDate)

    @Query("DELETE FROM activity_events WHERE quest_id = :questId AND occurrence_date >= :from")
    suspend fun deleteActivityFrom(questId: Long, from: LocalDate)

    @Query("UPDATE activity_events SET quest_id = :newQuestId WHERE quest_id = :questId AND occurrence_date >= :from")
    suspend fun moveActivityFrom(questId: Long, from: LocalDate, newQuestId: Long)

    // endregion
}
