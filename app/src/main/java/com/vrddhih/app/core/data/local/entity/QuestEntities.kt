package com.vrddhih.app.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/** Stable ID for the built-in "Other" catch-all category, used when no category is assigned. */
const val CATEGORY_OTHER_ID = "builtin_other"

/** Declared from most to least urgent; [ordinal] is used for sorting. */
enum class TaskPriority { CRITICAL, HIGH, MEDIUM, LOW }

/**
 * How a quest definition repeats. Occurrences are derived on demand from the rule and never stored as rows.
 * [QuestEntity.recurrenceInterval] makes DAILY/WEEKDAYS/WEEKLY/MONTHLY repeat every N days/weeks/months.
 */
enum class QuestRecurrence {
    /** A single occurrence on [QuestEntity.startDate]. */
    NONE,

    /** Every N days from [QuestEntity.startDate]. */
    DAILY,

    /** On the weekdays in [QuestEntity.recurrenceDaysMask], every N weeks. */
    WEEKDAYS,

    /** On the weekday of [QuestEntity.startDate], every N weeks. */
    WEEKLY,

    /** On the day-of-month of [QuestEntity.startDate] every N months, clamped to shorter months (31st → Feb 28/29). */
    MONTHLY,
}

/**
 * A quest/task definition. For recurring quests this is the template, not an individual occurrence.
 *
 * Time semantics: [startDate], [endDate] and [deadline] are calendar days and [dueTime] is a wall-clock time,
 * all without a time zone. A quest due at 09:00 on 25 Oct stays 09:00 on 25 Oct if the device changes zone.
 */
@Entity(
    tableName = "quests",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            // No cascade: deleting a category must not delete tasks. Repository reassigns to Other first.
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("goal_id"), Index("start_date"), Index("category_id")],
)
data class QuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    /** User-facing "description". */
    @ColumnInfo(defaultValue = "") val notes: String = "",
    @ColumnInfo(defaultValue = "MEDIUM") val priority: TaskPriority = TaskPriority.MEDIUM,
    @ColumnInfo(name = "xp_reward", defaultValue = "10") val xpReward: Int = 10,
    /** The scheduled day for one-off quests, or the first occurrence for recurring quests. */
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(defaultValue = "NONE") val recurrence: QuestRecurrence = QuestRecurrence.NONE,
    /** Last day (inclusive) a recurring quest can occur. Null means no end. Ignored for one-off quests. */
    @ColumnInfo(name = "end_date") val endDate: LocalDate? = null,
    @ColumnInfo(name = "goal_id") val goalId: Long? = null,
    @ColumnInfo(name = "is_archived", defaultValue = "0") val isArchived: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    // Added in schema v2.
    @ColumnInfo(name = "due_time") val dueTime: LocalTime? = null,
    /** Last acceptable day for a one-off quest. Overdue = not completed and (deadline ?: startDate) < today. */
    val deadline: LocalDate? = null,
    /**
     * Legacy plain-text category field from schema v2. Kept to avoid destructive migration.
     * All new saves use [categoryId] instead. Never read for display.
     */
    @ColumnInfo(defaultValue = "") val category: String = "",
    /** FK to [CategoryEntity.id]. Null is treated as the "Other" built-in category at read time. */
    @ColumnInfo(name = "category_id") val categoryId: String? = null,
    @ColumnInfo(name = "recurrence_interval", defaultValue = "1") val recurrenceInterval: Int = 1,
    /** ISO weekday bits (Monday = bit 0) used by [QuestRecurrence.WEEKDAYS]. */
    @ColumnInfo(name = "recurrence_days_mask", defaultValue = "0") val recurrenceDaysMask: Int = 0,
) {
    val isRecurring: Boolean get() = recurrence != QuestRecurrence.NONE

    val recurrenceDays: Set<DayOfWeek>
        get() = DayOfWeek.entries.filterTo(mutableSetOf()) { recurrenceDaysMask and (1 shl (it.value - 1)) != 0 }

    companion object {
        fun daysMaskOf(days: Collection<DayOfWeek>): Int = days.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }
    }
}

/**
 * Completion of one quest occurrence. The unique (quest_id, occurrence_date) index makes it impossible to
 * complete the same occurrence twice.
 */
@Entity(
    tableName = "quest_completions",
    foreignKeys = [
        ForeignKey(
            entity = QuestEntity::class,
            parentColumns = ["id"],
            childColumns = ["quest_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["quest_id", "occurrence_date"], unique = true), Index("occurrence_date")],
)
data class QuestCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "quest_id") val questId: Long,
    /** The scheduled day this completion belongs to, which may differ from the day it was ticked off. */
    @ColumnInfo(name = "occurrence_date") val occurrenceDate: LocalDate,
    @ColumnInfo(name = "completed_at") val completedAt: Instant,
    @ColumnInfo(name = "xp_awarded", defaultValue = "0") val xpAwarded: Int = 0,
)

/**
 * A change to one occurrence of a recurring quest ("edit/delete this occurrence only"). Null fields fall back
 * to the definition. [isSkipped] removes the occurrence without touching any other day.
 */
@Entity(
    tableName = "quest_occurrence_overrides",
    foreignKeys = [
        ForeignKey(
            entity = QuestEntity::class,
            parentColumns = ["id"],
            childColumns = ["quest_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["quest_id", "occurrence_date"], unique = true), Index("occurrence_date")],
)
data class QuestOccurrenceOverrideEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "quest_id") val questId: Long,
    @ColumnInfo(name = "occurrence_date") val occurrenceDate: LocalDate,
    @ColumnInfo(name = "is_skipped", defaultValue = "0") val isSkipped: Boolean = false,
    val title: String? = null,
    val notes: String? = null,
    val priority: TaskPriority? = null,
    @ColumnInfo(name = "due_time") val dueTime: LocalTime? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)

enum class ActivityType { QUEST_COMPLETED, QUEST_REOPENED }

/**
 * Append-only task activity log shown in the Day Log. Rows are written only when the completion state actually
 * changes, so repeated taps can't create duplicates. [occurrenceDate] is the day the event belongs to.
 * No foreign key: the title is a snapshot, and rows for a quest are removed explicitly when it is deleted.
 */
@Entity(
    tableName = "activity_events",
    indices = [Index("occurrence_date"), Index("quest_id")],
)
data class ActivityEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: ActivityType,
    @ColumnInfo(name = "quest_id") val questId: Long?,
    @ColumnInfo(name = "quest_title") val questTitle: String,
    @ColumnInfo(name = "occurrence_date") val occurrenceDate: LocalDate,
    @ColumnInfo(name = "occurred_at") val occurredAt: Instant,
)
