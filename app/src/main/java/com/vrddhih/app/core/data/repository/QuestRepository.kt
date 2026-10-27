package com.vrddhih.app.core.data.repository

import androidx.room.withTransaction
import com.vrddhih.app.core.data.local.VrddhihDatabase
import com.vrddhih.app.core.data.local.entity.ActivityEventEntity
import com.vrddhih.app.core.data.local.entity.ActivityType
import com.vrddhih.app.core.data.local.entity.QuestCompletionEntity
import com.vrddhih.app.core.data.local.entity.QuestEntity
import com.vrddhih.app.core.data.local.entity.QuestOccurrenceOverrideEntity
import com.vrddhih.app.core.data.local.entity.QuestRecurrence
import com.vrddhih.app.core.data.local.entity.TaskPriority
import com.vrddhih.app.core.data.quest.DayLog
import com.vrddhih.app.core.data.quest.QuestBook
import com.vrddhih.app.core.data.quest.QuestDraft
import com.vrddhih.app.core.data.quest.QuestOccurrence
import com.vrddhih.app.core.data.quest.occurrencesOn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Quest definitions, occurrences, completions and the task activity log.
 *
 * Completion policy: completing inserts one completion row (unique per occurrence) and one QUEST_COMPLETED event.
 * Reopening deletes the completion row and appends a QUEST_REOPENED event. Events are written only when the state
 * actually changes, so repeated taps never create duplicates. Multi-row changes run in a single transaction.
 */
class QuestRepository(
    private val db: VrddhihDatabase,
    private val clock: Clock,
) {
    private val dao = db.questDao()

    private fun now(): Instant = Instant.now(clock)

    // region Reads

    fun observeActiveQuests(): Flow<List<QuestEntity>> = dao.observeActiveQuests()

    fun observeQuest(id: Long): Flow<QuestEntity?> = dao.observeQuest(id)

    /** Visible occurrences scheduled on [date], with overrides and completion state applied. */
    fun observeOccurrencesOn(date: LocalDate): Flow<List<QuestOccurrence>> = combine(
        dao.observeQuestCandidatesOn(date),
        dao.observeCompletionsOn(date),
        dao.observeOverridesOn(date),
    ) { quests, completions, overrides -> occurrencesOn(date, quests, completions, overrides) }

    /** Quest definitions with a visible occurrence on [date]. */
    fun observeQuestsOn(date: LocalDate): Flow<List<QuestEntity>> = observeOccurrencesOn(date).map { list -> list.map { it.quest } }

    fun observeCompletionsOn(date: LocalDate): Flow<List<QuestCompletionEntity>> = dao.observeCompletionsOn(date)

    fun observeDayLog(date: LocalDate): Flow<DayLog> = combine(
        dao.observeCompletionCountOn(date),
        dao.observeActivityOn(date),
    ) { completed, events -> DayLog(date, completed, events) }

    fun observeQuestBook(): Flow<QuestBook> = combine(
        dao.observeActiveQuests(),
        dao.observeAllCompletions(),
        dao.observeAllOverrides(),
        ::QuestBook,
    )

    suspend fun getQuest(id: Long): QuestEntity? = dao.getQuest(id)

    suspend fun getOverride(questId: Long, date: LocalDate): QuestOccurrenceOverrideEntity? = dao.getOverride(questId, date)

    suspend fun getActivityFor(questId: Long): List<ActivityEventEntity> = dao.getActivityFor(questId)

    // endregion

    // region Create and edit

    suspend fun createQuest(draft: QuestDraft): Long {
        requireValid(draft)
        return dao.insertQuest(draft.toEntity(now()))
    }

    /** Edits the whole definition ("all occurrences"). History is kept because it is keyed by quest id and date. */
    suspend fun updateQuest(questId: Long, draft: QuestDraft) {
        requireValid(draft)
        val existing = requireNotNull(dao.getQuest(questId)) { "Quest $questId not found" }
        dao.updateQuest(draft.toEntity(now(), base = existing))
    }

    /**
     * "This and future": ends the current definition the day before [from] and creates a new definition starting
     * on [from]. Completions, overrides and events on or after [from] move to the new definition; earlier history
     * stays with the original. Returns the id of the definition that owns [from].
     */
    suspend fun updateFromDate(questId: Long, from: LocalDate, draft: QuestDraft): Long = db.withTransaction {
        val existing = requireNotNull(dao.getQuest(questId)) { "Quest $questId not found" }
        if (!existing.isRecurring || !from.isAfter(existing.startDate)) {
            updateQuest(questId, draft)
            return@withTransaction questId
        }
        val split = draft.copy(scheduledDate = from)
        requireValid(split)
        val now = now()
        dao.updateQuest(existing.copy(endDate = from.minusDays(1), updatedAt = now))
        val newId = dao.insertQuest(
            split.toEntity(now).copy(goalId = existing.goalId, xpReward = existing.xpReward),
        )
        dao.moveCompletionsFrom(questId, from, newId)
        dao.moveOverridesFrom(questId, from, newId)
        dao.moveActivityFrom(questId, from, newId)
        newId
    }

    /** "This occurrence only". Values equal to the definition are stored as null so later series edits still apply. */
    suspend fun updateOccurrence(
        questId: Long,
        date: LocalDate,
        title: String,
        description: String,
        priority: TaskPriority,
        dueTime: LocalTime?,
    ) = db.withTransaction {
        require(title.isNotBlank()) { "Quest title must not be blank" }
        val quest = requireNotNull(dao.getQuest(questId)) { "Quest $questId not found" }
        val existing = dao.getOverride(questId, date)
        dao.upsertOverride(
            QuestOccurrenceOverrideEntity(
                id = existing?.id ?: 0,
                questId = questId,
                occurrenceDate = date,
                isSkipped = false,
                title = title.trim().takeIf { it != quest.title },
                notes = description.trim().takeIf { it != quest.notes },
                priority = priority.takeIf { it != quest.priority },
                dueTime = dueTime.takeIf { it != quest.dueTime },
                updatedAt = now(),
            ),
        )
    }

    // endregion

    // region Complete and reopen

    /** Returns false (and records nothing) if the occurrence was already completed or the quest doesn't exist. */
    suspend fun completeOccurrence(questId: Long, date: LocalDate, xpAwarded: Int = 0): Boolean = db.withTransaction {
        val quest = dao.getQuest(questId) ?: return@withTransaction false
        val now = now()
        val inserted = dao.insertCompletion(
            QuestCompletionEntity(questId = questId, occurrenceDate = date, completedAt = now, xpAwarded = xpAwarded),
        ) != -1L
        if (inserted) logEvent(ActivityType.QUEST_COMPLETED, quest, date, now)
        inserted
    }

    /** Returns false (and records nothing) if there was no completion to remove. */
    suspend fun reopenOccurrence(questId: Long, date: LocalDate): Boolean = db.withTransaction {
        val quest = dao.getQuest(questId) ?: return@withTransaction false
        val removed = dao.deleteCompletion(questId, date) > 0
        if (removed) logEvent(ActivityType.QUEST_REOPENED, quest, date, now())
        removed
    }

    suspend fun undoCompletion(questId: Long, date: LocalDate): Boolean = reopenOccurrence(questId, date)

    private suspend fun logEvent(type: ActivityType, quest: QuestEntity, date: LocalDate, at: Instant) {
        val title = dao.getOverride(quest.id, date)?.title ?: quest.title
        dao.insertActivity(
            ActivityEventEntity(type = type, questId = quest.id, questTitle = title, occurrenceDate = date, occurredAt = at),
        )
    }

    // endregion

    // region Delete

    /** Removes one occurrence of a recurring quest (a one-off quest is deleted entirely), with that day's history. */
    suspend fun deleteOccurrence(questId: Long, date: LocalDate) = db.withTransaction {
        val quest = dao.getQuest(questId) ?: return@withTransaction
        if (!quest.isRecurring) {
            deleteQuest(questId)
            return@withTransaction
        }
        val existing = dao.getOverride(questId, date)
        dao.upsertOverride(
            (existing ?: QuestOccurrenceOverrideEntity(questId = questId, occurrenceDate = date, updatedAt = now()))
                .copy(isSkipped = true, updatedAt = now()),
        )
        dao.deleteCompletion(questId, date)
        dao.deleteActivityOn(questId, date)
    }

    /** Ends a recurring quest before [from]; history before [from] is kept. Deletes entirely if [from] is the first day. */
    suspend fun deleteFromDate(questId: Long, from: LocalDate) = db.withTransaction {
        val quest = dao.getQuest(questId) ?: return@withTransaction
        if (!quest.isRecurring || !from.isAfter(quest.startDate)) {
            deleteQuest(questId)
            return@withTransaction
        }
        dao.updateQuest(quest.copy(endDate = from.minusDays(1), updatedAt = now()))
        dao.deleteCompletionsFrom(questId, from)
        dao.deleteOverridesFrom(questId, from)
        dao.deleteActivityFrom(questId, from)
    }

    /** Deletes the definition with all of its completions, overrides and activity events. */
    suspend fun deleteQuest(questId: Long) = db.withTransaction {
        dao.deleteActivityFor(questId)
        dao.deleteQuestById(questId)
    }

    suspend fun deleteQuest(quest: QuestEntity) = deleteQuest(quest.id)

    suspend fun archiveQuest(id: Long) = dao.archiveQuest(id, now())

    // endregion

    // region Convenience for simple callers and tests

    suspend fun addQuest(
        title: String,
        startDate: LocalDate,
        priority: TaskPriority = TaskPriority.MEDIUM,
        xpReward: Int = 10,
        recurrence: QuestRecurrence = QuestRecurrence.NONE,
        endDate: LocalDate? = null,
        notes: String = "",
        goalId: Long? = null,
    ): Long {
        require(xpReward >= 0) { "XP reward must not be negative" }
        val draft = QuestDraft(
            title = title,
            scheduledDate = startDate,
            description = notes,
            priority = priority,
            recurrence = recurrence,
            endDate = endDate,
        )
        requireValid(draft)
        return dao.insertQuest(draft.toEntity(now()).copy(xpReward = xpReward, goalId = goalId))
    }

    suspend fun updateQuest(quest: QuestEntity) = dao.updateQuest(quest.copy(updatedAt = now()))

    // endregion

    private fun requireValid(draft: QuestDraft) {
        val errors = draft.validate()
        require(errors.isEmpty()) { "Invalid quest: $errors" }
    }
}

