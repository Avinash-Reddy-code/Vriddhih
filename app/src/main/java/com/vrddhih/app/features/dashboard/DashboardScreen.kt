package com.vrddhih.app.features.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.vrddhih.app.core.data.repository.DayActivity
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTheme
import com.vrddhih.app.core.time.DateSelectionState
import com.vrddhih.app.core.ui.components.CalendarToggleButton
import com.vrddhih.app.core.ui.components.DateStrip
import com.vrddhih.app.core.ui.components.ScreenHeader
import com.vrddhih.app.core.ui.components.SystemPanel
import com.vrddhih.app.core.ui.components.VrddhihCalendarDialog
import com.vrddhih.app.features.dashboard.mock.DashboardMockData
import java.time.LocalDate

@Composable
fun DashboardRoute(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dateState by viewModel.dateState.collectAsStateWithLifecycle()
    val dayLog by viewModel.dayLog.collectAsStateWithLifecycle()
    val occurrences by viewModel.occurrences.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val dayLogData by viewModel.dayLogData.collectAsStateWithLifecycle()

    // Re-read the device date on every resume so "today" stays correct past midnight or after a time-zone change.
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshToday()
        onPauseOrDispose { }
    }

    DashboardScreen(
        uiState = uiState,
        dateState = dateState,
        dayLog = dayLog,
        occurrences = occurrences,
        habits = habits,
        goals = goals,
        dayLogData = dayLogData,
        hasRealQuests = viewModel.hasRealQuests,
        hasRealHabits = viewModel.hasRealHabits,
        hasRealGoals = viewModel.hasRealGoals,
        onToggleQuest = viewModel::toggleQuest,
        onToggleHabit = viewModel::toggleHabit,
        onToggleQuestPreview = viewModel::toggleQuestPreview,
        onDateSelected = viewModel::selectDate,
        onShiftWindow = viewModel::shiftWindow,
        onSelectToday = viewModel::selectToday,
        modifier = modifier,
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    dateState: DateSelectionState,
    dayLog: DayLogUiState,
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
    onDateSelected: (LocalDate) -> Unit,
    onShiftWindow: (Int) -> Unit = {},
    onSelectToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCalendar by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = VrddhihDimens.ContentMaxWidth)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.lg),
        ) {
            item(key = "header") {
                ScreenHeader(
                    eyebrow = stringResource(R.string.dashboard_eyebrow),
                    title = stringResource(R.string.dashboard_title),
                    subtitle = stringResource(R.string.dashboard_subtitle),
                    action = { CalendarToggleButton(onClick = { showCalendar = true }) },
                )
            }
            item(key = "date-strip") {
                DateStrip(
                    selectedDate = dateState.selectedDate,
                    today = dateState.today,
                    onDateSelected = onDateSelected,
                    onSelectToday = onSelectToday,
                )
            }
            // 3. Daily progress summary
            item(key = "summary") {
                if (hasRealQuests) {
                    val completed = occurrences.count { it.isCompleted }
                    val remaining = occurrences.count { !it.isCompleted }
                    val xp = occurrences.filter { it.isCompleted }.sumOf { it.quest.xpReward }
                    DailySummaryPanel(
                        completedCount = completed,
                        remainingCount = remaining,
                        xpEarned = xp,
                        isSample = false,
                    )
                } else {
                    DailySummaryPanel(uiState = uiState)
                }
            }
            // 4. Today's Quests (or date-specific heading)
            item(key = "quests") {
                if (hasRealQuests) {
                    RealQuestsPanel(
                        date = dateState.selectedDate,
                        isToday = dateState.isTodaySelected,
                        occurrences = occurrences,
                        onToggleQuest = onToggleQuest,
                    )
                } else {
                    TodaysQuestsPanel(quests = uiState.quests, onToggleQuest = onToggleQuestPreview)
                }
            }
            // 5. Compact Day Log or recent-activity summary
            item(key = "day-log") {
                DayLogPanel(dateState = dateState, dayLog = dayLog, dayLogData = dayLogData)
            }
            item(key = "player") { PlayerStatusPanel(player = uiState.player) }
            item(key = "habits") {
                if (hasRealHabits) {
                    RealHabitProgressPanel(
                        date = dateState.selectedDate,
                        isToday = dateState.isTodaySelected,
                        habits = habits,
                        onToggleHabit = onToggleHabit,
                    )
                } else {
                    HabitProgressPanel(uiState = uiState)
                }
            }
            item(key = "goals") {
                if (hasRealGoals) {
                    RealGoalsPanel(goals = goals)
                } else {
                    GoalsPanel(goals = uiState.goals)
                }
            }
            item(key = "deadlines") { DeadlinesPanel(deadlines = uiState.deadlines) }
            item(key = "footer") {
                Text(
                    text = stringResource(R.string.dashboard_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = VrddhihColors.TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = VrddhihSpacing.sm),
                )
            }
        }
    }

    if (showCalendar) {
        VrddhihCalendarDialog(
            selectedDate = dateState.selectedDate,
            today = dateState.today,
            onDateSelected = { date ->
                onDateSelected(date)
                showCalendar = false
            },
            onTodaySelected = {
                onSelectToday()
                showCalendar = false
            },
            onDismiss = { showCalendar = false },
        )
    }
}

@Composable
private fun PreviewNotice(modifier: Modifier = Modifier) {
    SystemPanel(
        modifier = modifier,
        accent = VrddhihColors.Sample,
        contentPadding = PaddingValues(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = VrddhihColors.Sample,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(VrddhihSpacing.md))
            Text(
                text = stringResource(R.string.dashboard_preview_notice),
                style = MaterialTheme.typography.bodySmall,
                color = VrddhihColors.TextSecondary,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF04060C, widthDp = 390, heightDp = 2000)
@Composable
private fun DashboardScreenPreview() {
    val today = LocalDate.now()
    VrddhihTheme {
        DashboardScreen(
            uiState = DashboardMockData.initialState(),
            dateState = DateSelectionState(today = today, selectedDate = today),
            dayLog = DayLogUiState.Loaded(today, DayActivity(questCompletions = 0, habitCheckIns = 0, transactions = 0)),
            onToggleQuestPreview = {},
            onDateSelected = {},
            onSelectToday = {},
        )
    }
}
