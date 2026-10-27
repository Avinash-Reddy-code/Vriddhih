package com.vrddhih.app.core.data.repository

import androidx.room.withTransaction
import com.vrddhih.app.core.data.local.VrddhihDatabase
import com.vrddhih.app.core.data.local.dao.GoalDao
import com.vrddhih.app.core.data.local.dao.GoalWithAllConfigs
import com.vrddhih.app.core.data.local.entity.GoalCompletionTarget
import com.vrddhih.app.core.data.local.entity.GoalEntity
import com.vrddhih.app.core.data.local.entity.GoalNumericTarget
import com.vrddhih.app.core.data.local.entity.GoalStatus
import com.vrddhih.app.core.data.local.entity.GoalType
import com.vrddhih.app.core.data.local.entity.GoalWithCompletionTarget
import com.vrddhih.app.core.data.local.entity.GoalWithMilestones
import com.vrddhih.app.core.data.local.entity.GoalWithNumericTarget
import com.vrddhih.app.core.data.local.entity.MilestoneEntity
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

class GoalRepository(
    private val db: VrddhihDatabase,
    private val clock: Clock,
) {
    private val dao = db.goalDao()

    private fun now(): Instant = Instant.now(clock)

    fun observeActiveGoals(): Flow<List<GoalEntity>> = dao.observeActiveGoals()

    fun observeCompletedGoals(): Flow<List<GoalEntity>> = dao.observeCompletedGoals()

    fun observeArchivedGoals(): Flow<List<GoalEntity>> = dao.observeArchivedGoals()

    fun observeAllGoalsWithAllConfigs(): Flow<List<GoalWithAllConfigs>> = dao.observeAllGoalsWithAllConfigs()

    fun observeActiveGoalsWithAllConfigs(): Flow<List<GoalWithAllConfigs>> = dao.observeActiveGoalsWithAllConfigs()

    fun observeGoalWithMilestones(id: Long): Flow<GoalWithMilestones?> = dao.observeGoalWithMilestones(id)

    fun observeGoalWithNumericTarget(id: Long): Flow<GoalWithNumericTarget?> = dao.observeGoalWithNumericTarget(id)

    fun observeGoalWithCompletionTarget(id: Long): Flow<GoalWithCompletionTarget?> = dao.observeGoalWithCompletionTarget(id)

    suspend fun getGoal(id: Long): GoalEntity? = dao.getGoal(id)

    suspend fun getGoalWithAllConfigs(id: Long) = dao.getGoalWithAllConfigs(id)

    suspend fun addGoal(
        title: String,
        type: GoalType = GoalType.MILESTONE_BASED,
        categoryId: String? = null,
        startDate: LocalDate? = null,
        targetDate: LocalDate? = null,
        description: String = "",
    ): Long = db.withTransaction {
        require(title.isNotBlank()) { "Goal title must not be blank" }
        val now = now()
        dao.insertGoal(
            GoalEntity(
                title = title.trim(),
                type = type,
                categoryId = categoryId,
                startDate = startDate,
                targetDate = targetDate,
                description = description,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun updateGoal(goal: GoalEntity) = dao.updateGoal(goal.copy(updatedAt = now()))

    suspend fun setGoalStatus(goalId: Long, status: GoalStatus) {
        val goal = dao.getGoal(goalId) ?: return
        val completedAt = if (status == GoalStatus.COMPLETED) now() else null
        dao.updateGoal(goal.copy(status = status, completedAt = completedAt, updatedAt = now()))
    }

    suspend fun archiveGoal(goalId: Long) = setGoalStatus(goalId, GoalStatus.ARCHIVED)

    suspend fun deleteGoal(goal: GoalEntity) = dao.deleteGoal(goal)

    suspend fun addMilestone(goalId: Long, title: String, position: Int): Long {
        require(title.isNotBlank()) { "Milestone title must not be blank" }
        return dao.insertMilestone(
            MilestoneEntity(goalId = goalId, title = title.trim(), position = position, createdAt = now()),
        )
    }

    suspend fun setMilestoneCompleted(milestoneId: Long, completed: Boolean): Boolean =
        dao.setMilestoneCompletedAt(milestoneId, if (completed) now() else null) > 0

    suspend fun toggleMilestone(goalId: Long, milestoneId: Long, completed: Boolean) = db.withTransaction {
        setMilestoneCompleted(milestoneId, completed)
        val milestones = dao.getMilestones(goalId)
        val allCompleted = milestones.isNotEmpty() && milestones.all {
            if (it.id == milestoneId) completed else it.completedAt != null
        }
        val goal = dao.getGoal(goalId) ?: return@withTransaction
        if (allCompleted) {
            if (goal.status != GoalStatus.COMPLETED) {
                dao.updateGoal(goal.copy(status = GoalStatus.COMPLETED, completedAt = now(), updatedAt = now()))
            }
        } else if (goal.status == GoalStatus.COMPLETED && !completed) {
            dao.updateGoal(goal.copy(status = GoalStatus.ACTIVE, completedAt = null, updatedAt = now()))
        }
    }

    suspend fun deleteMilestone(milestone: MilestoneEntity) = dao.deleteMilestone(milestone)

    suspend fun setNumericTarget(
        goalId: Long,
        targetValue: Long,
        currentValue: Long = 0,
        unit: String,
    ) = db.withTransaction {
        val existing = dao.getNumericTarget(goalId)
        if (existing != null) {
            dao.updateNumericTarget(
                existing.copy(targetValue = targetValue, currentValue = currentValue, unit = unit),
            )
        } else {
            dao.insertNumericTarget(
                GoalNumericTarget(
                    goalId = goalId,
                    targetValue = targetValue,
                    currentValue = currentValue,
                    unit = unit,
                ),
            )
        }
    }

    suspend fun updateNumericProgress(goalId: Long, newValue: Long): Boolean = db.withTransaction {
        val target = dao.getNumericTarget(goalId) ?: return@withTransaction false
        val clampedValue = newValue.coerceAtLeast(0)
        dao.updateNumericTargetCurrentValue(goalId, clampedValue)

        val goal = dao.getGoal(goalId) ?: return@withTransaction true
        if (clampedValue >= target.targetValue && target.targetValue > 0) {
            if (goal.status != GoalStatus.COMPLETED) {
                dao.updateGoal(goal.copy(status = GoalStatus.COMPLETED, completedAt = now(), updatedAt = now()))
            }
        } else if (goal.status == GoalStatus.COMPLETED && clampedValue < target.targetValue) {
            dao.updateGoal(goal.copy(status = GoalStatus.ACTIVE, completedAt = null, updatedAt = now()))
        }
        true
    }

    suspend fun setCompletionTarget(
        goalId: Long,
        targetCount: Int,
        currentCount: Int = 0,
    ) = db.withTransaction {
        val existing = dao.getCompletionTarget(goalId)
        if (existing != null) {
            dao.updateCompletionTarget(
                existing.copy(targetCount = targetCount, currentCount = currentCount),
            )
        } else {
            dao.insertCompletionTarget(
                GoalCompletionTarget(
                    goalId = goalId,
                    targetCount = targetCount,
                    currentCount = currentCount,
                ),
            )
        }
    }

    suspend fun updateCompletionCount(goalId: Long, newCount: Int): Boolean = db.withTransaction {
        val target = dao.getCompletionTarget(goalId) ?: return@withTransaction false
        val clampedCount = newCount.coerceAtLeast(0)
        dao.updateCompletionTargetCurrentCount(goalId, clampedCount)

        val goal = dao.getGoal(goalId) ?: return@withTransaction true
        if (clampedCount >= target.targetCount && target.targetCount > 0) {
            if (goal.status != GoalStatus.COMPLETED) {
                dao.updateGoal(goal.copy(status = GoalStatus.COMPLETED, completedAt = now(), updatedAt = now()))
            }
        } else if (goal.status == GoalStatus.COMPLETED && clampedCount < target.targetCount) {
            dao.updateGoal(goal.copy(status = GoalStatus.ACTIVE, completedAt = null, updatedAt = now()))
        }
        true
    }

    suspend fun incrementCompletionCount(goalId: Long): Boolean = db.withTransaction {
        val target = dao.getCompletionTarget(goalId) ?: return@withTransaction false
        updateCompletionCount(goalId, target.currentCount + 1)
    }

    suspend fun saveGoalWithConfigs(
        goalId: Long?,
        title: String,
        description: String,
        type: GoalType,
        categoryId: String?,
        startDate: LocalDate?,
        targetDate: LocalDate?,
        milestoneTitles: List<String>,
        numericTargetValue: Long,
        numericCurrentValue: Long,
        numericUnit: String,
        completionTargetCount: Int,
        completionCurrentCount: Int,
    ): Long = db.withTransaction {
        require(title.isNotBlank()) { "Goal title must not be blank" }
        val now = now()
        val id = if (goalId == null || goalId == 0L) {
            dao.insertGoal(
                GoalEntity(
                    title = title.trim(),
                    description = description.trim(),
                    type = type,
                    categoryId = categoryId,
                    startDate = startDate,
                    targetDate = targetDate,
                    status = GoalStatus.ACTIVE,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        } else {
            val existing = dao.getGoal(goalId)
            requireNotNull(existing) { "Goal with id $goalId not found" }
            dao.updateGoal(
                existing.copy(
                    title = title.trim(),
                    description = description.trim(),
                    type = type,
                    categoryId = categoryId,
                    startDate = startDate,
                    targetDate = targetDate,
                    updatedAt = now,
                ),
            )
            goalId
        }

        when (type) {
            GoalType.MILESTONE_BASED -> {
                val existingMilestones = dao.getMilestones(id)
                val existingByTitle = existingMilestones.associateBy { it.title.trim().lowercase() }
                val newTitles = milestoneTitles.map { it.trim() }.filter { it.isNotBlank() }

                // Delete milestones no longer in list
                val newTitlesSet = newTitles.map { it.lowercase() }.toSet()
                existingMilestones.filter { it.title.trim().lowercase() !in newTitlesSet }.forEach {
                    dao.deleteMilestone(it)
                }

                // Add or retain existing
                newTitles.forEachIndexed { index, titleStr ->
                    val existing = existingByTitle[titleStr.lowercase()]
                    if (existing == null) {
                        dao.insertMilestone(
                            MilestoneEntity(
                                goalId = id,
                                title = titleStr,
                                position = index,
                                createdAt = now,
                            ),
                        )
                    }
                }
            }
            GoalType.NUMERIC_TARGET -> {
                setNumericTarget(
                    goalId = id,
                    targetValue = numericTargetValue,
                    currentValue = numericCurrentValue,
                    unit = numericUnit,
                )
            }
            GoalType.COMPLETION_BASED -> {
                setCompletionTarget(
                    goalId = id,
                    targetCount = completionTargetCount,
                    currentCount = completionCurrentCount,
                )
            }
        }

        id
    }
}

