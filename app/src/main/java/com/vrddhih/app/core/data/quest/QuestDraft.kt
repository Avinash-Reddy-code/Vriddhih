package com.vrddhih.app.core.data.quest

import com.vrddhih.app.core.data.local.entity.CATEGORY_OTHER_ID
import com.vrddhih.app.core.data.local.entity.QuestEntity
import com.vrddhih.app.core.data.local.entity.QuestRecurrence
import com.vrddhih.app.core.data.local.entity.TaskPriority
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

enum class QuestDraftError {
    TITLE_REQUIRED,
    TITLE_TOO_LONG,
    DEADLINE_BEFORE_DATE,
    END_BEFORE_START,
    INTERVAL_OUT_OF_RANGE,
    NO_WEEKDAYS,
}

/** Which occurrences an edit or delete of a recurring quest applies to. One-off quests always use [ALL]. */
enum class QuestScope { OCCURRENCE, FUTURE, ALL }

/**
 * User input for creating or editing a quest definition. Only [title] and [scheduledDate] are required.
 *
 * [intervalText] is the raw string from the interval input field. It is validated on save and converted
 * to a positive integer then. It is never overwritten by recomposition, so typing "1", "10", etc. works
 * correctly without the field resetting mid-input.
 *
 * [categoryId] is the stable ID of the selected [com.vrddhih.app.core.data.local.entity.CategoryEntity].
 * Null and empty string represent unassigned (defaults to Other at read time).
 */
data class QuestDraft(
    val title: String,
    val scheduledDate: LocalDate,
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueTime: LocalTime? = null,
    val deadline: LocalDate? = null,
    val categoryId: String? = null,
    val recurrence: QuestRecurrence = QuestRecurrence.NONE,
    /** Raw text for the recurrence interval input. Must be a positive integer string 1–999 when saving. */
    val intervalText: String = "1",
    val weekdays: Set<DayOfWeek> = emptySet(),
    val endDate: LocalDate? = null,
) {
    /** Parsed interval, or null if [intervalText] is not a valid positive integer. */
    val parsedInterval: Int?
        get() = intervalText.toIntOrNull()?.takeIf { it in 1..MAX_INTERVAL }

    fun validate(): Set<QuestDraftError> = buildSet {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) add(QuestDraftError.TITLE_REQUIRED)
        if (trimmed.length > MAX_TITLE_LENGTH) add(QuestDraftError.TITLE_TOO_LONG)
        // End date / deadline boundary must never be before scheduled start date
        val effectiveEnd = endDate ?: deadline
        if (effectiveEnd != null && effectiveEnd.isBefore(scheduledDate)) {
            add(QuestDraftError.END_BEFORE_START)
        }
        if (deadline != null && deadline.isBefore(scheduledDate)) {
            add(QuestDraftError.DEADLINE_BEFORE_DATE)
        }
        if (recurrence != QuestRecurrence.NONE) {
            if (parsedInterval == null) add(QuestDraftError.INTERVAL_OUT_OF_RANGE)
            if (recurrence == QuestRecurrence.WEEKDAYS && weekdays.isEmpty()) add(QuestDraftError.NO_WEEKDAYS)
        }
    }

    /** Applies this draft to [base] (or a new entity). Fields irrelevant to the chosen recurrence are cleared. */
    fun toEntity(now: Instant, base: QuestEntity? = null): QuestEntity {
        val recurring = recurrence != QuestRecurrence.NONE
        val interval = parsedInterval ?: 1
        val resolvedCategoryId = categoryId?.takeIf { it.isNotBlank() }
        val effectiveDeadline = if (recurring) null else (endDate ?: deadline)
        val effectiveEndDate = if (recurring) (endDate ?: deadline) else null
        val fields = (base ?: QuestEntity(title = "", startDate = scheduledDate, createdAt = now, updatedAt = now)).copy(
            title = title.trim(),
            notes = description.trim(),
            priority = priority,
            startDate = scheduledDate,
            dueTime = dueTime,
            deadline = effectiveDeadline,
            categoryId = resolvedCategoryId,
            recurrence = recurrence,
            recurrenceInterval = if (recurring) interval else 1,
            recurrenceDaysMask = if (recurrence == QuestRecurrence.WEEKDAYS) QuestEntity.daysMaskOf(weekdays) else 0,
            endDate = effectiveEndDate,
            updatedAt = now,
        )
        return fields
    }

    companion object {
        const val MAX_TITLE_LENGTH = 120
        const val MAX_INTERVAL = 999

        fun from(quest: QuestEntity): QuestDraft {
            val effectiveEndDate = quest.endDate ?: (if (quest.recurrence == QuestRecurrence.NONE) quest.deadline else null)
            val effectiveDeadline = quest.deadline ?: (if (quest.recurrence == QuestRecurrence.NONE) quest.endDate else null)
            return QuestDraft(
                title = quest.title,
                scheduledDate = quest.startDate,
                description = quest.notes,
                priority = quest.priority,
                dueTime = quest.dueTime,
                deadline = effectiveDeadline,
                categoryId = quest.categoryId,
                recurrence = quest.recurrence,
                intervalText = quest.recurrenceInterval.toString(),
                weekdays = quest.recurrenceDays,
                endDate = effectiveEndDate,
            )
        }
    }
}
