package com.vrddhih.app.features.goals

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.vrddhih.app.core.data.local.VrddhihDatabase
import com.vrddhih.app.core.data.local.entity.GoalStatus
import com.vrddhih.app.core.data.local.entity.GoalType
import com.vrddhih.app.core.data.repository.CategoryRepository
import com.vrddhih.app.core.data.repository.GoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GoalsViewModelTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-27T08:00:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 10, 27)

    private lateinit var db: VrddhihDatabase
    private lateinit var goalRepository: GoalRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var viewModel: GoalsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VrddhihDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(VrddhihDatabase.SEED_CALLBACK)
            .build()
        goalRepository = GoalRepository(db, clock)
        categoryRepository = CategoryRepository(db)
        viewModel = GoalsViewModel(
            goalRepository = goalRepository,
            categoryRepository = categoryRepository,
            clock = clock,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        db.close()
    }

    private fun TestScope.collectUiState() {
        backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect { }
        }
    }

    @Test
    fun initialState_hasCorrectDefaults() = runTest(testDispatcher) {
        collectUiState()
        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals(GoalFilter.ACTIVE, state.filter)
        assertFalse(state.isEditorOpen)
        assertNull(state.editingGoal)
        assertNull(state.error)
        assertTrue(state.goals.isEmpty())
    }

    @Test
    fun saveGoal_milestoneBased_createsGoalAndMilestones() = runTest(testDispatcher) {
        collectUiState()
        val milestones = listOf("Learn Compose basics", "Build state architecture", "Add animations")
        viewModel.saveGoal(
            goalId = null,
            title = "Master Modern Android",
            description = "Complete Compose architecture",
            type = GoalType.MILESTONE_BASED,
            categoryId = null,
            startDate = today,
            targetDate = today.plusMonths(3),
            milestoneTitles = milestones,
            numericTargetValue = 0,
            numericCurrentValue = 0,
            numericUnit = "",
            completionTargetCount = 1,
            completionCurrentCount = 0,
        )

        val state = viewModel.uiState.first { it.filteredGoals.isNotEmpty() }
        assertEquals(1, state.filteredGoals.size)
        val goalWithConfigs = state.filteredGoals.first()
        assertEquals("Master Modern Android", goalWithConfigs.goal.title)
        assertEquals(GoalType.MILESTONE_BASED, goalWithConfigs.goal.type)
        assertEquals(3, goalWithConfigs.milestones.size)
        assertFalse(state.isEditorOpen)
    }

    @Test
    fun toggleMilestone_updatesProgressAndCompletesWhenAllDone() = runTest(testDispatcher) {
        collectUiState()
        val milestones = listOf("Alpha Milestone", "Beta Milestone")
        viewModel.saveGoal(
            goalId = null,
            title = "Release App",
            description = "",
            type = GoalType.MILESTONE_BASED,
            categoryId = null,
            startDate = today,
            targetDate = today.plusWeeks(2),
            milestoneTitles = milestones,
            numericTargetValue = 0,
            numericCurrentValue = 0,
            numericUnit = "",
            completionTargetCount = 1,
            completionCurrentCount = 0,
        )

        val createdState = viewModel.uiState.first { it.filteredGoals.isNotEmpty() }
        val goalId = createdState.filteredGoals.first().goal.id
        val milestoneItems = createdState.filteredGoals.first().milestones.sortedBy { it.position }

        // Toggle first milestone
        viewModel.toggleMilestone(goalId, milestoneItems[0].id, true)
        val state1 = viewModel.uiState.first {
            it.filteredGoals.firstOrNull { g -> g.goal.id == goalId }?.milestones?.find { m -> m.id == milestoneItems[0].id }?.completedAt != null
        }
        val goal1 = state1.filteredGoals.first { it.goal.id == goalId }
        assertEquals(GoalStatus.ACTIVE, goal1.goal.status)

        // Toggle second milestone -> All milestones completed! Goal becomes COMPLETED
        viewModel.toggleMilestone(goalId, milestoneItems[1].id, true)
        val state2 = viewModel.uiState.first {
            it.goals.find { g -> g.goal.id == goalId }?.goal?.status == GoalStatus.COMPLETED
        }
        val goal2 = state2.goals.first { it.goal.id == goalId }
        assertEquals(GoalStatus.COMPLETED, goal2.goal.status)

        // Revert second milestone -> Goal becomes ACTIVE again
        viewModel.toggleMilestone(goalId, milestoneItems[1].id, false)
        val state3 = viewModel.uiState.first {
            it.goals.find { g -> g.goal.id == goalId }?.goal?.status == GoalStatus.ACTIVE
        }
        val goal3 = state3.goals.first { it.goal.id == goalId }
        assertEquals(GoalStatus.ACTIVE, goal3.goal.status)
    }

    @Test
    fun numericGoal_stepperUpdates_completesWhenTargetReached() = runTest(testDispatcher) {
        collectUiState()
        viewModel.saveGoal(
            goalId = null,
            title = "Cycling 500km",
            description = "Monthly challenge",
            type = GoalType.NUMERIC_TARGET,
            categoryId = null,
            startDate = today,
            targetDate = today.plusMonths(1),
            milestoneTitles = emptyList(),
            numericTargetValue = 500,
            numericCurrentValue = 0,
            numericUnit = "km",
            completionTargetCount = 1,
            completionCurrentCount = 0,
        )

        val createdState = viewModel.uiState.first { it.filteredGoals.isNotEmpty() }
        val goalId = createdState.filteredGoals.first().goal.id

        // Partial progress
        viewModel.updateNumericProgress(goalId, 250)
        val state1 = viewModel.uiState.first {
            it.filteredGoals.firstOrNull { g -> g.goal.id == goalId }?.numericTarget?.currentValue == 250L
        }
        val goal1 = state1.filteredGoals.first { it.goal.id == goalId }
        assertEquals(GoalStatus.ACTIVE, goal1.goal.status)

        // Hit target -> Completes goal
        viewModel.updateNumericProgress(goalId, 500)
        val state2 = viewModel.uiState.first {
            it.goals.find { g -> g.goal.id == goalId }?.goal?.status == GoalStatus.COMPLETED
        }
        val goal2 = state2.goals.first { it.goal.id == goalId }
        assertEquals(GoalStatus.COMPLETED, goal2.goal.status)
    }

    @Test
    fun completionGoal_countUpdates_completesWhenTargetReached() = runTest(testDispatcher) {
        collectUiState()
        viewModel.saveGoal(
            goalId = null,
            title = "Read 5 Books",
            description = "",
            type = GoalType.COMPLETION_BASED,
            categoryId = null,
            startDate = today,
            targetDate = today.plusMonths(2),
            milestoneTitles = emptyList(),
            numericTargetValue = 0,
            numericCurrentValue = 0,
            numericUnit = "",
            completionTargetCount = 5,
            completionCurrentCount = 0,
        )

        val createdState = viewModel.uiState.first { it.filteredGoals.isNotEmpty() }
        val goalId = createdState.filteredGoals.first().goal.id

        viewModel.updateCompletionCount(goalId, 5)
        val state = viewModel.uiState.first {
            it.goals.find { g -> g.goal.id == goalId }?.goal?.status == GoalStatus.COMPLETED
        }
        val goal = state.goals.first { it.goal.id == goalId }
        assertEquals(GoalStatus.COMPLETED, goal.goal.status)
        assertEquals(5, goal.completionTarget?.currentCount)
    }

    @Test
    fun filter_togglesBetweenActiveAndCompleted() = runTest(testDispatcher) {
        collectUiState()
        viewModel.saveGoal(
            goalId = null,
            title = "Active Goal",
            description = "",
            type = GoalType.COMPLETION_BASED,
            categoryId = null,
            startDate = today,
            targetDate = null,
            milestoneTitles = emptyList(),
            numericTargetValue = 0,
            numericCurrentValue = 0,
            numericUnit = "",
            completionTargetCount = 1,
            completionCurrentCount = 0,
        )

        viewModel.saveGoal(
            goalId = null,
            title = "Completed Goal",
            description = "",
            type = GoalType.COMPLETION_BASED,
            categoryId = null,
            startDate = today,
            targetDate = null,
            milestoneTitles = emptyList(),
            numericTargetValue = 0,
            numericCurrentValue = 0,
            numericUnit = "",
            completionTargetCount = 1,
            completionCurrentCount = 1,
        )

        // Complete the second goal
        val allState = viewModel.uiState.first { it.goals.size == 2 }
        val completedGoalId = allState.goals.first { it.goal.title == "Completed Goal" }.goal.id
        viewModel.setGoalStatus(completedGoalId, GoalStatus.COMPLETED)

        // Filter ACTIVE
        viewModel.setFilter(GoalFilter.ACTIVE)
        val activeState = viewModel.uiState.first { it.filter == GoalFilter.ACTIVE && it.filteredGoals.size == 1 }
        assertEquals("Active Goal", activeState.filteredGoals.first().goal.title)

        // Filter COMPLETED
        viewModel.setFilter(GoalFilter.COMPLETED)
        val completedState = viewModel.uiState.first { it.filter == GoalFilter.COMPLETED && it.filteredGoals.size == 1 }
        assertEquals("Completed Goal", completedState.filteredGoals.first().goal.title)

        // Filter ALL
        viewModel.setFilter(GoalFilter.ALL)
        val allFilteredState = viewModel.uiState.first { it.filter == GoalFilter.ALL && it.filteredGoals.size == 2 }
        assertEquals(2, allFilteredState.filteredGoals.size)
    }

    @Test
    fun deleteGoal_removesGoalFromList() = runTest(testDispatcher) {
        collectUiState()
        viewModel.saveGoal(
            goalId = null,
            title = "Goal to delete",
            description = "",
            type = GoalType.COMPLETION_BASED,
            categoryId = null,
            startDate = today,
            targetDate = null,
            milestoneTitles = emptyList(),
            numericTargetValue = 0,
            numericCurrentValue = 0,
            numericUnit = "",
            completionTargetCount = 1,
            completionCurrentCount = 0,
        )

        val createdState = viewModel.uiState.first { it.goals.size == 1 }
        val goalId = createdState.goals.first().goal.id

        viewModel.deleteGoal(goalId)
        val emptyState = viewModel.uiState.first { it.goals.isEmpty() }
        assertTrue(emptyState.goals.isEmpty())
    }
}
