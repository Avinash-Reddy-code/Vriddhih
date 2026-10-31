package com.vrddhih.app.features.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTheme
import com.vrddhih.app.core.time.DateSelectionState
import com.vrddhih.app.core.ui.components.DateStrip
import com.vrddhih.app.features.dashboard.mock.DashboardMockData
import java.time.LocalDate

@Composable
fun DashboardRoute(
    modifier: Modifier = Modifier,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
) {
    val dateState by viewModel.dateState.collectAsStateWithLifecycle()

    // Re-read the device date on every resume so "today" stays correct past midnight or after a time-zone change.
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshToday()
        onPauseOrDispose { }
    }

    DashboardScreen(
        dateState = dateState,
        onDateSelected = viewModel::selectDate,
        onSelectToday = viewModel::selectToday,
        onNavigateToCategories = onNavigateToCategories,
        onNavigateToStats = onNavigateToStats,
        modifier = modifier,
    )
}

@Composable
fun DashboardScreen(
    dateState: DateSelectionState,
    onDateSelected: (LocalDate) -> Unit,
    onSelectToday: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    uiState: DashboardUiState = DashboardMockData.initialState(),
    dayLog: DayLogUiState = DayLogUiState.Unavailable,
    occurrences: List<com.vrddhih.app.core.data.quest.QuestOccurrence> = emptyList(),
    habits: List<DashboardHabitItem> = emptyList(),
    goals: List<DashboardGoalItem> = emptyList(),
    dayLogData: com.vrddhih.app.core.data.quest.DayLog? = null,
    hasRealQuests: Boolean = false,
    hasRealHabits: Boolean = false,
    hasRealGoals: Boolean = false,
    onToggleQuest: (com.vrddhih.app.core.data.quest.QuestOccurrence) -> Unit = {},
    onToggleHabit: (Long) -> Unit = {},
    onToggleQuestPreview: (String) -> Unit = {},
    onShiftWindow: (Int) -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = VrddhihDimens.ContentMaxWidth)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = VrddhihSpacing.lg,
                end = VrddhihSpacing.lg,
                top = VrddhihSpacing.xs,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
        ) {
            item(key = "date-strip") {
                DateStrip(
                    selectedDate = dateState.selectedDate,
                    today = dateState.today,
                    onDateSelected = onDateSelected,
                    onSelectToday = onSelectToday,
                )
            }
        }

        Text(
            text = stringResource(R.string.dashboard_footer),
            style = MaterialTheme.typography.bodySmall,
            color = VrddhihColors.TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = VrddhihDimens.ContentMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.xl),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 800)
@Composable
private fun DashboardScreenPreview() {
    val today = LocalDate.now()
    VrddhihTheme {
        DashboardScreen(
            dateState = DateSelectionState(today = today, selectedDate = today),
            onDateSelected = {},
            onSelectToday = {},
        )
    }
}
