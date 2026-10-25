package com.vrddhih.app.features.dashboard

import com.vrddhih.app.features.dashboard.mock.DashboardMockData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardUiStateTest {

    private val quests = listOf(
        QuestPreview("a", "A", QuestPriority.HIGH, xpReward = 100, isCompleted = true),
        QuestPreview("b", "B", QuestPriority.LOW, xpReward = 30, isCompleted = false),
        QuestPreview("c", "C", QuestPriority.MEDIUM, xpReward = 50, isCompleted = true),
    )

    private fun state(
        quests: List<QuestPreview> = this.quests,
        habits: List<HabitPreview> = emptyList(),
    ) = DashboardUiState(
        player = PlayerStatus("Title", "Rank", level = 1, currentXp = 0, xpForNextLevel = 100),
        quests = quests,
        habits = habits,
        goals = emptyList(),
        deadlines = emptyList(),
    )

    @Test
    fun questSummary_isDerivedFromQuests() {
        val state = state()
        assertEquals(2, state.completedQuestCount)
        assertEquals(1, state.remainingQuestCount)
        assertEquals(150, state.previewXpEarned)
    }

    @Test
    fun habitProgress_handlesEmptyAndPartialLists() {
        assertEquals(0f, state(habits = emptyList()).habitProgress, 0f)
        val habits = listOf(
            HabitPreview("1", "One", isDoneToday = true),
            HabitPreview("2", "Two", isDoneToday = false),
            HabitPreview("3", "Three", isDoneToday = true),
            HabitPreview("4", "Four", isDoneToday = false),
        )
        assertEquals(0.5f, state(habits = habits).habitProgress, 0.0001f)
    }

    @Test
    fun playerLevelProgress_isClampedAndSafe() {
        assertEquals(0.62f, PlayerStatus("t", "r", 7, 1_240, 2_000).levelProgress, 0.0001f)
        assertEquals(760, PlayerStatus("t", "r", 7, 1_240, 2_000).xpToNextLevel)
        assertEquals(1f, PlayerStatus("t", "r", 1, 500, 100).levelProgress, 0f)
        assertEquals(0, PlayerStatus("t", "r", 1, 500, 100).xpToNextLevel)
        assertEquals(0f, PlayerStatus("t", "r", 1, 10, 0).levelProgress, 0f)
    }

    @Test
    fun goalPercent_isRoundedAndClamped() {
        assertEquals(42, GoalPreview("g", "G", "C", 0.42f).percent)
        assertEquals(100, GoalPreview("g", "G", "C", 1.4f).percent)
        assertEquals(0, GoalPreview("g", "G", "C", -0.2f).percent)
    }

    @Test
    fun deadlineUrgency_followsDaysRemaining() {
        assertEquals(DeadlineUrgency.CRITICAL, DeadlinePreview("d", "D", 0).urgency)
        assertEquals(DeadlineUrgency.CRITICAL, DeadlinePreview("d", "D", 1).urgency)
        assertEquals(DeadlineUrgency.SOON, DeadlinePreview("d", "D", 3).urgency)
        assertEquals(DeadlineUrgency.SCHEDULED, DeadlinePreview("d", "D", 4).urgency)
    }

    @Test
    fun mockData_idsAreUniqueAndMarkedAsSamples() {
        val mock = DashboardMockData.initialState()
        val ids = mock.quests.map { it.id } + mock.habits.map { it.id } +
            mock.goals.map { it.id } + mock.deadlines.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.all { it.startsWith("sample-") })
    }
}
