package com.vrddhih.app.features.dashboard

import androidx.lifecycle.ViewModel
import com.vrddhih.app.features.dashboard.mock.DashboardMockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holds dashboard state for the lifetime of the dashboard back-stack entry.
 * Phase 0: state comes from mock data and quest toggles are in-memory only (never saved).
 */
class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardMockData.initialState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

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
}
