package com.vrddhih.app.features.habits

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.vrddhih.app.core.data.local.VrddhihDatabase
import com.vrddhih.app.core.data.local.entity.HabitScheduleType
import com.vrddhih.app.core.data.local.entity.HabitType
import com.vrddhih.app.core.data.local.entity.NumericDirection
import com.vrddhih.app.core.data.repository.CategoryRepository
import com.vrddhih.app.core.data.repository.HabitRepository
import com.vrddhih.app.core.time.DateSelectionStateHolder
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
class HabitsViewModelTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-27T08:00:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 10, 27)

    private lateinit var db: VrddhihDatabase
    private lateinit var habitRepository: HabitRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var viewModel: HabitsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VrddhihDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(VrddhihDatabase.SEED_CALLBACK)
            .build()
        habitRepository = HabitRepository(db, clock)
        categoryRepository = CategoryRepository(db)
        viewModel = HabitsViewModel(
            habitRepository = habitRepository,
            categoryRepository = categoryRepository,
            clock = clock,
            dateSelection = DateSelectionStateHolder({ today }),
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
        assertEquals(today, viewModel.dateState.value.selectedDate)
        assertFalse(state.isEditorOpen)
        assertNull(state.editingHabit)
        assertNull(state.error)
        assertTrue(state.habitItems.isEmpty())
    }

    @Test
    fun saveHabit_yesNo_createsHabitAndShowsInList() = runTest(testDispatcher) {
        collectUiState()
        viewModel.saveHabit(
            habitId = null,
            name = "Morning Meditation",
            type = HabitType.YES_NO,
            description = "10 mins mindfulness",
            scheduleType = HabitScheduleType.DAILY,
            scheduleInterval = 1,
            scheduleDaysMask = 0x7F,
            startDate = today,
            categoryId = null,
            xpPerCompletion = 15,
            numericTargetValue = 1,
            numericUnit = "",
            numericDirection = NumericDirection.AT_LEAST,
            numericIncrement = 1,
            timerTargetMinutes = 10,
            limitDailyTarget = 1,
            limitUnit = "",
            checklistItemTitles = emptyList(),
        )

        val state = viewModel.uiState.first { it.habitItems.size == 1 }
        val habit = state.habitItems.first()
        assertEquals("Morning Meditation", habit.habit.name)
        assertEquals(HabitType.YES_NO, habit.habit.type)
        assertFalse(state.isEditorOpen)
    }

    @Test
    fun completeHabit_and_undoCompletion_updatesProgressMap() = runTest(testDispatcher) {
        collectUiState()
        viewModel.saveHabit(
            habitId = null,
            name = "Cold Shower",
            type = HabitType.YES_NO,
            description = "",
            scheduleType = HabitScheduleType.DAILY,
            scheduleInterval = 1,
            scheduleDaysMask = 0x7F,
            startDate = today,
            categoryId = null,
            xpPerCompletion = 10,
            numericTargetValue = 1,
            numericUnit = "",
            numericDirection = NumericDirection.AT_LEAST,
            numericIncrement = 1,
            timerTargetMinutes = 5,
            limitDailyTarget = 1,
            limitUnit = "",
            checklistItemTitles = emptyList(),
        )

        val createdState = viewModel.uiState.first { it.habitItems.size == 1 }
        val habitId = createdState.habitItems.first().habit.id

        viewModel.completeHabit(habitId)
        val completedState = viewModel.uiState.first { it.progressMap[habitId]?.completedAt != null }
        val completedProgress = completedState.progressMap[habitId]
        assertNotNull(completedProgress)
        assertTrue(completedProgress!!.completedAt != null)

        viewModel.undoCompletion(habitId)
        val undoneState = viewModel.uiState.first { it.progressMap[habitId]?.completedAt == null }
        val undoneProgress = undoneState.progressMap[habitId]
        assertNotNull(undoneProgress)
        assertFalse(undoneProgress!!.completedAt != null)
    }

    @Test
    fun numericHabit_updateProgress_completesWhenTargetReached() = runTest(testDispatcher) {
        collectUiState()
        viewModel.saveHabit(
            habitId = null,
            name = "Water Intake",
            type = HabitType.NUMERIC,
            description = "Stay hydrated",
            scheduleType = HabitScheduleType.DAILY,
            scheduleInterval = 1,
            scheduleDaysMask = 0x7F,
            startDate = today,
            categoryId = null,
            xpPerCompletion = 20,
            numericTargetValue = 2500,
            numericUnit = "ml",
            numericDirection = NumericDirection.AT_LEAST,
            numericIncrement = 250,
            timerTargetMinutes = 0,
            limitDailyTarget = 0,
            limitUnit = "",
            checklistItemTitles = emptyList(),
        )

        val createdState = viewModel.uiState.first { it.habitItems.size == 1 }
        val habitId = createdState.habitItems.first().habit.id

        // Partial progress
        viewModel.updateNumericProgress(habitId, 1500)
        val partialState = viewModel.uiState.first { it.progressMap[habitId]?.numericValue == 1500L }
        val partialProgress = partialState.progressMap[habitId]
        assertNotNull(partialProgress)
        assertEquals(1500L, partialProgress!!.numericValue)
        assertFalse(partialProgress.completedAt != null)

        // Reach target
        viewModel.updateNumericProgress(habitId, 2500)
        val reachedState = viewModel.uiState.first { it.progressMap[habitId]?.numericValue == 2500L && it.progressMap[habitId]?.completedAt != null }
        val reachedProgress = reachedState.progressMap[habitId]
        assertNotNull(reachedProgress)
        assertEquals(2500L, reachedProgress!!.numericValue)
        assertTrue(reachedProgress.completedAt != null)
    }

    @Test
    fun timerHabit_logTimerSeconds_accumulatesAndCompletes() = runTest(testDispatcher) {
        collectUiState()
        viewModel.saveHabit(
            habitId = null,
            name = "Deep Study",
            type = HabitType.TIMER,
            description = "Focus block",
            scheduleType = HabitScheduleType.DAILY,
            scheduleInterval = 1,
            scheduleDaysMask = 0x7F,
            startDate = today,
            categoryId = null,
            xpPerCompletion = 25,
            numericTargetValue = 1,
            numericUnit = "",
            numericDirection = NumericDirection.AT_LEAST,
            numericIncrement = 1,
            timerTargetMinutes = 45, // 2700 seconds
            limitDailyTarget = 0,
            limitUnit = "",
            checklistItemTitles = emptyList(),
        )

        val createdState = viewModel.uiState.first { it.habitItems.size == 1 }
        val habitId = createdState.habitItems.first().habit.id

        // Log 15 minutes (900 seconds)
        viewModel.logTimerSeconds(habitId, 900)
        val partialState = viewModel.uiState.first { it.progressMap[habitId]?.timerSeconds == 900 }
        val partialProgress = partialState.progressMap[habitId]
        assertNotNull(partialProgress)
        assertEquals(900, partialProgress!!.timerSeconds)
        assertFalse(partialProgress.completedAt != null)

        // Log 30 more minutes (1800 seconds) -> Total 2700 seconds
        viewModel.logTimerSeconds(habitId, 1800)
        val completedState = viewModel.uiState.first { it.progressMap[habitId]?.timerSeconds == 2700 && it.progressMap[habitId]?.completedAt != null }
        val completedProgress = completedState.progressMap[habitId]
        assertNotNull(completedProgress)
        assertEquals(2700, completedProgress!!.timerSeconds)
        assertTrue(completedProgress.completedAt != null)
    }

    @Test
    fun checklistHabit_toggleItems_autoCompletesWhenAllItemsChecked() = runTest(testDispatcher) {
        collectUiState()
        val items = listOf("Warmup stretches", "Pushups", "Core plank")
        viewModel.saveHabit(
            habitId = null,
            name = "Morning Workout",
            type = HabitType.CHECKLIST,
            description = "Full body activation",
            scheduleType = HabitScheduleType.DAILY,
            scheduleInterval = 1,
            scheduleDaysMask = 0x7F,
            startDate = today,
            categoryId = null,
            xpPerCompletion = 30,
            numericTargetValue = 1,
            numericUnit = "",
            numericDirection = NumericDirection.AT_LEAST,
            numericIncrement = 1,
            timerTargetMinutes = 0,
            limitDailyTarget = 0,
            limitUnit = "",
            checklistItemTitles = items,
        )

        val createdState = viewModel.uiState.first { it.habitItems.size == 1 && it.habitItems.first().checklistItems.size == 3 }
        val habitWithConfigs = createdState.habitItems.first()
        val habitId = habitWithConfigs.habit.id
        val checklistItems = habitWithConfigs.checklistItems
        assertEquals(3, checklistItems.size)

        // Check item 1
        viewModel.toggleChecklistItem(habitId, checklistItems[0].id, true)
        val state1 = viewModel.uiState.first { it.checklistProgressMap[habitId]?.get(checklistItems[0].id) == true }
        var progress = state1.progressMap[habitId]
        assertFalse(progress?.completedAt != null)

        // Check item 2
        viewModel.toggleChecklistItem(habitId, checklistItems[1].id, true)
        val state2 = viewModel.uiState.first { it.checklistProgressMap[habitId]?.get(checklistItems[1].id) == true }
        progress = state2.progressMap[habitId]
        assertFalse(progress?.completedAt != null)

        // Check item 3 -> All completed
        viewModel.toggleChecklistItem(habitId, checklistItems[2].id, true)
        val state3 = viewModel.uiState.first { it.checklistProgressMap[habitId]?.get(checklistItems[2].id) == true && it.progressMap[habitId]?.completedAt != null }
        progress = state3.progressMap[habitId]
        assertNotNull(progress)
        assertTrue(progress!!.completedAt != null)

        // Uncheck item 2 -> Should revert completion
        viewModel.toggleChecklistItem(habitId, checklistItems[1].id, false)
        val state4 = viewModel.uiState.first { it.checklistProgressMap[habitId]?.get(checklistItems[1].id) == false && it.progressMap[habitId]?.completedAt == null }
        progress = state4.progressMap[habitId]
        assertNotNull(progress)
        assertFalse(progress!!.completedAt != null)
    }

    @Test
    fun limitHabit_updateLimitProgress_tracksCount() = runTest(testDispatcher) {
        collectUiState()
        viewModel.saveHabit(
            habitId = null,
            name = "Coffee Consumption",
            type = HabitType.LIMIT,
            description = "Max 2 cups per day",
            scheduleType = HabitScheduleType.DAILY,
            scheduleInterval = 1,
            scheduleDaysMask = 0x7F,
            startDate = today,
            categoryId = null,
            xpPerCompletion = 10,
            numericTargetValue = 1,
            numericUnit = "",
            numericDirection = NumericDirection.AT_MOST,
            numericIncrement = 1,
            timerTargetMinutes = 0,
            limitDailyTarget = 2,
            limitUnit = "cups",
            checklistItemTitles = emptyList(),
        )

        val createdState = viewModel.uiState.first { it.habitItems.size == 1 }
        val habitId = createdState.habitItems.first().habit.id

        // Log 1 cup
        viewModel.updateLimitProgress(habitId, 1)
        val state1 = viewModel.uiState.first { it.progressMap[habitId]?.numericValue == 1L }
        val progress1 = state1.progressMap[habitId]
        assertNotNull(progress1)
        assertEquals(1L, progress1!!.numericValue)

        // Log 3 cups (exceeded limit)
        viewModel.updateLimitProgress(habitId, 3)
        val state2 = viewModel.uiState.first { it.progressMap[habitId]?.numericValue == 3L }
        val progress2 = state2.progressMap[habitId]
        assertNotNull(progress2)
        assertEquals(3L, progress2!!.numericValue)
    }

    @Test
    fun deleteHabit_removesHabitFromList() = runTest(testDispatcher) {
        collectUiState()
        viewModel.saveHabit(
            habitId = null,
            name = "Obsolete Habit",
            type = HabitType.YES_NO,
            description = "",
            scheduleType = HabitScheduleType.DAILY,
            scheduleInterval = 1,
            scheduleDaysMask = 0x7F,
            startDate = today,
            categoryId = null,
            xpPerCompletion = 10,
            numericTargetValue = 1,
            numericUnit = "",
            numericDirection = NumericDirection.AT_LEAST,
            numericIncrement = 1,
            timerTargetMinutes = 0,
            limitDailyTarget = 0,
            limitUnit = "",
            checklistItemTitles = emptyList(),
        )

        val createdState = viewModel.uiState.first { it.habitItems.size == 1 }
        val habitId = createdState.habitItems.first().habit.id

        viewModel.deleteHabit(habitId)
        val emptyState = viewModel.uiState.first { it.habitItems.isEmpty() }
        assertTrue(emptyState.habitItems.isEmpty())
    }
}
