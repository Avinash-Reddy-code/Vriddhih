package com.vrddhih.app.features.dashboard

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.vrddhih.app.core.data.local.VrddhihDatabase
import com.vrddhih.app.core.data.local.entity.GoalType
import com.vrddhih.app.core.data.local.entity.HabitScheduleType
import com.vrddhih.app.core.data.local.entity.HabitType
import com.vrddhih.app.core.data.repository.CategoryRepository
import com.vrddhih.app.core.data.repository.DayActivity
import com.vrddhih.app.core.data.repository.GoalRepository
import com.vrddhih.app.core.data.repository.HabitRepository
import com.vrddhih.app.features.dashboard.mock.DashboardMockData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DashboardViewModelTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-25T08:00:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 10, 25)
    private val emptyDay = DayActivity(questCompletions = 0, habitCheckIns = 0, transactions = 0)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(source: (LocalDate) -> Flow<DayActivity> = { flowOf(emptyDay) }) =
        DashboardViewModel(observeDayActivity = source, currentDate = { today })

    private fun TestScope.collectDayLog(viewModel: DashboardViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.dayLog.collect { } }
    }

    private fun TestScope.collectHabits(viewModel: DashboardViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.habits.collect { } }
    }

    private fun TestScope.collectGoals(viewModel: DashboardViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.goals.collect { } }
    }

    @Test
    fun initialState_comesFromMockDataAndSelectsToday() {
        val viewModel = viewModel()
        assertEquals(DashboardMockData.initialState(), viewModel.uiState.value)
        assertEquals(today, viewModel.dateState.value.selectedDate)
        assertEquals(today, viewModel.dateState.value.today)
        assertFalse(viewModel.hasRealHabits)
        assertFalse(viewModel.hasRealGoals)
    }

    @Test
    fun selectDate_updatesDateStateAndQueriesThatDay() = runTest {
        val requested = mutableListOf<LocalDate>()
        val viewModel = viewModel { date ->
            requested += date
            flowOf(DayActivity(questCompletions = date.dayOfMonth, habitCheckIns = 0, transactions = 0))
        }
        collectDayLog(viewModel)
        val target = LocalDate.of(2026, 11, 2)

        viewModel.selectDate(target)

        assertEquals(target, viewModel.dateState.value.selectedDate)
        assertEquals(today, viewModel.dateState.value.today)
        assertEquals(listOf(today, target), requested)
        assertEquals(
            DayLogUiState.Loaded(target, DayActivity(questCompletions = 2, habitCheckIns = 0, transactions = 0)),
            viewModel.dayLog.value,
        )
    }

    @Test
    fun selectToday_returnsToTodayAfterBrowsing() = runTest {
        val viewModel = viewModel()
        collectDayLog(viewModel)
        viewModel.selectDate(LocalDate.of(2024, 2, 29))

        viewModel.selectToday()

        assertEquals(today, viewModel.dateState.value.selectedDate)
        assertEquals(DayLogUiState.Loaded(today, emptyDay), viewModel.dayLog.value)
    }

    @Test
    fun selectingADate_doesNotChangeSampleContent() {
        val viewModel = viewModel()
        val before = viewModel.uiState.value

        viewModel.selectDate(LocalDate.of(2026, 10, 20))

        assertEquals(before, viewModel.uiState.value)
    }

    @Test
    fun emptyDatabase_reportsAnEmptyDayRatherThanFabricatedActivity() = runTest {
        val viewModel = viewModel()
        collectDayLog(viewModel)

        val state = viewModel.dayLog.value as DayLogUiState.Loaded
        assertEquals(true, state.activity.isEmpty)
    }

    @Test
    fun failingSource_reportsUnavailableWithoutBreakingDateSelection() = runTest {
        val viewModel = viewModel { flow { throw IOException("disk error") } }
        collectDayLog(viewModel)

        assertEquals(DayLogUiState.Unavailable, viewModel.dayLog.value)
        viewModel.selectDate(LocalDate.of(2026, 10, 26))
        assertEquals(LocalDate.of(2026, 10, 26), viewModel.dateState.value.selectedDate)
    }

    @Test
    fun toggleQuestPreview_flipsOnlyTheTargetQuest() {
        val viewModel = viewModel()
        val before = viewModel.uiState.value
        val target = before.quests.first()

        viewModel.toggleQuestPreview(target.id)

        val after = viewModel.uiState.value
        assertEquals(!target.isCompleted, after.quests.first().isCompleted)
        assertEquals(before.quests.drop(1), after.quests.drop(1))
        assertNotEquals(before.completedQuestCount, after.completedQuestCount)
    }

    @Test
    fun toggleQuestPreview_twiceRestoresOriginalState() {
        val viewModel = viewModel()
        val before = viewModel.uiState.value
        val id = before.quests.last().id

        viewModel.toggleQuestPreview(id)
        viewModel.toggleQuestPreview(id)

        assertEquals(before, viewModel.uiState.value)
    }

    @Test
    fun toggleQuestPreview_unknownIdIsIgnored() {
        val viewModel = viewModel()
        val before = viewModel.uiState.value

        viewModel.toggleQuestPreview("does-not-exist")

        assertEquals(before, viewModel.uiState.value)
    }

    @Test
    fun realHabits_observedAndToggledOnDashboard() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, VrddhihDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(VrddhihDatabase.SEED_CALLBACK)
            .build()
        val habitRepo = HabitRepository(db, clock)

        val viewModel = DashboardViewModel(
            observeDayActivity = { flowOf(emptyDay) },
            currentDate = { today },
            habitRepository = habitRepo,
        )
        collectHabits(viewModel)

        assertTrue(viewModel.hasRealHabits)

        // Create a real active habit scheduled daily
        val habitId = habitRepo.createHabit(
            name = "Morning Stretch",
            type = HabitType.YES_NO,
            scheduleType = HabitScheduleType.DAILY,
            startDate = today,
        )

        // Verify habit appears in viewModel.habits as pending
        val habits1 = viewModel.habits.first { it.isNotEmpty() }
        assertEquals(1, habits1.size)
        assertEquals("Morning Stretch", habits1.first().name)
        assertFalse(habits1.first().isDone)

        // Toggle habit completed
        viewModel.toggleHabit(habitId)
        val habits2 = viewModel.habits.first { it.firstOrNull()?.isDone == true }
        assertTrue(habits2.first().isDone)

        // Toggle habit back to uncompleted
        viewModel.toggleHabit(habitId)
        val habits3 = viewModel.habits.first { it.firstOrNull()?.isDone == false }
        assertFalse(habits3.first().isDone)

        db.close()
    }

    @Test
    fun realGoals_observedWithProgressOnDashboard() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, VrddhihDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(VrddhihDatabase.SEED_CALLBACK)
            .build()
        val goalRepo = GoalRepository(db, clock)
        val categoryRepo = CategoryRepository(db)

        val viewModel = DashboardViewModel(
            observeDayActivity = { flowOf(emptyDay) },
            currentDate = { today },
            goalRepository = goalRepo,
            categoryRepository = categoryRepo,
        )
        collectGoals(viewModel)

        assertTrue(viewModel.hasRealGoals)

        // Create a milestone-based goal with 2 milestones
        val goalId = goalRepo.saveGoalWithConfigs(
            goalId = null,
            title = "Launch v1",
            description = "MVP launch",
            type = GoalType.MILESTONE_BASED,
            categoryId = null,
            startDate = today,
            targetDate = today.plusMonths(1),
            milestoneTitles = listOf("Design System", "Core Engine"),
            numericTargetValue = 0,
            numericCurrentValue = 0,
            numericUnit = "",
            completionTargetCount = 1,
            completionCurrentCount = 0,
        )

        val goals1 = viewModel.goals.first { it.isNotEmpty() }
        assertEquals(1, goals1.size)
        assertEquals("Launch v1", goals1.first().title)
        assertEquals(0, goals1.first().percent)

        // Check off 1 milestone
        val goalWithConfigs = goalRepo.getGoalWithAllConfigs(goalId)!!
        val milestoneId = goalWithConfigs.milestones.first().id
        goalRepo.toggleMilestone(goalId, milestoneId, true)

        val goals2 = viewModel.goals.first { it.firstOrNull()?.percent == 50 }
        assertEquals(50, goals2.first().percent)
        assertEquals(0.5f, goals2.first().progress, 0.001f)

        db.close()
    }
}
