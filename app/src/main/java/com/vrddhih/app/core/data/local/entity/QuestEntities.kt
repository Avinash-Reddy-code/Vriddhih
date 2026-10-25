package com.vrddhih.app.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

enum class TaskPriority { HIGH, MEDIUM, LOW }

/** How a quest definition repeats. Occurrences are calculated from the rule and never stored as rows. */
enum class QuestRecurrence {
    /** A single occurrence on [QuestEntity.startDate]. */
    NONE,

    /** Every day from [QuestEntity.startDate] to [QuestEntity.endDate]. */
    DAILY,

    /** Weekly on the weekday of [QuestEntity.startDate], up to [QuestEntity.endDate]. */
    WEEKLY,
}

/** A quest/task definition. For recurring quests this is the template, not an individual occurrence. */
@Entity(
    tableName = "quests",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("goal_id"), Index("start_date")],
)
data class QuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(defaultValue = "") val notes: String = "",
    @ColumnInfo(defaultValue = "MEDIUM") val priority: TaskPriority = TaskPriority.MEDIUM,
    @ColumnInfo(name = "xp_reward", defaultValue = "10") val xpReward: Int = 10,
    /** The due day for one-off quests, or the first occurrence for recurring quests. */
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(defaultValue = "NONE") val recurrence: QuestRecurrence = QuestRecurrence.NONE,
    /** Last day (inclusive) a recurring quest can occur. Null means no end. Ignored for one-off quests. */
    @ColumnInfo(name = "end_date") val endDate: LocalDate? = null,
    @ColumnInfo(name = "goal_id") val goalId: Long? = null,
    @ColumnInfo(name = "is_archived", defaultValue = "0") val isArchived: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)

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
