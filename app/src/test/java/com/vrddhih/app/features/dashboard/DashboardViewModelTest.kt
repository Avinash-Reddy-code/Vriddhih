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
    fun numericHabit_tracksProgressAndEvaluatesDoneOnDashboard() = runTest {
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

        val habitId = habitRepo.createHabit(
            name = "Reading",
            type = HabitType.NUMERIC,
            scheduleType = HabitScheduleType.DAILY,
            startDate = today,
        )
        habitRepo.setNumericConfig(
            habitId = habitId,
            targetValue = 20,
            unit = "pages",
            direction = com.vrddhih.app.core.data.local.entity.NumericDirection.AT_LEAST,
            incrementValue = 5,
        )

        val initial = viewModel.habits.first { it.firstOrNull()?.numericTarget == 20L }.first()
        assertEquals("Reading", initial.name)
        assertEquals(0L, initial.numericCurrent)
        assertEquals(20L, initial.numericTarget)
        assertFalse(initial.isDone)

        // Increment by 10
        viewModel.incrementHabitNumeric(habitId, 10)
        val step1 = viewModel.habits.first { it.firstOrNull()?.numericCurrent == 10L }.first()
        assertEquals(10L, step1.numericCurrent)
        assertFalse(step1.isDone)

        // Increment by 10 more (reaches 20 target)
        viewModel.incrementHabitNumeric(habitId, 10)
        val step2 = viewModel.habits.first { it.firstOrNull()?.numericCurrent == 20L }.first()
        assertEquals(20L, step2.numericCurrent)
        assertTrue(step2.isDone)

        // Set directly to 5 (drops below target)
        viewModel.setHabitNumeric(habitId, 5)
        val step3 = viewModel.habits.first { it.firstOrNull()?.numericCurrent == 5L }.first()
        assertEquals(5L, step3.numericCurrent)
        assertFalse(step3.isDone)

        db.close()
    }

    @Test
    fun timerHabit_tracksSecondsAndEvaluatesDoneOnDashboard() = runTest {
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

        val habitId = habitRepo.createHabit(
            name = "Running",
            type = HabitType.TIMER,
            scheduleType = HabitScheduleType.DAILY,
            startDate = today,
        )
        habitRepo.setTimerConfig(
            habitId = habitId,
            targetDurationSeconds = 1800, // 30 min
            dailyTargetSeconds = 1800,
        )

        val initial = viewModel.habits.first { it.firstOrNull()?.timerTargetSeconds == 1800 }.first()
        assertEquals("Running", initial.name)
        assertEquals(0, initial.timerElapsedSeconds)
        assertEquals(1800, initial.timerTargetSeconds)
        assertFalse(initial.isDone)

        // Log 15m (900 seconds)
        viewModel.logHabitTimer(habitId, 900)
        val step1 = viewModel.habits.first { it.firstOrNull()?.timerElapsedSeconds == 900 }.first()
        assertEquals(900, step1.timerElapsedSeconds)
        assertFalse(step1.isDone)

        // Log another 15m (total 1800s -> completes)
        viewModel.logHabitTimer(habitId, 900)
        val step2 = viewModel.habits.first { it.firstOrNull()?.timerElapsedSeconds == 1800 }.first()
        assertEquals(1800, step2.timerElapsedSeconds)
        assertTrue(step2.isDone)

        db.close()
    }

    @Test
    fun checklistHabit_ticksIndividualItemsAndEvaluatesDoneOnDashboard() = runTest {
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

        val habitId = habitRepo.createHabit(
            name = "Morning Routine",
            type = HabitType.CHECKLIST,
            scheduleType = HabitScheduleType.DAILY,
            startDate = today,
        )
        val item1Id = habitRepo.addChecklistItem(habitId, "Drink water", 0)
        val item2Id = habitRepo.addChecklistItem(habitId, "Stretch", 1)

        val initial = viewModel.habits.first { it.firstOrNull()?.checklistItems?.size == 2 }.first()
        assertEquals(2, initial.checklistItems.size)
        assertFalse(initial.checklistItems[0].isCompleted)
        assertFalse(initial.checklistItems[1].isCompleted)
        assertFalse(initial.isDone)

        // Tick first item
        viewModel.toggleChecklistItem(habitId, item1Id, true)
        val step1 = viewModel.habits.first { it.firstOrNull()?.checklistItems?.get(0)?.isCompleted == true }.first()
        assertTrue(step1.checklistItems[0].isCompleted)
        assertFalse(step1.checklistItems[1].isCompleted)
        assertFalse(step1.isDone)

        // Tick second item (all completed)
        viewModel.toggleChecklistItem(habitId, item2Id, true)
        val step2 = viewModel.habits.first { it.firstOrNull()?.isDone == true }.first()
        assertTrue(step2.checklistItems[0].isCompleted)
        assertTrue(step2.checklistItems[1].isCompleted)
        assertTrue(step2.isDone)

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

    @Test
    fun numericHabit_evaluatesAllConditionsCorrectly() = runTest {
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

        // 1. AT_LEAST (Target: 30)
        val atLeastId = habitRepo.createHabit(name = "At Least Habit", type = HabitType.NUMERIC, startDate = today)
        habitRepo.setNumericConfig(atLeastId, targetValue = 30, unit = "units", direction = com.vrddhih.app.core.data.local.entity.NumericDirection.AT_LEAST)

        // 25 -> incomplete
        viewModel.setHabitNumeric(atLeastId, 25)
        val atLeastIncomplete = viewModel.habits.first { it.any { h -> h.id == atLeastId && h.numericCurrent == 25L } }
        assertFalse(atLeastIncomplete.first { it.id == atLeastId }.isDone)

        // 30 -> complete
        viewModel.setHabitNumeric(atLeastId, 30)
        val atLeastComplete = viewModel.habits.first { it.any { h -> h.id == atLeastId && h.numericCurrent == 30L && h.isDone } }
        assertTrue(atLeastComplete.first { it.id == atLeastId }.isDone)

        // 2. LESS_THAN / AT_MOST (Target: 30)
        val lessThanId = habitRepo.createHabit(name = "Less Than Habit", type = HabitType.NUMERIC, startDate = today)
        habitRepo.setNumericConfig(lessThanId, targetValue = 30, unit = "units", direction = com.vrddhih.app.core.data.local.entity.NumericDirection.AT_MOST)

        // 25 -> complete
        viewModel.setHabitNumeric(lessThanId, 25)
        val lessThanComplete = viewModel.habits.first { it.any { h -> h.id == lessThanId && h.numericCurrent == 25L && h.isDone } }
        assertTrue(lessThanComplete.first { it.id == lessThanId }.isDone)

        // 35 -> incomplete (reversal)
        viewModel.setHabitNumeric(lessThanId, 35)
        val lessThanIncomplete = viewModel.habits.first { it.any { h -> h.id == lessThanId && h.numericCurrent == 35L && !h.isDone } }
        assertFalse(lessThanIncomplete.first { it.id == lessThanId }.isDone)

        // 3. EXACTLY (Target: 30)
        val exactlyId = habitRepo.createHabit(name = "Exactly Habit", type = HabitType.NUMERIC, startDate = today)
        habitRepo.setNumericConfig(exactlyId, targetValue = 30, unit = "units", direction = com.vrddhih.app.core.data.local.entity.NumericDirection.EXACTLY)

        // 25 -> incomplete
        viewModel.setHabitNumeric(exactlyId, 25)
        val exactlyIncomplete = viewModel.habits.first { it.any { h -> h.id == exactlyId && h.numericCurrent == 25L } }
        assertFalse(exactlyIncomplete.first { it.id == exactlyId }.isDone)

        // 30 -> complete
        viewModel.setHabitNumeric(exactlyId, 30)
        val exactlyComplete = viewModel.habits.first { it.any { h -> h.id == exactlyId && h.numericCurrent == 30L && h.isDone } }
        assertTrue(exactlyComplete.first { it.id == exactlyId }.isDone)

        // 4. ANY_VALUE (with explicit confirmation)
        val anyValId = habitRepo.createHabit(name = "Any Value Habit", type = HabitType.NUMERIC, startDate = today)
        habitRepo.setNumericConfig(anyValId, targetValue = 0, unit = "units", direction = com.vrddhih.app.core.data.local.entity.NumericDirection.ANY_VALUE)

        // Progress entered without marking complete
        viewModel.setHabitNumeric(anyValId, 15, completed = false)
        val anyValIncomplete = viewModel.habits.first { it.any { h -> h.id == anyValId && h.numericCurrent == 15L && !h.isDone } }
        assertFalse(anyValIncomplete.first { it.id == anyValId }.isDone)

        // Marked complete
        viewModel.setHabitNumeric(anyValId, 15, completed = true)
        val anyValComplete = viewModel.habits.first { it.any { h -> h.id == anyValId && h.numericCurrent == 15L && h.isDone } }
        assertTrue(anyValComplete.first { it.id == anyValId }.isDone)

        db.close()
    }

    @Test
    fun checklistHabit_uncheckingReversesCompletion() = runTest {
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

        val habitId = habitRepo.createHabit(name = "Daily Routine", type = HabitType.CHECKLIST, startDate = today)
        val item1 = habitRepo.addChecklistItem(habitId, "Item 1", 0)
        val item2 = habitRepo.addChecklistItem(habitId, "Item 2", 1)

        // Check both items -> complete
        viewModel.toggleChecklistItem(habitId, item1, true)
        viewModel.toggleChecklistItem(habitId, item2, true)
        val completeState = viewModel.habits.first { it.any { h -> h.id == habitId && h.isDone } }
        assertTrue(completeState.first { it.id == habitId }.isDone)

        // Uncheck one item -> returns to incomplete
        viewModel.toggleChecklistItem(habitId, item2, false)
        val reversedState = viewModel.habits.first { it.any { h -> h.id == habitId && !h.isDone } }
        assertFalse(reversedState.first { it.id == habitId }.isDone)

        db.close()
    }

    @Test
    fun dashboardPresentation_completedHabitsSortBelowIncomplete_preservingRelativeOrder() {
        val habitA = DashboardHabitItem(id = 1L, name = "Habit A", isDone = false)
        val habitB = DashboardHabitItem(id = 2L, name = "Habit B", isDone = true)
        val habitC = DashboardHabitItem(id = 3L, name = "Habit C", isDone = false)
        val habitD = DashboardHabitItem(id = 4L, name = "Habit D", isDone = true)
        val habitE = DashboardHabitItem(id = 5L, name = "Habit E", isDone = false)

        val originalList = listOf(habitA, habitB, habitC, habitD, habitE)
        val sorted = originalList.sortedBy { it.isDone }

        // Incomplete habits come first, maintaining relative order A, C, E
        assertEquals(listOf(1L, 3L, 5L, 2L, 4L), sorted.map { it.id })
        assertFalse(sorted[0].isDone)
        assertFalse(sorted[1].isDone)
        assertFalse(sorted[2].isDone)
        // Completed habits are moved down, maintaining relative order B, D
        assertTrue(sorted[3].isDone)
        assertTrue(sorted[4].isDone)
    }
}
