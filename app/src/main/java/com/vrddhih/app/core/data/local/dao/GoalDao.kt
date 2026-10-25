package com.vrddhih.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.vrddhih.app.core.data.local.entity.GoalEntity
import com.vrddhih.app.core.data.local.entity.GoalWithMilestones
import com.vrddhih.app.core.data.local.entity.MilestoneEntity
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

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeGoalWithMilestones(id: Long): Flow<GoalWithMilestones?>

    @Insert
    suspend fun insertMilestone(milestone: MilestoneEntity): Long

    @Query("UPDATE goal_milestones SET completed_at = :completedAt WHERE id = :id")
    suspend fun setMilestoneCompletedAt(id: Long, completedAt: Instant?): Int

    @Delete
    suspend fun deleteMilestone(milestone: MilestoneEntity)

    @Query("SELECT * FROM goal_milestones WHERE goal_id = :goalId ORDER BY position, id")
    suspend fun getMilestones(goalId: Long): List<MilestoneEntity>
}
