package com.vrddhih.app.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vrddhih.app.VrddhihApplication
import com.vrddhih.app.core.data.local.entity.GoalType
import com.vrddhih.app.core.data.quest.DayLog
import com.vrddhih.app.core.data.quest.QuestOccurrence
import com.vrddhih.app.core.data.repository.CategoryRepository
import com.vrddhih.app.core.data.repository.DayActivity
import com.vrddhih.app.core.data.repository.GoalRepository
import com.vrddhih.app.core.data.repository.HabitRepository
import com.vrddhih.app.core.data.repository.QuestRepository
import com.vrddhih.app.core.time.DateSelectionState
import com.vrddhih.app.core.time.DateSelectionStateHolder
import com.vrddhih.app.features.dashboard.mock.DashboardMockData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Real, on-device records for the selected day. */
sealed interface DayLogUiState {
    data object Loading : DayLogUiState
    data class Loaded(val date: LocalDate, val activity: DayActivity) : DayLogUiState
    data object Unavailable : DayLogUiState
}

/**
 * Dashboard state holder.
 * - [dateState]: the selected day (single source of truth for the date strip and calendar).
 * - [occurrences]: real quest occurrences for the selected day, read from Room.
 * - [habits]: real habit items for the selected day, read from Room.
 * - [goals]: real active goals with current progress, read from Room.
 * - [dayLogData]: real historical activity log for the selected day.
 * - [dayLog]: day activity counts.
 * - [uiState]: dashboard preview state; when repositories are provided, live habits & goals update this stream.
 */
class DashboardViewModel(
    observeDayActivity: (LocalDate) -> Flow<DayActivity>,
    currentDate: () -> LocalDate = { LocalDate.now() },
    private val questRepository: QuestRepository? = null,
    private val habitRepository: HabitRepository? = null,
    private val goalRepository: GoalRepository? = null,
    private val categoryRepository: CategoryRepository? = null,
) : ViewModel() {

    private val dateSelection = DateSelectionStateHolder(currentDate)
    val dateState: StateFlow<DateSelectionState> = dateSelection.state

    private val _uiState = MutableStateFlow(DashboardMockData.initialState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val occurrences: StateFlow<List<QuestOccurrence>> = dateState
        .map { it.selectedDate }
        .distinctUntilChanged()
        .flatMapLatest { date ->
            questRepository?.observeOccurrencesOn(date) ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val habits: StateFlow<List<DashboardHabitItem>> = dateState
        .map { it.selectedDate }
        .distinctUntilChanged()
        .flatMapLatest { date ->
            if (habitRepository == null) {
                flowOf(emptyList())
            } else {
                combine(
                    habitRepository.observeActiveHabitsWithConfigs(),
                    habitRepository.observeDailyProgressOn(date),
                ) { habitsWithConfigs, progressList ->
                    val progressMap = progressList.associateBy { it.habitId }
                    habitsWithConfigs
                        .filter { it.habit.isScheduledOn(date) || progressMap[it.habit.id]?.completedAt != null }
                        .map { habitWithConfigs ->
                            DashboardHabitItem(
                                id = habitWithConfigs.habit.id,
                                name = habitWithConfigs.habit.name,
                                isDone = progressMap[habitWithConfigs.habit.id]?.completedAt != null,
                            )
                        }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val goals: StateFlow<List<DashboardGoalItem>> = if (goalRepository == null) {
        MutableStateFlow(emptyList())
    } else {
        val categoriesFlow = categoryRepository?.observeAll() ?: flowOf(emptyList())
        combine(
            goalRepository.observeActiveGoalsWithAllConfigs(),
            categoriesFlow,
        ) { goalsWithConfigs, categories ->
            goalsWithConfigs.map { goalWithConfigs ->
                val categoryName = goalWithConfigs.goal.categoryId?.let { catId ->
                    categories.find { it.id == catId }?.name
                } ?: "Objective"

                val progress = when (goalWithConfigs.goal.type) {
                    GoalType.MILESTONE_BASED -> {
                        val total = goalWithConfigs.milestones.size
                        val completed = goalWithConfigs.milestones.count { it.completedAt != null }
                        if (total > 0) completed.toFloat() / total.toFloat() else 0f
                    }
                    GoalType.NUMERIC_TARGET -> {
                        val target = goalWithConfigs.numericTarget
                        if (target != null && target.targetValue > 0) {
                            target.currentValue.toFloat() / target.targetValue.toFloat()
                        } else 0f
                    }
                    GoalType.COMPLETION_BASED -> {
                        val target = goalWithConfigs.completionTarget
                        if (target != null && target.targetCount > 0) {
                            target.currentCount.toFloat() / target.targetCount.toFloat()
                        } else 0f
                    }
                }.coerceIn(0f, 1f)

                DashboardGoalItem(
                    id = goalWithConfigs.goal.id,
                    title = goalWithConfigs.goal.title,
                    category = categoryName,
                    progress = progress,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val dayLogData: StateFlow<DayLog?> = dateState
        .map { it.selectedDate }
        .distinctUntilChanged()
        .flatMapLatest { date ->
            questRepository?.observeDayLog(date) ?: flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val dayLog: StateFlow<DayLogUiState> = dateState
        .map { it.selectedDate }
        .distinctUntilChanged()
        .flatMapLatest { date ->
            observeDayActivity(date)
                .map<DayActivity, DayLogUiState> { DayLogUiState.Loaded(date, it) }
                .catch { emit(DayLogUiState.Unavailable) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DayLogUiState.Loading)

    val hasRealQuests: Boolean get() = questRepository != null
    val hasRealHabits: Boolean get() = habitRepository != null
    val hasRealGoals: Boolean get() = goalRepository != null

    init {
        // When real repositories are present, synchronize live habits into _uiState as well
        if (habitRepository != null) {
            viewModelScope.launch {
                habits.collect { habitItems ->
                    _uiState.update { state ->
                        state.copy(
                            habits = habitItems.map { HabitPreview(it.id.toString(), it.name, it.isDone) },
                        )
                    }
                }
            }
        }
        if (goalRepository != null) {
            viewModelScope.launch {
                goals.collect { goalItems ->
                    _uiState.update { state ->
                        state.copy(
                            goals = goalItems.map { GoalPreview(it.id.toString(), it.title, it.category, it.progress) },
                        )
                    }
                }
            }
        }
    }

    fun selectDate(date: LocalDate) = dateSelection.select(date)

    fun shiftWindow(direction: Int) = dateSelection.shiftWindow(direction)

    fun selectToday() = dateSelection.selectToday()

    fun refreshToday() = dateSelection.refreshToday()

    /** Toggles real quest completion in Room. */
    fun toggleQuest(occurrence: QuestOccurrence) {
        val repo = questRepository ?: return
        viewModelScope.launch {
            if (occurrence.isCompleted) {
                repo.reopenOccurrence(occurrence.questId, occurrence.date)
            } else {
                repo.completeOccurrence(occurrence.questId, occurrence.date, xpAwarded = occurrence.quest.xpReward)
            }
        }
    }

    /** Toggles real habit check-in in Room. */
    fun toggleHabit(habitId: Long) {
        val repo = habitRepository ?: return
        viewModelScope.launch {
            val date = dateState.value.selectedDate
            val habitItem = habits.value.find { it.id == habitId } ?: return@launch
            if (habitItem.isDone) {
                repo.undoCompletion(habitId, date)
            } else {
                repo.completeHabit(habitId, date)
            }
        }
    }

    /** Temporary preview interaction for mock preview panel. */
    fun toggleQuestPreview(questId: String) {
        _uiState.update { state ->
            state.copy(
                quests = state.quests.map { quest ->
                    if (quest.id == questId) quest.copy(isCompleted = !quest.isCompleted) else quest
                },
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as VrddhihApplication
                DashboardViewModel(
                    observeDayActivity = app.container.dayActivityRepository::observeDayActivity,
                    questRepository = app.container.questRepository,
                    habitRepository = app.container.habitRepository,
                    goalRepository = app.container.goalRepository,
                    categoryRepository = app.container.categoryRepository,
                )
            }
        }
    }
}
