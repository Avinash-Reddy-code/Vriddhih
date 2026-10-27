package com.vrddhih.app.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.Instant
import java.time.LocalDate

enum class GoalType { MILESTONE_BASED, NUMERIC_TARGET, COMPLETION_BASED }

enum class GoalStatus { NOT_STARTED, ACTIVE, PAUSED, COMPLETED, ARCHIVED }

@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("status"), Index("category_id"), Index("target_date")],
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(defaultValue = "") val description: String = "",
    val type: GoalType = GoalType.MILESTONE_BASED,
    @ColumnInfo(name = "category_id") val categoryId: String? = null,
    @ColumnInfo(name = "start_date") val startDate: LocalDate? = null,
    @ColumnInfo(name = "target_date") val targetDate: LocalDate? = null,
    @ColumnInfo(defaultValue = "ACTIVE") val status: GoalStatus = GoalStatus.ACTIVE,
    @ColumnInfo(name = "completed_at") val completedAt: Instant? = null,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)

@Entity(
    tableName = "goal_milestones",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("goal_id")],
)
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "goal_id") val goalId: Long,
    val title: String,
    @ColumnInfo(defaultValue = "0") val position: Int = 0,
    /** Null until the milestone is completed. */
    @ColumnInfo(name = "completed_at") val completedAt: Instant? = null,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
)

@Entity(
    tableName = "goal_numeric_target",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("goal_id", unique = true)],
)
data class GoalNumericTarget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "goal_id") val goalId: Long,
    @ColumnInfo(name = "target_value") val targetValue: Long,
    @ColumnInfo(name = "current_value") val currentValue: Long = 0,
    val unit: String,
)

@Entity(
    tableName = "goal_completion_target",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("goal_id", unique = true)],
)
data class GoalCompletionTarget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "goal_id") val goalId: Long,
    @ColumnInfo(name = "target_count") val targetCount: Int,
    @ColumnInfo(name = "current_count") val currentCount: Int = 0,
)

data class GoalWithMilestones(
    @Embedded val goal: GoalEntity,
    @Relation(parentColumn = "id", entityColumn = "goal_id")
    val milestones: List<MilestoneEntity>,
)

data class GoalWithNumericTarget(
    @Embedded val goal: GoalEntity,
    @Relation(parentColumn = "id", entityColumn = "goal_id")
    val numericTarget: GoalNumericTarget?,
)

data class GoalWithCompletionTarget(
    @Embedded val goal: GoalEntity,
    @Relation(parentColumn = "id", entityColumn = "goal_id")
    val completionTarget: GoalCompletionTarget?,
)
