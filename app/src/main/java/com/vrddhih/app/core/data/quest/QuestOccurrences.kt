package com.vrddhih.app.core.data.quest

import com.vrddhih.app.core.data.local.entity.QuestCompletionEntity
import com.vrddhih.app.core.data.local.entity.QuestEntity
import com.vrddhih.app.core.data.local.entity.QuestOccurrenceOverrideEntity
import com.vrddhih.app.core.data.local.entity.QuestRecurrence
import com.vrddhih.app.core.data.local.entity.TaskPriority
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * Recurrence strategy: occurrences are DERIVED ON DEMAND from the definition and never pre-generated.
 * An occurrence's stable identity is (questId, date). Completions and overrides attach to that key, so
 * deriving the same day any number of times can never create duplicates.
 */
fun QuestEntity.occursOn(date: LocalDate): Boolean {
    if (isArchived || date.isBefore(startDate)) return false
    if (recurrence == QuestRecurrence.NONE) return date == startDate
    if (endDate != null && date.isAfter(endDate)) return false
    val every = recurrenceInterval.coerceAtLeast(1).toLong()
    return when (recurrence) {
        QuestRecurrence.NONE -> false
        QuestRecurrence.DAILY -> ChronoUnit.DAYS.between(startDate, date) % every == 0L
        QuestRecurrence.WEEKLY ->
            date.dayOfWeek == startDate.dayOfWeek && weeksBetween(startDate, date) % every == 0L
        QuestRecurrence.WEEKDAYS ->
            date.dayOfWeek in recurrenceDays && weeksBetween(startDate, date) % every == 0L
        QuestRecurrence.MONTHLY -> {
            val month = YearMonth.from(date)
            val months = ChronoUnit.MONTHS.between(YearMonth.from(startDate), month)
            months % every == 0L && date.dayOfMonth == minOf(startDate.dayOfMonth, month.lengthOfMonth())
        }
    }
}

/** Whole Sunday-started weeks between the weeks containing [from] and [to]. */
private fun weeksBetween(from: LocalDate, to: LocalDate): Long {
    val sunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)
    return ChronoUnit.WEEKS.between(from.with(sunday), to.with(sunday))
}

/** Occurrence dates in [from]..[to] (inclusive). Bounded by the range, so it can never be unbounded. */
fun QuestEntity.occurrencesBetween(from: LocalDate, to: LocalDate): List<LocalDate> {
    if (isArchived) return emptyList()
    val start = maxOf(from, startDate)
    val end = if (isRecurring && endDate != null) minOf(to, endDate) else to
    if (start.isAfter(end)) return emptyList()
    if (!isRecurring) return if (!startDate.isBefore(start) && !startDate.isAfter(end)) listOf(startDate) else emptyList()
    return generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }.filter(::occursOn).toList()
}

/** First occurrence on or after [date], searching at most [horizonDays] ahead. */
fun QuestEntity.nextOccurrenceOnOrAfter(date: LocalDate, horizonDays: Long = 400): LocalDate? =
    occurrencesBetween(date, date.plusDays(horizonDays)).firstOrNull()

/** One concrete occurrence of a quest, with its per-day override and completion applied. */
data class QuestOccurrence(
    val quest: QuestEntity,
    val date: LocalDate,
    val override: QuestOccurrenceOverrideEntity? = null,
    val completion: QuestCompletionEntity? = null,
) {
    val key: String get() = "${quest.id}@${date.toEpochDay()}"
    val questId: Long get() = quest.id
    val title: String get() = override?.title ?: quest.title
    val notes: String get() = override?.notes ?: quest.notes
    val priority: TaskPriority get() = override?.priority ?: quest.priority
    val dueTime: LocalTime? get() = override?.dueTime ?: quest.dueTime
    val isCompleted: Boolean get() = completion != null
    val isRecurring: Boolean get() = quest.isRecurring

    /** Only one-off quests become overdue; a missed recurring occurrence is simply missed. */
    val effectiveDeadline: LocalDate? get() = if (quest.isRecurring) null else quest.deadline ?: quest.startDate

    fun isOverdue(today: LocalDate): Boolean = !isCompleted && effectiveDeadline?.isBefore(today) == true
}

/** Display order: incomplete first, then priority, due time (untimed last) and title. */
val OccurrenceDisplayOrder: Comparator<QuestOccurrence> =
    compareBy<QuestOccurrence> { it.isCompleted }
        .thenBy { it.priority.ordinal }
        .thenBy(nullsLast()) { it.dueTime }
        .thenBy { it.title.lowercase() }
        .thenBy { it.questId }

/** Builds the visible occurrences for [date] from raw rows. Skipped occurrences are excluded. */
fun occurrencesOn(
    date: LocalDate,
    quests: List<QuestEntity>,
    completions: List<QuestCompletionEntity>,
    overrides: List<QuestOccurrenceOverrideEntity>,
): List<QuestOccurrence> {
    val completionByQuest = completions.filter { it.occurrenceDate == date }.associateBy { it.questId }
    val overrideByQuest = overrides.filter { it.occurrenceDate == date }.associateBy { it.questId }
    return quests
        .filter { it.occursOn(date) && overrideByQuest[it.id]?.isSkipped != true }
        .map { QuestOccurrence(it, date, overrideByQuest[it.id], completionByQuest[it.id]) }
        .sortedWith(OccurrenceDisplayOrder)
}
