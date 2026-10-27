package com.vrddhih.app.features.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vrddhih.app.VrddhihApplication
import com.vrddhih.app.core.data.local.dao.GoalWithAllConfigs
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.data.local.entity.GoalStatus
import com.vrddhih.app.core.data.local.entity.GoalType
import com.vrddhih.app.core.data.repository.CategoryRepository
import com.vrddhih.app.core.data.repository.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

enum class GoalFilter {
    ALL,
    ACTIVE,
    COMPLETED,
}

data class GoalsUiState(
    val goals: List<GoalWithAllConfigs> = emptyList(),
    val filteredGoals: List<GoalWithAllConfigs> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val filter: GoalFilter = GoalFilter.ACTIVE,
    val isEditorOpen: Boolean = false,
    val editingGoal: GoalWithAllConfigs? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class GoalsViewModel(
    private val goalRepository: GoalRepository,
    private val categoryRepository: CategoryRepository,
    private val clock: Clock,
) : ViewModel() {

    private val filterFlow = MutableStateFlow(GoalFilter.ACTIVE)
    private val isEditorOpenFlow = MutableStateFlow(false)
    private val editingGoalFlow = MutableStateFlow<GoalWithAllConfigs?>(null)
    private val errorFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<GoalsUiState> = combine(
        goalRepository.observeAllGoalsWithAllConfigs(),
        categoryRepository.observeAll(),
        filterFlow,
        isEditorOpenFlow,
        editingGoalFlow,
    ) { goals, categories, filter, isEditorOpen, editingGoal ->
        val filtered = when (filter) {
            GoalFilter.ALL -> goals
            GoalFilter.ACTIVE -> goals.filter { it.goal.status == GoalStatus.ACTIVE || it.goal.status == GoalStatus.NOT_STARTED }
            GoalFilter.COMPLETED -> goals.filter { it.goal.status == GoalStatus.COMPLETED }
        }
        GoalsUiState(
            goals = goals,
            filteredGoals = filtered,
            categories = categories,
            filter = filter,
            isEditorOpen = isEditorOpen,
            editingGoal = editingGoal,
            isLoading = false,
            error = null,
        )
    }.combine(errorFlow) { state, error ->
        state.copy(error = error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GoalsUiState(isLoading = true),
    )

    fun setFilter(filter: GoalFilter) {
        filterFlow.value = filter
    }

    fun openCreateGoal() {
        editingGoalFlow.value = null
        isEditorOpenFlow.value = true
    }

    fun openEditGoal(goalWithConfigs: GoalWithAllConfigs) {
        editingGoalFlow.value = goalWithConfigs
        isEditorOpenFlow.value = true
    }

    fun closeEditor() {
        isEditorOpenFlow.value = false
        editingGoalFlow.value = null
    }

    fun saveGoal(
        goalId: Long?,
        title: String,
        description: String,
        type: GoalType,
        categoryId: String?,
        startDate: LocalDate?,
        targetDate: LocalDate?,
        milestoneTitles: List<String>,
        numericTargetValue: Long,
        numericCurrentValue: Long,
        numericUnit: String,
        completionTargetCount: Int,
        completionCurrentCount: Int,
    ) {
        viewModelScope.launch {
            try {
                goalRepository.saveGoalWithConfigs(
                    goalId = goalId,
                    title = title,
                    description = description,
                    type = type,
                    categoryId = categoryId,
                    startDate = startDate,
                    targetDate = targetDate,
                    milestoneTitles = milestoneTitles,
                    numericTargetValue = numericTargetValue,
                    numericCurrentValue = numericCurrentValue,
                    numericUnit = numericUnit,
                    completionTargetCount = completionTargetCount,
                    completionCurrentCount = completionCurrentCount,
                )
                closeEditor()
            } catch (e: Exception) {
                errorFlow.value = e.message
            }
        }
    }

    fun toggleMilestone(goalId: Long, milestoneId: Long, completed: Boolean) {
        viewModelScope.launch {
            goalRepository.toggleMilestone(goalId, milestoneId, completed)
        }
    }

    fun updateNumericProgress(goalId: Long, newValue: Long) {
        viewModelScope.launch {
            goalRepository.updateNumericProgress(goalId, newValue)
        }
    }

    fun updateCompletionCount(goalId: Long, newCount: Int) {
        viewModelScope.launch {
            goalRepository.updateCompletionCount(goalId, newCount)
        }
    }

    fun setGoalStatus(goalId: Long, status: GoalStatus) {
        viewModelScope.launch {
            goalRepository.setGoalStatus(goalId, status)
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            val goal = goalRepository.getGoal(goalId) ?: return@launch
            goalRepository.deleteGoal(goal)
            closeEditor()
        }
    }

    fun clearError() {
        errorFlow.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as VrddhihApplication
                GoalsViewModel(
                    goalRepository = app.container.goalRepository,
                    categoryRepository = app.container.categoryRepository,
                    clock = java.time.Clock.systemUTC(),
                )
            }
        }
    }
}
