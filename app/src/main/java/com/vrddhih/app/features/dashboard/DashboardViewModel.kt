package com.vrddhih.app.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vrddhih.app.VrddhihApplication
import com.vrddhih.app.core.data.repository.DayActivity
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
 * - [dayLog]: real local records for the selected day, read from Room.
 * - [uiState]: Phase 0 sample content. Quest toggles here are in-memory only and never saved.
 */
class DashboardViewModel(
    observeDayActivity: (LocalDate) -> Flow<DayActivity>,
    currentDate: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val dateSelection = DateSelectionStateHolder(currentDate)
    val dateState: StateFlow<DateSelectionState> = dateSelection.state

    private val _uiState = MutableStateFlow(DashboardMockData.initialState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

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

    fun selectDate(date: LocalDate) = dateSelection.select(date)

    fun selectToday() = dateSelection.selectToday()

    fun refreshToday() = dateSelection.refreshToday()

    /** Temporary preview interaction. The change is lost when the process ends. */
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
                DashboardViewModel(observeDayActivity = app.container.dayActivityRepository::observeDayActivity)
            }
        }
    }
}
