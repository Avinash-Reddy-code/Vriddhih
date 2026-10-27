package com.vrddhih.app.features.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vrddhih.app.VrddhihApplication
import com.vrddhih.app.core.data.local.dao.HabitWithConfigs
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.data.local.entity.HabitDailyProgress
import com.vrddhih.app.core.data.local.entity.HabitScheduleType
import com.vrddhih.app.core.data.local.entity.HabitType
import com.vrddhih.app.core.data.local.entity.NumericDirection
import com.vrddhih.app.core.data.repository.CategoryRepository
import com.vrddhih.app.core.data.repository.HabitRepository
import com.vrddhih.app.core.time.DateSelectionState
import com.vrddhih.app.core.time.DateSelectionStateHolder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

data class HabitsUiState(
    val habitItems: List<HabitWithConfigs> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val progressMap: Map<Long, HabitDailyProgress> = emptyMap(),
    val checklistProgressMap: Map<Long, Map<Long, Boolean>> = emptyMap(),
    val isEditorOpen: Boolean = false,
    val editingHabit: HabitWithConfigs? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

private data class HabitsData(
    val habits: List<HabitWithConfigs>,
    val categories: List<CategoryEntity>,
    val progressMap: Map<Long, HabitDailyProgress>,
    val checklistMap: Map<Long, Map<Long, Boolean>>,
)

class HabitsViewModel(
    private val habitRepository: HabitRepository,
    private val categoryRepository: CategoryRepository,
    private val clock: Clock,
    private val dateSelection: DateSelectionStateHolder = DateSelectionStateHolder({ LocalDate.now(clock) }),
) : ViewModel() {

    val dateState: StateFlow<DateSelectionState> = dateSelection.state

    private val isEditorOpenFlow = MutableStateFlow(false)
    private val editingHabitFlow = MutableStateFlow<HabitWithConfigs?>(null)
    private val errorFlow = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HabitsUiState> = dateState
        .map { it.selectedDate }
        .flatMapLatest { selectedDate ->
            val habitsDataFlow = combine(
                habitRepository.observeActiveHabitsWithConfigs(),
                categoryRepository.observeAll(),
                habitRepository.observeDailyProgressOn(selectedDate),
                habitRepository.observeChecklistProgressOn(selectedDate),
            ) { habits, categories, progressList, checklistProgressList ->
                val progressMap = progressList.associateBy { it.habitId }
                val checklistMap = checklistProgressList
                    .groupBy { it.habitId }
                    .mapValues { (_, items) -> items.associate { it.itemId to (it.completedAt != null) } }
                HabitsData(habits, categories, progressMap, checklistMap)
            }

            combine(
                habitsDataFlow,
                isEditorOpenFlow,
                editingHabitFlow,
                errorFlow,
            ) { data, isEditorOpen, editingHabit, error ->
                HabitsUiState(
                    habitItems = data.habits,
                    categories = data.categories,
                    progressMap = data.progressMap,
                    checklistProgressMap = data.checklistMap,
                    isEditorOpen = isEditorOpen,
                    editingHabit = editingHabit,
                    isLoading = false,
                    error = error,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitsUiState(isLoading = true),
        )

    fun selectDate(date: LocalDate) = dateSelection.select(date)

    fun selectToday() = dateSelection.selectToday()

    fun shiftWindow(direction: Int) = dateSelection.shiftWindow(direction)

    fun refreshToday() = dateSelection.refreshToday()

    fun openCreateHabit() {
        editingHabitFlow.value = null
        isEditorOpenFlow.value = true
    }

    fun openEditHabit(habitWithConfigs: HabitWithConfigs) {
        editingHabitFlow.value = habitWithConfigs
        isEditorOpenFlow.value = true
    }

    fun closeEditor() {
        isEditorOpenFlow.value = false
        editingHabitFlow.value = null
    }

    fun saveHabit(
        habitId: Long?,
        name: String,
        type: HabitType,
        description: String,
        scheduleType: HabitScheduleType,
        scheduleInterval: Int,
        scheduleDaysMask: Int,
        startDate: LocalDate,
        categoryId: String?,
        xpPerCompletion: Int,
        numericTargetValue: Long,
        numericUnit: String,
        numericDirection: NumericDirection,
        numericIncrement: Long,
        timerTargetMinutes: Int,
        limitDailyTarget: Long,
        limitUnit: String,
        checklistItemTitles: List<String>,
    ) {
        viewModelScope.launch {
            try {
                habitRepository.saveHabitWithConfigs(
                    habitId = habitId,
                    name = name,
                    type = type,
                    description = description,
                    scheduleType = scheduleType,
                    scheduleInterval = scheduleInterval,
                    scheduleDaysMask = scheduleDaysMask,
                    startDate = startDate,
                    categoryId = categoryId,
                    xpPerCompletion = xpPerCompletion,
                    numericTargetValue = numericTargetValue,
                    numericUnit = numericUnit,
                    numericDirection = numericDirection,
                    numericIncrement = numericIncrement,
                    timerTargetMinutes = timerTargetMinutes,
                    limitDailyTarget = limitDailyTarget,
                    limitUnit = limitUnit,
                    checklistItemTitles = checklistItemTitles,
                )
                closeEditor()
            } catch (e: Exception) {
                errorFlow.value = e.message
            }
        }
    }

    fun completeHabit(habitId: Long) {
        viewModelScope.launch {
            val date = dateState.value.selectedDate
            habitRepository.completeHabit(habitId, date)
        }
    }

    fun undoCompletion(habitId: Long) {
        viewModelScope.launch {
            val date = dateState.value.selectedDate
            habitRepository.undoCompletion(habitId, date)
        }
    }

    fun updateNumericProgress(habitId: Long, value: Long) {
        viewModelScope.launch {
            val date = dateState.value.selectedDate
            habitRepository.updateNumericProgress(habitId, date, value)
        }
    }

    fun logTimerSeconds(habitId: Long, additionalSeconds: Int) {
        viewModelScope.launch {
            val date = dateState.value.selectedDate
            habitRepository.logTimerSeconds(habitId, date, additionalSeconds)
        }
    }

    fun updateLimitProgress(habitId: Long, value: Long) {
        viewModelScope.launch {
            val date = dateState.value.selectedDate
            habitRepository.updateLimitProgress(habitId, date, value)
        }
    }

    fun toggleChecklistItem(habitId: Long, itemId: Long, completed: Boolean) {
        viewModelScope.launch {
            val date = dateState.value.selectedDate
            habitRepository.toggleChecklistItem(habitId, date, itemId, completed)
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            val habit = habitRepository.getHabit(habitId) ?: return@launch
            habitRepository.deleteHabit(habit)
        }
    }

    fun clearError() {
        errorFlow.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as VrddhihApplication
                HabitsViewModel(
                    habitRepository = app.container.habitRepository,
                    categoryRepository = app.container.categoryRepository,
                    clock = java.time.Clock.systemUTC(),
                    dateSelection = DateSelectionStateHolder(),
                )
            }
        }
    }
}
