package com.vrddhih.app.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

enum class HabitType { YES_NO, NUMERIC, TIMER, CHECKLIST, LIMIT }

enum class HabitScheduleType {
    DAILY,
    EVERY_N_DAYS,
    WEEKLY_FREQUENCY,
    SELECTED_WEEKDAYS,
}

enum class NumericDirection { AT_LEAST, AT_MOST }

enum class HabitCompletionState { COMPLETED, PARTIALLY_COMPLETED, MISSED, SKIPPED, NOT_SCHEDULED }

@Entity(
    tableName = "habits",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("category_id"), Index("start_date")],
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(defaultValue = "") val description: String = "",
    val type: HabitType = HabitType.YES_NO,
    @ColumnInfo(defaultValue = "DAILY") val scheduleType: HabitScheduleType = HabitScheduleType.DAILY,
    @ColumnInfo(name = "schedule_interval", defaultValue = "1") val scheduleInterval: Int = 1,
    @ColumnInfo(name = "schedule_days_mask", defaultValue = "127") val scheduleDaysMask: Int = EVERY_DAY_MASK,
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(name = "end_date") val endDate: LocalDate? = null,
    @ColumnInfo(name = "category_id") val categoryId: String? = null,
    @ColumnInfo(name = "icon_key") val iconKey: String? = null,
    @ColumnInfo(name = "color_argb") val colorArgb: Int? = null,
    @ColumnInfo(name = "is_archived", defaultValue = "0") val isArchived: Boolean = false,
    @ColumnInfo(name = "xp_per_completion", defaultValue = "5") val xpPerCompletion: Int = 5,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
) {
    fun isScheduledOn(date: LocalDate): Boolean {
        if (date.isBefore(startDate)) return false
        if (endDate != null && date.isAfter(endDate)) return false
        
        return (scheduleDaysMask and date.dayOfWeek.maskBit()) != 0
    }

    companion object {
        const val EVERY_DAY_MASK = 0b111_1111

        fun maskOf(vararg days: DayOfWeek): Int = days.fold(0) { mask, day -> mask or day.maskBit() }

        internal fun DayOfWeek.maskBit(): Int = 1 shl (value - 1)
    }
}

/** A habit check-in for a calendar day. Unique per (habit_id, date), so repeated check-ins can't duplicate. */
@Entity(
    tableName = "habit_check_ins",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id", "date"], unique = true), Index("date")],
)
data class HabitCheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val date: LocalDate,
    @ColumnInfo(name = "checked_in_at") val checkedInAt: Instant,
)

@Entity(
    tableName = "habit_numeric_config",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habit_id", unique = true)],
)
data class HabitNumericConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    @ColumnInfo(name = "target_value") val targetValue: Long,
    val unit: String,
    val direction: NumericDirection,
    @ColumnInfo(name = "increment_value") val incrementValue: Long? = null,
)

@Entity(
    tableName = "habit_timer_config",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habit_id", unique = true)],
)
data class HabitTimerConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    @ColumnInfo(name = "target_duration_seconds") val targetDurationSeconds: Int,
    @ColumnInfo(name = "daily_target_seconds") val dailyTargetSeconds: Int,
)

@Entity(
    tableName = "habit_timer_sessions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habit_id"), Index("date"), Index(value = ["habit_id", "date"], unique = true)],
)
data class HabitTimerSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val date: LocalDate,
    @ColumnInfo(name = "started_at") val startedAt: Instant,
    @ColumnInfo(name = "paused_at") val pausedAt: Instant? = null,
    @ColumnInfo(name = "resumed_at") val resumedAt: Instant? = null,
    @ColumnInfo(name = "finished_at") val finishedAt: Instant? = null,
    @ColumnInfo(name = "total_seconds") val totalSeconds: Int,
    @ColumnInfo(name = "is_paused", defaultValue = "0") val isPaused: Boolean = false,
)

@Entity(
    tableName = "habit_checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habit_id")],
)
data class HabitChecklistItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val title: String,
    @ColumnInfo(defaultValue = "0") val position: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
)

@Entity(
    tableName = "habit_checklist_progress",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = HabitChecklistItem::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id", "date", "item_id"], unique = true), Index("date"), Index("item_id")],
)
data class HabitChecklistProgress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val date: LocalDate,
    @ColumnInfo(name = "item_id") val itemId: Long,
    @ColumnInfo(name = "completed_at") val completedAt: Instant? = null,
)

@Entity(
    tableName = "habit_limit_config",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habit_id", unique = true)],
)
data class HabitLimitConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    @ColumnInfo(name = "daily_limit") val dailyLimit: Long,
    val unit: String,
    @ColumnInfo(name = "allow_yes_no", defaultValue = "0") val allowYesNo: Boolean = false,
)

@Entity(
    tableName = "habit_excluded_dates",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id", "date"], unique = true)],
)
data class HabitExcludedDate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val date: LocalDate,
)

@Entity(
    tableName = "habit_daily_progress",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id", "date"], unique = true), Index("date"), Index("completion_state")],
)
data class HabitDailyProgress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val date: LocalDate,
    @ColumnInfo(name = "completion_state") val completionState: HabitCompletionState = HabitCompletionState.NOT_SCHEDULED,
    @ColumnInfo(name = "numeric_value") val numericValue: Long? = null,
    @ColumnInfo(name = "timer_seconds") val timerSeconds: Int? = null,
    @ColumnInfo(name = "note") val note: String? = null,
    @ColumnInfo(name = "completed_at") val completedAt: Instant? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)

@Entity(
    tableName = "habit_goal_links",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id", "goal_id"], unique = true), Index("goal_id")],
)
data class HabitGoalLink(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    @ColumnInfo(name = "goal_id") val goalId: Long,
    @ColumnInfo(name = "contribution_rule") val contributionRule: String? = null,
)
