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

enum class GoalStatus { ACTIVE, COMPLETED, ARCHIVED }

@Entity(tableName = "goals", indices = [Index("status")])
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(defaultValue = "") val notes: String = "",
    @ColumnInfo(defaultValue = "") val category: String = "",
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

data class GoalWithMilestones(
    @Embedded val goal: GoalEntity,
    @Relation(parentColumn = "id", entityColumn = "goal_id")
    val milestones: List<MilestoneEntity>,
)
