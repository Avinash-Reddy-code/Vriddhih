package com.vrddhih.app.features.quests

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.vrddhih.app.core.data.local.VrddhihDatabase
import com.vrddhih.app.core.data.local.entity.QuestRecurrence
import com.vrddhih.app.core.data.local.entity.TaskPriority
import com.vrddhih.app.core.data.quest.QuestDraft
import com.vrddhih.app.core.data.quest.QuestFilter
import com.vrddhih.app.core.data.quest.QuestScope
import com.vrddhih.app.core.data.quest.QuestSort
import com.vrddhih.app.core.data.repository.CategoryRepository
import com.vrddhih.app.core.data.repository.QuestRepository
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
class QuestsViewModelTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-25T08:00:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 10, 25)

    private lateinit var db: VrddhihDatabase
    private lateinit var repository: QuestRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var viewModel: QuestsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VrddhihDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(VrddhihDatabase.SEED_CALLBACK)
            .build()
        repository = QuestRepository(db, clock)
        categoryRepository = CategoryRepository(db)
        viewModel = QuestsViewModel(
            questRepository = repository,
            categoryRepository = categoryRepository,
            currentDate = { today },
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
        val state = viewModel.uiState.value
        assertEquals(QuestFilter.TODAY, state.filter)
        assertEquals(QuestSort.DEADLINE, state.sort)
        assertEquals("", state.searchQuery)
        assertEquals(today, state.today)
        assertTrue(state.occurrences.isEmpty())
        assertFalse(state.isEditorOpen)
    }

    @Test
    fun filteringAndCounts_reflectCurrentQuests() = runTest(testDispatcher) {
        collectUiState()
        // Today quest
        repository.createQuest(QuestDraft(title = "Task Today", scheduledDate = today))
        // Tomorrow quest
        repository.createQuest(QuestDraft(title = "Task Tomorrow", scheduledDate = today.plusDays(1)))
        // Past one-off (overdue)
        repository.createQuest(QuestDraft(title = "Overdue Task", scheduledDate = today.minusDays(2)))

        val state = viewModel.uiState.first { it.counts[QuestFilter.ALL] == 3 }
        assertEquals(1, state.counts[QuestFilter.TODAY])
        assertEquals(1, state.counts[QuestFilter.UPCOMING])
        assertEquals(1, state.counts[QuestFilter.OVERDUE])
        assertEquals(3, state.counts[QuestFilter.ALL])
        assertEquals(0, state.counts[QuestFilter.COMPLETED])

        // Switch to OVERDUE
        viewModel.setFilter(QuestFilter.OVERDUE)
        val overdueState = viewModel.uiState.first { it.filter == QuestFilter.OVERDUE }
        assertEquals(1, overdueState.occurrences.size)
        assertEquals("Overdue Task", overdueState.occurrences.first().title)

        // Switch to ALL
        viewModel.setFilter(QuestFilter.ALL)
        val allState = viewModel.uiState.first { it.filter == QuestFilter.ALL }
        assertEquals(3, allState.occurrences.size)
    }

    @Test
    fun search_filtersByTitleAndCategory() = runTest(testDispatcher) {
        collectUiState()
        val yogaCat = categoryRepository.createCustom("Yoga", "spa", 0xFF80DEEA.toInt())
        val runningCat = categoryRepository.createCustom("Running", "fitness_center", 0xFF69F0AE.toInt())

        repository.createQuest(QuestDraft(title = "Morning Meditation", scheduledDate = today, categoryId = yogaCat?.id))
        repository.createQuest(QuestDraft(title = "Evening Session", scheduledDate = today, categoryId = runningCat?.id))

        viewModel.setFilter(QuestFilter.ALL)
        viewModel.uiState.first { it.counts[QuestFilter.ALL] == 2 }

        viewModel.setSearchQuery("meditation")
        val medState = viewModel.uiState.first { it.searchQuery == "meditation" && it.occurrences.size == 1 }
        assertEquals("Morning Meditation", medState.occurrences.first().title)

        viewModel.setSearchQuery("running")
        val fitState = viewModel.uiState.first { it.searchQuery == "running" && it.occurrences.size == 1 }
        assertEquals("Evening Session", fitState.occurrences.first().title)

        viewModel.setSearchQuery("nomatch")
        val noMatchState = viewModel.uiState.first { it.searchQuery == "nomatch" && it.occurrences.isEmpty() }
        assertTrue(noMatchState.occurrences.isEmpty())
    }

    @Test
    fun toggleCompletion_updatesOccurrenceAndCounts() = runTest(testDispatcher) {
        collectUiState()
        repository.createQuest(QuestDraft(title = "Read", scheduledDate = today))

        val state = viewModel.uiState.first { it.occurrences.isNotEmpty() }
        val occurrence = state.occurrences.first()
        assertFalse(occurrence.isCompleted)

        // Toggle complete
        viewModel.toggleCompletion(occurrence)
        val completedState = viewModel.uiState.first { it.counts[QuestFilter.COMPLETED] == 1 }
        assertTrue(completedState.occurrences.first().isCompleted)
        assertEquals(1, completedState.counts[QuestFilter.COMPLETED])

        // Toggle reopen
        viewModel.toggleCompletion(completedState.occurrences.first())
        val reopenedState = viewModel.uiState.first { it.counts[QuestFilter.COMPLETED] == 0 }
        assertFalse(reopenedState.occurrences.first().isCompleted)
    }

    @Test
    fun createQuestFlow_savesNewQuestToDatabase() = runTest(testDispatcher) {
        collectUiState()
        viewModel.openCreateQuest()
        val openState = viewModel.uiState.first { it.isEditorOpen }
        assertNotNull(openState.editingDraft)

        val draft = QuestDraft(
            title = "New Quest Created",
            priority = TaskPriority.HIGH,
            scheduledDate = today,
        )
        viewModel.updateEditorDraft(draft)
        viewModel.saveEditor()

        val state = viewModel.uiState.first { !it.isEditorOpen && it.occurrences.any { occ -> occ.title == "New Quest Created" } }
        val created = state.occurrences.first { it.title == "New Quest Created" }
        assertEquals(TaskPriority.HIGH, created.priority)
    }

    @Test
    fun editQuestFlow_updatesExistingQuest() = runTest(testDispatcher) {
        collectUiState()
        repository.createQuest(QuestDraft(title = "Old Title", scheduledDate = today))

        val state = viewModel.uiState.first { it.occurrences.isNotEmpty() }
        val occ = state.occurrences.first()

        viewModel.openEditQuest(occ)
        val editorOpenState = viewModel.uiState.first { it.isEditorOpen }
        assertNotNull(editorOpenState.editingDraft)

        val updatedDraft = editorOpenState.editingDraft!!.copy(title = "Updated Title")
        viewModel.updateEditorDraft(updatedDraft)
        viewModel.saveEditor()

        val afterState = viewModel.uiState.first { !it.isEditorOpen && it.occurrences.any { o -> o.title == "Updated Title" } }
        assertEquals("Updated Title", afterState.occurrences.first().title)
    }

    @Test
    fun deleteQuestFlow_removesQuestFromList() = runTest(testDispatcher) {
        collectUiState()
        repository.createQuest(QuestDraft(title = "To Delete", scheduledDate = today))

        val state = viewModel.uiState.first { it.occurrences.isNotEmpty() }
        val occ = state.occurrences.first()

        viewModel.requestDelete(occ)
        val deleteRequestedState = viewModel.uiState.first { it.deleteTarget != null }
        assertEquals("To Delete", deleteRequestedState.deleteTarget?.title)

        viewModel.confirmDelete(QuestScope.ALL)
        val afterDeleteState = viewModel.uiState.first { it.deleteTarget == null && it.occurrences.none { o -> o.title == "To Delete" } }
        assertTrue(afterDeleteState.occurrences.isEmpty())
    }

    @Test
    fun prepareEditor_asyncLoadsExistingQuestWithoutBlocking() = runTest(testDispatcher) {
        collectUiState()
        val questId = repository.createQuest(
            QuestDraft(
                title = "Async Quest",
                priority = TaskPriority.CRITICAL,
                scheduledDate = today,
                recurrence = QuestRecurrence.WEEKLY,
                intervalText = "2",
                endDate = today.plusMonths(3),
            )
        )

        // Invoke prepareEditor for the existing quest
        viewModel.prepareEditor(questId, today)

        // Editor state should be loaded with the existing quest details
        val loadedState = viewModel.uiState.first { it.isEditorOpen && !it.isEditorLoading && it.editingDraft?.title == "Async Quest" }
        assertNotNull(loadedState.editingDraft)
        assertEquals("Async Quest", loadedState.editingDraft?.title)
        assertEquals(TaskPriority.CRITICAL, loadedState.editingDraft?.priority)
        assertEquals(QuestRecurrence.WEEKLY, loadedState.editingDraft?.recurrence)
        assertEquals(2, loadedState.editingDraft?.parsedInterval)
        assertEquals(today.plusMonths(3), loadedState.editingDraft?.endDate)
        assertEquals(questId, loadedState.editingQuestId)
    }

    @Test
    fun prepareEditor_nullId_opensCreateEditorImmediately() = runTest(testDispatcher) {
        collectUiState()
        viewModel.prepareEditor(null, today)

        val createModeState = viewModel.uiState.first { it.isEditorOpen && !it.isEditorLoading && it.editingQuestId == null }
        assertNotNull(createModeState.editingDraft)
        assertEquals("", createModeState.editingDraft?.title)
        assertEquals(today, createModeState.editingDraft?.scheduledDate)
        assertFalse(createModeState.isEditorLoading)
    }
}
