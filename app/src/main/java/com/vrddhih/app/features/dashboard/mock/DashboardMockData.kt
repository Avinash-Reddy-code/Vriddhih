package com.vrddhih.app.features.dashboard.mock

import com.vrddhih.app.features.dashboard.DashboardUiState
import com.vrddhih.app.features.dashboard.DeadlinePreview
import com.vrddhih.app.features.dashboard.GoalPreview
import com.vrddhih.app.features.dashboard.HabitPreview
import com.vrddhih.app.features.dashboard.PlayerStatus
import com.vrddhih.app.features.dashboard.QuestPreview
import com.vrddhih.app.features.dashboard.QuestPriority

/**
 * MOCK DATA BOUNDARY (Phase 0).
 *
 * Hard-coded sample values used to design the dashboard. None of this is user data and none of
 * it is persisted. Phase 1 replaces this object with state derived from the local Room database;
 * nothing outside the dashboard feature should depend on it.
 */
object DashboardMockData {

    fun initialState(): DashboardUiState = DashboardUiState(
        player = PlayerStatus(
            title = "Shadow Initiate",
            rank = "E-Rank Hunter",
            level = 7,
            currentXp = 1_240,
            xpForNextLevel = 2_000,
        ),
        quests = listOf(
            QuestPreview("sample-quest-1", "Deep work: 90 minutes on the main project", QuestPriority.HIGH, xpReward = 120, isCompleted = false),
            QuestPreview("sample-quest-2", "30-minute strength training", QuestPriority.HIGH, xpReward = 80, isCompleted = true),
            QuestPreview("sample-quest-3", "Read 20 pages", QuestPriority.MEDIUM, xpReward = 40, isCompleted = false),
            QuestPreview("sample-quest-4", "Plan tomorrow's priorities", QuestPriority.LOW, xpReward = 20, isCompleted = true),
        ),
        habits = listOf(
            HabitPreview("sample-habit-1", "Drink 2 L of water", isDoneToday = true),
            HabitPreview("sample-habit-2", "Meditate for 10 minutes", isDoneToday = true),
            HabitPreview("sample-habit-3", "Write a journal entry", isDoneToday = false),
            HabitPreview("sample-habit-4", "In bed by 23:00", isDoneToday = false),
            HabitPreview("sample-habit-5", "No screens after 22:30", isDoneToday = true),
        ),
        goals = listOf(
            GoalPreview("sample-goal-1", "Run a half marathon", "Fitness", progress = 0.42f),
            GoalPreview("sample-goal-2", "Finish the Kotlin course", "Skills", progress = 0.68f),
            GoalPreview("sample-goal-3", "Build a 3-month emergency fund", "Finance", progress = 0.25f),
        ),
        deadlines = listOf(
            DeadlinePreview("sample-deadline-1", "Submit project report", daysRemaining = 1),
            DeadlinePreview("sample-deadline-2", "Renew passport documents", daysRemaining = 3),
            DeadlinePreview("sample-deadline-3", "Pay credit card bill", daysRemaining = 9),
        ),
    )
}
