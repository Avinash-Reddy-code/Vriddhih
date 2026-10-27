package com.vrddhih.app.features.dashboard

import kotlin.math.roundToInt

/**
 * UI state for the dashboard. In Phase 0 it is populated only from
 * [com.vrddhih.app.features.dashboard.mock.DashboardMockData]; the `*Preview` models are
 * display models, not persistence entities.
 */
data class DashboardUiState(
    val player: PlayerStatus,
    val quests: List<QuestPreview>,
    val habits: List<HabitPreview>,
    val goals: List<GoalPreview>,
    val deadlines: List<DeadlinePreview>,
) {
    val completedQuestCount: Int get() = quests.count { it.isCompleted }
    val remainingQuestCount: Int get() = quests.size - completedQuestCount
    val previewXpEarned: Int get() = quests.filter { it.isCompleted }.sumOf { it.xpReward }
    val completedHabitCount: Int get() = habits.count { it.isDoneToday }
    val habitProgress: Float
        get() = if (habits.isEmpty()) 0f else completedHabitCount.toFloat() / habits.size
}

data class PlayerStatus(
    val title: String,
    val rank: String,
    val level: Int,
    val currentXp: Int,
    val xpForNextLevel: Int,
) {
    val levelProgress: Float
        get() = if (xpForNextLevel <= 0) 0f else (currentXp.toFloat() / xpForNextLevel).coerceIn(0f, 1f)
    val xpToNextLevel: Int get() = (xpForNextLevel - currentXp).coerceAtLeast(0)
}

enum class QuestPriority { HIGH, MEDIUM, LOW }

data class QuestPreview(
    val id: String,
    val title: String,
    val priority: QuestPriority,
    val xpReward: Int,
    val isCompleted: Boolean,
)

data class HabitPreview(
    val id: String,
    val name: String,
    val isDoneToday: Boolean,
)

data class DashboardHabitItem(
    val id: Long,
    val name: String,
    val isDone: Boolean,
)

data class GoalPreview(
    val id: String,
    val title: String,
    val category: String,
    val progress: Float,
) {
    val percent: Int get() = (progress.coerceIn(0f, 1f) * 100).roundToInt()
}

data class DashboardGoalItem(
    val id: Long,
    val title: String,
    val category: String,
    val progress: Float,
) {
    val percent: Int get() = (progress.coerceIn(0f, 1f) * 100).roundToInt()
}

enum class DeadlineUrgency { CRITICAL, SOON, SCHEDULED }

data class DeadlinePreview(
    val id: String,
    val title: String,
    val daysRemaining: Int,
) {
    val urgency: DeadlineUrgency
        get() = when {
            daysRemaining <= 1 -> DeadlineUrgency.CRITICAL
            daysRemaining <= 3 -> DeadlineUrgency.SOON
            else -> DeadlineUrgency.SCHEDULED
        }
}
