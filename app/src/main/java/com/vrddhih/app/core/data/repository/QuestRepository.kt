package com.vrddhih.app.core.data.repository

import com.vrddhih.app.core.data.local.dao.QuestDao
import com.vrddhih.app.core.data.local.entity.QuestCompletionEntity
import com.vrddhih.app.core.data.local.entity.QuestEntity
import com.vrddhih.app.core.data.local.entity.QuestRecurrence
import com.vrddhih.app.core.data.local.entity.TaskPriority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

class QuestRepository(
    private val dao: QuestDao,
    private val clock: Clock,
) {
    fun observeActiveQuests(): Flow<List<QuestEntity>> = dao.observeActiveQuests()

    /** Quest definitions with an occurrence on [date]. */
    fun observeQuestsOn(date: LocalDate): Flow<List<QuestEntity>> =
        dao.observeQuestCandidatesOn(date).map { quests -> quests.filter { it.occursOn(date) } }

    fun observeCompletionsOn(date: LocalDate): Flow<List<QuestCompletionEntity>> = dao.observeCompletionsOn(date)

    suspend fun getQuest(id: Long): QuestEntity? = dao.getQuest(id)

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
        require(title.isNotBlank()) { "Quest title must not be blank" }
        require(xpReward >= 0) { "XP reward must not be negative" }
        require(endDate == null || !endDate.isBefore(startDate)) { "End date must not be before start date" }
        val now = Instant.now(clock)
        return dao.insertQuest(
            QuestEntity(
                title = title.trim(),
                notes = notes,
                priority = priority,
                xpReward = xpReward,
                startDate = startDate,
                recurrence = recurrence,
                endDate = endDate,
                goalId = goalId,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun updateQuest(quest: QuestEntity) = dao.updateQuest(quest.copy(updatedAt = Instant.now(clock)))

    suspend fun archiveQuest(id: Long) = dao.archiveQuest(id, Instant.now(clock))

    suspend fun deleteQuest(quest: QuestEntity) = dao.deleteQuest(quest)

    /** Records completion of the occurrence on [date]. Returns false if it was already completed. */
    suspend fun completeOccurrence(questId: Long, date: LocalDate, xpAwarded: Int): Boolean =
        dao.insertCompletion(
            QuestCompletionEntity(
                questId = questId,
                occurrenceDate = date,
                completedAt = Instant.now(clock),
                xpAwarded = xpAwarded,
            ),
        ) != -1L

    /** Returns false if there was no completion to remove. */
    suspend fun undoCompletion(questId: Long, date: LocalDate): Boolean = dao.deleteCompletion(questId, date) > 0
}

/** Whether this quest definition has an occurrence on [date]. Archived quests never occur. */
fun QuestEntity.occursOn(date: LocalDate): Boolean {
    if (isArchived || date.isBefore(startDate)) return false
    if (recurrence == QuestRecurrence.NONE) return date == startDate
    if (endDate != null && date.isAfter(endDate)) return false
    return when (recurrence) {
        QuestRecurrence.DAILY -> true
        QuestRecurrence.WEEKLY -> date.dayOfWeek == startDate.dayOfWeek
        QuestRecurrence.NONE -> false
    }
}
