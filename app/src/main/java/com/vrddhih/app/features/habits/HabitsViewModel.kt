package com.vrddhih.app.features.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vrddhih.app.VrddhihApplication
import com.vrddhih.app.core.data.habit.HabitAnalytics
import com.vrddhih.app.core.data.habit.HabitDraft
import com.vrddhih.app.core.data.habit.HabitDraftError
import com.vrddhih.app.core.data.habit.HabitExtraGoalDraft
import com.vrddhih.app.core.data.habit.HabitExtraGoalsHelper
import com.vrddhih.app.core.data.habit.HabitScheduler
import com.vrddhih.app.core.data.habit.HabitStatistics
import com.vrddhih.app.core.data.local.dao.HabitWithConfigs
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.data.local.entity.HabitDailyProgress
import com.vrddhih.app.core.data.local.entity.HabitEntity
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate

data class HabitWeeklyDayStatus(
    val date: LocalDate,
    val isScheduled: Boolean,
    val isCompleted: Boolean,
    val isInProgress: Boolean = false,
    val isToday: Boolean = false,
    val isFuture: Boolean = false,
)

data class HabitItemDisplay(
    val habitWithConfigs: HabitWithConfigs,
    val category: CategoryEntity?,
    val isScheduledToday: Boolean,
    val weeklyDays: List<HabitWeeklyDayStatus>,
    val currentStreak: Int,
    val weeklyCompletionRate: Float,
    val extraGoals: List<HabitExtraGoalDraft>,
    val cleanNotes: String,
    val todayProgress: HabitDailyProgress? = null,
    val checklistItemProgress: Map<Long, Boolean> = emptyMap(),
    val statistics: HabitStatistics? = null,
)

data class HabitsUiState(
    val habitItems: List<HabitWithConfigs> = emptyList(),
    val habitDisplays: List<HabitItemDisplay> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val progressMap: Map<Long, HabitDailyProgress> = emptyMap(),
    val checklistProgressMap: Map<Long, Map<Long, Boolean>> = emptyMap(),
    val isEditorOpen: Boolean = false,
    val editingHabit: HabitWithConfigs? = null,
    val editingDraft: HabitDraft? = null,
    val isEditorLoading: Boolean = false,
    val editorErrors: Set<HabitDraftError> = emptySet(),
    val weekDays: List<LocalDate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

private data class HabitsData(
    val habits: List<HabitWithConfigs>,
    val categories: List<CategoryEntity>,
    val progressMap: Map<Long, HabitDailyProgress>,
    val checklistMap: Map<Long, Map<Long, Boolean>>,
    val weeklyProgressList: List<HabitDailyProgress>,
)

private data class EditorSubState(
    val isOpen: Boolean,
    val habit: HabitWithConfigs?,
    val draft: HabitDraft?,
    val isLoading: Boolean,
    val errors: Set<HabitDraftError>,
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
    private val editingDraftFlow = MutableStateFlow<HabitDraft?>(null)
    private val isEditorLoadingFlow = MutableStateFlow(false)
    private val editorErrorsFlow = MutableStateFlow<Set<HabitDraftError>>(emptySet())
    private val errorFlow = MutableStateFlow<String?>(null)

    private val editorSubStateFlow = combine(
        isEditorOpenFlow,
        editingHabitFlow,
        editingDraftFlow,
        isEditorLoadingFlow,
        editorErrorsFlow,
    ) { isOpen, habit, draft, isLoading, errors ->
        EditorSubState(isOpen, habit, draft, isLoading, errors)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HabitsUiState> = dateState
        .map { it.selectedDate to it.today }
        .flatMapLatest { (selectedDate, today) ->
            val startWeek = today.minusDays(6)
            val endWeek = today
            val historyStart = today.minusDays(90)

            val habitsDataFlow = combine(
                habitRepository.observeActiveHabitsWithConfigs(),
                categoryRepository.observeAll(),
                habitRepository.observeDailyProgressOn(today),
                habitRepository.observeChecklistProgressOn(today),
                habitRepository.observeAllDailyProgressBetween(historyStart, endWeek),
            ) { habits, categories, progressList, checklistProgressList, allProgressList ->
                val progressMap = progressList.associateBy { it.habitId }
                val checklistMap = checklistProgressList
                    .groupBy { it.habitId }
                    .mapValues { (_, items) -> items.associate { it.itemId to (it.completedAt != null) } }
                HabitsData(habits, categories, progressMap, checklistMap, allProgressList)
            }

            combine(
                habitsDataFlow,
                editorSubStateFlow,
                errorFlow,
            ) { data, editor, error ->
                val currentWeekDays = (6 downTo 0).map { today.minusDays(it.toLong()) }
                val weeklyProgressByHabitAndDate = data.weeklyProgressList
                    .associateBy { it.habitId to it.date }

                val categoriesMap = data.categories.associateBy { it.id }

                val displays = data.habits.map { habitWithConfigs ->
                    val habit = habitWithConfigs.habit
                    val category = habit.categoryId?.let { categoriesMap[it] }
                    val (cleanNotes, extraGoals) = HabitExtraGoalsHelper.parse(habit.description)

                    val weeklyDays = currentWeekDays.map { date ->
                        val progress = weeklyProgressByHabitAndDate[habit.id to date]
                        val isSched = HabitScheduler.isScheduledOn(habit, date)
                        val isDone = progress?.completedAt != null || progress?.completionState == com.vrddhih.app.core.data.local.entity.HabitCompletionState.COMPLETED
                        val inProgress = !isDone && (
                            progress?.completionState == com.vrddhih.app.core.data.local.entity.HabitCompletionState.PARTIALLY_COMPLETED ||
                            (progress?.numericValue ?: 0L) > 0L ||
                            (progress?.timerSeconds ?: 0) > 0
                        )
                        HabitWeeklyDayStatus(
                            date = date,
                            isScheduled = isSched,
                            isCompleted = isDone,
                            isInProgress = inProgress,
                            isToday = date == today,
                            isFuture = date.isAfter(today),
                        )
                    }

                    val schedDaysInWeek = weeklyDays.count { it.isScheduled && !it.isFuture }
                    val doneDaysInWeek = weeklyDays.count { it.isScheduled && it.isCompleted }
                    val weekRate = if (schedDaysInWeek > 0) doneDaysInWeek.toFloat() / schedDaysInWeek.toFloat() else 0f

                    val habitProgressHistory = data.weeklyProgressList.filter { it.habitId == habit.id }
                    val schedDates = HabitScheduler.getScheduledDatesInRange(habit, habit.startDate, today)
                    val streak = HabitAnalytics.calculateStreak(habit, habitProgressHistory, schedDates)

                    val stats = HabitAnalytics.calculateStatistics(
                        habit = habit,
                        progressList = habitProgressHistory,
                        checklistProgressMap = emptyMap(),
                        numericConfig = habitWithConfigs.numericConfig,
                        timerConfig = habitWithConfigs.timerConfig,
                        limitConfig = habitWithConfigs.limitConfig,
                        checklistItemCount = habitWithConfigs.checklistItems.size,
                        startDate = habit.startDate,
                        endDate = today,
                    )

                    HabitItemDisplay(
                        habitWithConfigs = habitWithConfigs,
                        category = category,
                        isScheduledToday = HabitScheduler.isScheduledOn(habit, today),
                        weeklyDays = weeklyDays,
                        currentStreak = streak.current,
                        weeklyCompletionRate = weekRate,
                        extraGoals = extraGoals,
                        cleanNotes = cleanNotes,
                        todayProgress = data.progressMap[habit.id],
                        checklistItemProgress = data.checklistMap[habit.id] ?: emptyMap(),
                        statistics = stats,
                    )
                }

                HabitsUiState(
                    habitItems = data.habits,
                    habitDisplays = displays,
                    categories = data.categories,
                    progressMap = data.progressMap,
                    checklistProgressMap = data.checklistMap,
                    isEditorOpen = editor.isOpen,
                    editingHabit = editor.habit,
                    editingDraft = editor.draft,
                    isEditorLoading = editor.isLoading,
                    editorErrors = editor.errors,
                    weekDays = currentWeekDays,
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

    fun prepareEditor(habitId: Long?) {
        viewModelScope.launch {
            isEditorLoadingFlow.value = true
            try {
                val categories = categoryRepository.observeAll().first()
                val defaultCategoryId = categories.firstOrNull()?.id

                if (habitId == null || habitId == 0L) {
                    editingDraftFlow.value = HabitDraft(
                        startDate = dateState.value.today,
                        categoryId = defaultCategoryId,
                    )
                    editorErrorsFlow.value = emptySet()
                } else {
                    val habitWithConfigs = habitRepository.getHabitWithConfigs(habitId)
                    if (habitWithConfigs != null) {
                        val habit = habitWithConfigs.habit
                        val (notes, extraGoals) = HabitExtraGoalsHelper.parse(habit.description)
                        val weekdays = DayOfWeek.entries.filter { (habit.scheduleDaysMask and (1 shl (it.value - 1))) != 0 }.toSet()
                        val monthDays = (1..31).filter { (habit.scheduleDaysMask and (1 shl (it - 1))) != 0 }.toSet()

                        val numConfig = habitWithConfigs.numericConfig
                        val timerConfig = habitWithConfigs.timerConfig
                        val timerSec = timerConfig?.targetDurationSeconds ?: (15 * 60)

                        editingDraftFlow.value = HabitDraft(
                            id = habit.id,
                            name = habit.name,
                            description = notes,
                            categoryId = habit.categoryId ?: defaultCategoryId,
                            type = habit.type,
                            scheduleType = habit.scheduleType,
                            intervalText = habit.scheduleInterval.toString(),
                            weekdays = if (weekdays.isEmpty()) DayOfWeek.entries.toSet() else weekdays,
                            monthDays = if (monthDays.isEmpty()) setOf(1) else monthDays,
                            startDate = habit.startDate,
                            endDate = habit.endDate,
                            xpPerCompletion = habit.xpPerCompletion,
                            numericTarget = (numConfig?.targetValue ?: 1L).toString(),
                            numericUnit = numConfig?.unit.orEmpty(),
                            numericCondition = numConfig?.direction ?: NumericDirection.AT_LEAST,
                            numericIncrement = (numConfig?.incrementValue ?: 1L).toString(),
                            timerHours = (timerSec / 3600).toString(),
                            timerMinutes = ((timerSec % 3600) / 60).toString(),
                            checklistItems = if (habitWithConfigs.checklistItems.isEmpty()) listOf("") else habitWithConfigs.checklistItems.map { it.title },
                            extraGoals = extraGoals,
                        )
                        editorErrorsFlow.value = emptySet()
                    }
                }
            } catch (e: Exception) {
                errorFlow.value = e.message
            } finally {
                isEditorLoadingFlow.value = false
            }
        }
    }

    fun updateEditorDraft(draft: HabitDraft) {
        editingDraftFlow.value = draft
        if (editorErrorsFlow.value.isNotEmpty()) {
            editorErrorsFlow.value = draft.validate()
        }
    }

    fun saveEditor(onSaved: () -> Unit) {
        val draft = editingDraftFlow.value ?: return
        val errors = draft.validate()
        if (errors.isNotEmpty()) {
            editorErrorsFlow.value = errors
            return
        }

        viewModelScope.launch {
            try {
                val category = draft.categoryId?.let { categoryRepository.getById(it) }
                val finalDescription = HabitExtraGoalsHelper.format(draft.description, draft.extraGoals)
                val mask = when (draft.scheduleType) {
                    HabitScheduleType.DAILY -> HabitEntity.EVERY_DAY_MASK
                    HabitScheduleType.SELECTED_WEEKDAYS -> HabitEntity.maskOf(*draft.weekdays.toTypedArray())
                    HabitScheduleType.MONTHLY_DAYS -> HabitEntity.monthMaskOf(*draft.monthDays.toIntArray())
                    HabitScheduleType.EVERY_N_DAYS -> HabitEntity.EVERY_DAY_MASK
                    HabitScheduleType.WEEKLY_FREQUENCY -> HabitEntity.EVERY_DAY_MASK
                }

                habitRepository.saveHabitWithConfigs(
                    habitId = draft.id,
                    name = draft.name,
                    type = draft.type,
                    description = finalDescription,
                    scheduleType = draft.scheduleType,
                    scheduleInterval = draft.parsedInterval ?: 1,
                    scheduleDaysMask = mask,
                    startDate = draft.startDate,
                    endDate = draft.endDate,
                    categoryId = draft.categoryId,
                    iconKey = category?.iconKey,
                    colorArgb = category?.colorArgb,
                    xpPerCompletion = draft.xpPerCompletion,
                    numericTargetValue = draft.parsedNumericTarget,
                    numericUnit = draft.numericUnit,
                    numericDirection = draft.numericCondition,
                    numericIncrement = draft.parsedNumericIncrement,
                    timerTargetMinutes = draft.parsedTimerTargetMinutes,
                    checklistItemTitles = draft.checklistItems.filter { it.isNotBlank() },
                )
                editingDraftFlow.value = null
                onSaved()
            } catch (e: Exception) {
                errorFlow.value = e.message
            }
        }
    }

    fun saveAndNewHabit(defaultCategoryId: String? = null, onSaved: () -> Unit = {}) {
        val draft = editingDraftFlow.value ?: return
        val errors = draft.validate()
        if (errors.isNotEmpty()) {
            editorErrorsFlow.value = errors
            return
        }

        viewModelScope.launch {
            try {
                val category = draft.categoryId?.let { categoryRepository.getById(it) }
                val finalDescription = HabitExtraGoalsHelper.format(draft.description, draft.extraGoals)
                val mask = when (draft.scheduleType) {
                    HabitScheduleType.DAILY -> HabitEntity.EVERY_DAY_MASK
                    HabitScheduleType.SELECTED_WEEKDAYS -> HabitEntity.maskOf(*draft.weekdays.toTypedArray())
                    HabitScheduleType.MONTHLY_DAYS -> HabitEntity.monthMaskOf(*draft.monthDays.toIntArray())
                    HabitScheduleType.EVERY_N_DAYS -> HabitEntity.EVERY_DAY_MASK
                    HabitScheduleType.WEEKLY_FREQUENCY -> HabitEntity.EVERY_DAY_MASK
                }

                habitRepository.saveHabitWithConfigs(
                    habitId = null, // Ensure new record insert
                    name = draft.name,
                    type = draft.type,
                    description = finalDescription,
                    scheduleType = draft.scheduleType,
                    scheduleInterval = draft.parsedInterval ?: 1,
                    scheduleDaysMask = mask,
                    startDate = draft.startDate,
                    endDate = draft.endDate,
                    categoryId = draft.categoryId,
                    iconKey = category?.iconKey,
                    colorArgb = category?.colorArgb,
                    xpPerCompletion = draft.xpPerCompletion,
                    numericTargetValue = draft.parsedNumericTarget,
                    numericUnit = draft.numericUnit,
                    numericDirection = draft.numericCondition,
                    numericIncrement = draft.parsedNumericIncrement,
                    timerTargetMinutes = draft.parsedTimerTargetMinutes,
                    checklistItemTitles = draft.checklistItems.filter { it.isNotBlank() },
                )

                // Reset form for next habit cleanly with null ID and fresh defaults
                val today = LocalDate.now(clock)
                editingDraftFlow.value = HabitDraft(
                    id = null,
                    categoryId = draft.categoryId ?: defaultCategoryId,
                    startDate = today,
                    type = draft.type,
                )
                editorErrorsFlow.value = emptySet()
                onSaved()
            } catch (e: Exception) {
                errorFlow.value = e.message
            }
        }
    }

    fun deleteEditingHabit(onDeleted: () -> Unit) {
        val habitId = editingDraftFlow.value?.id ?: return
        viewModelScope.launch {
            try {
                habitRepository.deleteHabit(habitId)
                editingDraftFlow.value = null
                onDeleted()
            } catch (e: Exception) {
                errorFlow.value = e.message
            }
        }
    }

    fun deleteHabit(habitId: Long, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                habitRepository.deleteHabit(habitId)
                onDeleted()
            } catch (e: Exception) {
                errorFlow.value = e.message
            }
        }
    }

    suspend fun createCustomCategory(name: String, iconKey: String, colorArgb: Int): CategoryEntity? {
        return categoryRepository.createCustom(name, iconKey, colorArgb)
    }

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
        editingDraftFlow.value = null
        editorErrorsFlow.value = emptySet()
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
                val category = categoryId?.let { categoryRepository.getById(it) }
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
                    iconKey = category?.iconKey,
                    colorArgb = category?.colorArgb,
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
