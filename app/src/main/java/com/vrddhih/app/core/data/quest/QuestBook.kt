package com.vrddhih.app.core.data.quest

import com.vrddhih.app.core.data.local.entity.ActivityEventEntity
import com.vrddhih.app.core.data.local.entity.ActivityType
import com.vrddhih.app.core.data.local.entity.QuestCompletionEntity
import com.vrddhih.app.core.data.local.entity.QuestEntity
import com.vrddhih.app.core.data.local.entity.QuestOccurrenceOverrideEntity
import java.time.LocalDate

/** Snapshot of all active quest data used to build the Quests screen views. */
data class QuestBook(
    val quests: List<QuestEntity>,
    val completions: List<QuestCompletionEntity>,
    val overrides: List<QuestOccurrenceOverrideEntity>,
) {
    private val questById = quests.associateBy { it.id }
    private val completionIndex = completions.associateBy { it.questId to it.occurrenceDate }
    private val overrideIndex = overrides.associateBy { it.questId to it.occurrenceDate }

    fun occurrence(quest: QuestEntity, date: LocalDate) =
        QuestOccurrence(quest, date, overrideIndex[quest.id to date], completionIndex[quest.id to date])

    private fun isSkipped(quest: QuestEntity, date: LocalDate) = overrideIndex[quest.id to date]?.isSkipped == true

    /** First visible occurrence on or after [from] matching [predicate], looking at most [horizonDays] ahead. */
    fun firstOccurrence(
        quest: QuestEntity,
        from: LocalDate,
        horizonDays: Long = 400,
        predicate: (QuestOccurrence) -> Boolean = { true },
    ): QuestOccurrence? = quest.occurrencesBetween(from, from.plusDays(horizonDays)).asSequence()
        .filterNot { isSkipped(quest, it) }
        .map { occurrence(quest, it) }
        .firstOrNull(predicate)

    /** The occurrence that represents a definition in "All": its next occurrence from today, else its latest one. */
    fun representative(quest: QuestEntity, today: LocalDate): QuestOccurrence? {
        if (!quest.isRecurring) return if (isSkipped(quest, quest.startDate)) null else occurrence(quest, quest.startDate)
        return firstOccurrence(quest, today)
            ?: quest.occurrencesBetween(quest.startDate, today).lastOrNull { !isSkipped(quest, it) }?.let { occurrence(quest, it) }
    }

    fun completedOccurrences(): List<QuestOccurrence> =
        completions.mapNotNull { completion -> questById[completion.questId]?.let { occurrence(it, completion.occurrenceDate) } }
}

enum class QuestFilter { ALL, TODAY, UPCOMING, OVERDUE, COMPLETED }

enum class QuestSort { DEADLINE, PRIORITY, CREATED }

/** Pure view logic for the Quests screen. Every filter uses local calendar days relative to [today]. */
object QuestLists {

    fun build(
        book: QuestBook,
        filter: QuestFilter,
        today: LocalDate,
        query: String = "",
        sort: QuestSort = QuestSort.DEADLINE,
        categoryNames: Map<String, String> = emptyMap(),
    ): List<QuestOccurrence> {
        val items = when (filter) {
            QuestFilter.ALL -> book.quests.mapNotNull { book.representative(it, today) }
            QuestFilter.TODAY -> occurrencesOn(today, book.quests, book.completions, book.overrides)
            QuestFilter.UPCOMING -> book.quests.mapNotNull { quest ->
                book.firstOccurrence(quest, today.plusDays(1)) { !it.isCompleted }
            }
            QuestFilter.OVERDUE -> book.quests
                .filter { !it.isRecurring }
                .mapNotNull { book.representative(it, today) }
                .filter { it.isOverdue(today) }
            QuestFilter.COMPLETED -> book.completedOccurrences()
        }
        val needle = query.trim()
        val matching = if (needle.isEmpty()) {
            items
        } else {
            items.filter { item ->
                val categoryName = item.quest.categoryId?.let { categoryNames[it] } ?: ""
                listOf(item.title, item.notes, item.quest.category, categoryName).any { it.contains(needle, ignoreCase = true) }
            }
        }
        if (filter == QuestFilter.COMPLETED) return matching.sortedByDescending { it.completion?.completedAt }
        return matching.sortedWith(comparatorFor(sort))
    }

    fun comparatorFor(sort: QuestSort): Comparator<QuestOccurrence> = when (sort) {
        QuestSort.DEADLINE -> compareBy<QuestOccurrence> { it.effectiveDeadline ?: it.date }
            .thenBy { it.priority.ordinal }
            .thenBy(nullsLast()) { it.dueTime }
        QuestSort.PRIORITY -> compareBy<QuestOccurrence> { it.priority.ordinal }
            .thenBy { it.effectiveDeadline ?: it.date }
        QuestSort.CREATED -> compareByDescending<QuestOccurrence> { it.quest.createdAt }
    }.thenBy { it.questId }
}

/** Day Log read model: historical activity for one day, built only from persisted rows. */
data class DayLog(
    val date: LocalDate,
    /** Occurrences of that day that are currently completed. */
    val completedCount: Int,
    /** Completion and reopen events, oldest first. */
    val events: List<ActivityEventEntity>,
) {
    val reopenedCount: Int get() = events.count { it.type == ActivityType.QUEST_REOPENED }
    val isEmpty: Boolean get() = completedCount == 0 && events.isEmpty()
}
