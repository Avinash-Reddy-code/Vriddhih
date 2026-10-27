package com.vrddhih.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.vrddhih.app.core.data.local.entity.GoalEntity
import com.vrddhih.app.core.data.local.entity.GoalWithMilestones
import com.vrddhih.app.core.data.local.entity.MilestoneEntity
import com.vrddhih.app.core.data.local.entity.GoalNumericTarget
import com.vrddhih.app.core.data.local.entity.GoalCompletionTarget
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface GoalDao {
    @Insert
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoal(id: Long): GoalEntity?

    @Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY target_date IS NULL, target_date, id")
    fun observeActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY target_date IS NULL, target_date, id")
    suspend fun getActiveGoals(): List<GoalEntity>

    @Query("SELECT * FROM goals WHERE status = 'COMPLETED' ORDER BY completed_at DESC")
    fun observeCompletedGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE status = 'ARCHIVED' ORDER BY updated_at DESC")
    fun observeArchivedGoals(): Flow<List<GoalEntity>>

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeGoalWithMilestones(id: Long): Flow<GoalWithMilestones?>

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalWithMilestones(id: Long): GoalWithMilestones?

    @Insert
    suspend fun insertMilestone(milestone: MilestoneEntity): Long

    @Query("UPDATE goal_milestones SET completed_at = :completedAt WHERE id = :id")
    suspend fun setMilestoneCompletedAt(id: Long, completedAt: Instant?): Int

    @Delete
    suspend fun deleteMilestone(milestone: MilestoneEntity)

    @Query("SELECT * FROM goal_milestones WHERE goal_id = :goalId ORDER BY position, id")
    suspend fun getMilestones(goalId: Long): List<MilestoneEntity>

    @Query("SELECT * FROM goal_milestones WHERE goal_id = :goalId ORDER BY position, id")
    fun observeMilestones(goalId: Long): Flow<List<MilestoneEntity>>

    @Query("DELETE FROM goal_milestones WHERE goal_id = :goalId")
    suspend fun deleteMilestonesForGoal(goalId: Long): Int

    @Insert
    suspend fun insertNumericTarget(target: GoalNumericTarget): Long

    @Update
    suspend fun updateNumericTarget(target: GoalNumericTarget)

    @Query("SELECT * FROM goal_numeric_target WHERE goal_id = :goalId")
    suspend fun getNumericTarget(goalId: Long): GoalNumericTarget?

    @Query("SELECT * FROM goal_numeric_target WHERE goal_id = :goalId")
    fun observeNumericTarget(goalId: Long): Flow<GoalNumericTarget?>

    @Query("UPDATE goal_numeric_target SET current_value = :currentValue WHERE goal_id = :goalId")
    suspend fun updateNumericTargetCurrentValue(goalId: Long, currentValue: Long): Int

    @Delete
    suspend fun deleteNumericTarget(target: GoalNumericTarget)

    @Insert
    suspend fun insertCompletionTarget(target: GoalCompletionTarget): Long

    @Update
    suspend fun updateCompletionTarget(target: GoalCompletionTarget)

    @Query("SELECT * FROM goal_completion_target WHERE goal_id = :goalId")
    suspend fun getCompletionTarget(goalId: Long): GoalCompletionTarget?

    @Query("SELECT * FROM goal_completion_target WHERE goal_id = :goalId")
    fun observeCompletionTarget(goalId: Long): Flow<GoalCompletionTarget?>

    @Query("UPDATE goal_completion_target SET current_count = :currentCount WHERE goal_id = :goalId")
    suspend fun updateCompletionTargetCurrentCount(goalId: Long, currentCount: Int): Int

    @Delete
    suspend fun deleteCompletionTarget(target: GoalCompletionTarget)

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeGoalWithNumericTarget(id: Long): Flow<com.vrddhih.app.core.data.local.entity.GoalWithNumericTarget?>

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeGoalWithCompletionTarget(id: Long): Flow<com.vrddhih.app.core.data.local.entity.GoalWithCompletionTarget?>

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalWithAllConfigs(id: Long): GoalWithAllConfigs?

    @Transaction
    @Query("SELECT * FROM goals ORDER BY target_date IS NULL, target_date, id")
    fun observeAllGoalsWithAllConfigs(): Flow<List<GoalWithAllConfigs>>

    @Transaction
    @Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY target_date IS NULL, target_date, id")
    fun observeActiveGoalsWithAllConfigs(): Flow<List<GoalWithAllConfigs>>
}

data class GoalWithAllConfigs(
    @Embedded val goal: GoalEntity,
    @Relation(parentColumn = "id", entityColumn = "goal_id")
    val milestones: List<MilestoneEntity>,
    @Relation(parentColumn = "id", entityColumn = "goal_id")
    val numericTarget: GoalNumericTarget?,
    @Relation(parentColumn = "id", entityColumn = "goal_id")
    val completionTarget: GoalCompletionTarget?,
)
