package com.vrddhih.app.core.data.repository

import com.vrddhih.app.core.data.local.dao.GoalDao
import com.vrddhih.app.core.data.local.entity.GoalEntity
import com.vrddhih.app.core.data.local.entity.GoalWithMilestones
import com.vrddhih.app.core.data.local.entity.MilestoneEntity
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

class GoalRepository(
    private val dao: GoalDao,
    private val clock: Clock,
) {
    fun observeActiveGoals(): Flow<List<GoalEntity>> = dao.observeActiveGoals()

    fun observeGoalWithMilestones(id: Long): Flow<GoalWithMilestones?> = dao.observeGoalWithMilestones(id)

    suspend fun addGoal(
        title: String,
        category: String = "",
        targetDate: LocalDate? = null,
        notes: String = "",
    ): Long {
        require(title.isNotBlank()) { "Goal title must not be blank" }
        val now = Instant.now(clock)
        return dao.insertGoal(
            GoalEntity(
                title = title.trim(),
                notes = notes,
                category = category,
                targetDate = targetDate,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun updateGoal(goal: GoalEntity) = dao.updateGoal(goal.copy(updatedAt = Instant.now(clock)))

    suspend fun deleteGoal(goal: GoalEntity) = dao.deleteGoal(goal)

    suspend fun addMilestone(goalId: Long, title: String, position: Int): Long {
        require(title.isNotBlank()) { "Milestone title must not be blank" }
        return dao.insertMilestone(
            MilestoneEntity(goalId = goalId, title = title.trim(), position = position, createdAt = Instant.now(clock)),
        )
    }

    suspend fun setMilestoneCompleted(milestoneId: Long, completed: Boolean): Boolean =
        dao.setMilestoneCompletedAt(milestoneId, if (completed) Instant.now(clock) else null) > 0
}
