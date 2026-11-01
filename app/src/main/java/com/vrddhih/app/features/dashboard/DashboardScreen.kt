package com.vrddhih.app.features.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dayLog by viewModel.dayLog.collectAsStateWithLifecycle()
    val occurrences by viewModel.occurrences.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val dayLogData by viewModel.dayLogData.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    // Re-read the device date on every resume so "today" stays correct past midnight or after a time-zone change.
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshToday()
        onPauseOrDispose { }
    }

    DashboardScreen(
        dateState = dateState,
        uiState = uiState,
        dayLog = dayLog,
        occurrences = occurrences,
        habits = habits,
        goals = goals,
        categories = categories,
        dayLogData = dayLogData,
        hasRealQuests = viewModel.hasRealQuests,
        hasRealHabits = viewModel.hasRealHabits,
        hasRealGoals = viewModel.hasRealGoals,
        onToggleQuest = viewModel::toggleQuest,
        onToggleHabit = viewModel::toggleHabit,
        onIncrementHabitNumeric = viewModel::incrementHabitNumeric,
        onSetHabitNumeric = viewModel::setHabitNumeric,
        onLogHabitTimer = viewModel::logHabitTimer,
        onSetHabitTimer = viewModel::setHabitTimer,
        onToggleChecklistItem = viewModel::toggleChecklistItem,
        onToggleQuestPreview = viewModel::toggleQuestPreview,
        onDateSelected = viewModel::selectDate,
        onSelectToday = viewModel::selectToday,
        onShiftWindow = viewModel::shiftWindow,
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
    categories: List<com.vrddhih.app.core.data.local.entity.CategoryEntity> = emptyList(),
    dayLogData: com.vrddhih.app.core.data.quest.DayLog? = null,
    hasRealQuests: Boolean = false,
    hasRealHabits: Boolean = false,
    hasRealGoals: Boolean = false,
    onToggleQuest: (com.vrddhih.app.core.data.quest.QuestOccurrence) -> Unit = {},
    onToggleHabit: (Long) -> Unit = {},
    onIncrementHabitNumeric: (Long, Long) -> Unit = { _, _ -> },
    onSetHabitNumeric: (Long, Long, Boolean?) -> Unit = { _, _, _ -> },
    onLogHabitTimer: (Long, Int) -> Unit = { _, _ -> },
    onSetHabitTimer: (Long, Int) -> Unit = { _, _ -> },
    onToggleChecklistItem: (Long, Long, Boolean) -> Unit = { _, _, _ -> },
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

            // Unified Daily Activity / Tracking Area
            item(key = "unified-activities") {
                UnifiedDailyActivitiesPanel(
                    date = dateState.selectedDate,
                    isToday = dateState.isTodaySelected,
                    habits = habits,
                    occurrences = occurrences,
                    goals = goals,
                    categories = categories,
                    onToggleQuest = onToggleQuest,
                    onToggleHabit = onToggleHabit,
                    onIncrementHabitNumeric = onIncrementHabitNumeric,
                    onSetHabitNumeric = onSetHabitNumeric,
                    onLogHabitTimer = onLogHabitTimer,
                    onSetHabitTimer = onSetHabitTimer,
                    onToggleChecklistItem = onToggleChecklistItem,
                    hasRealQuests = hasRealQuests,
                    hasRealHabits = hasRealHabits,
                    hasRealGoals = hasRealGoals,
                    uiState = uiState,
                    onToggleQuestPreview = onToggleQuestPreview,
                )
            }


            val completedActivities = (if (hasRealQuests) occurrences.count { it.isCompleted } else uiState.completedQuestCount) +
                (if (hasRealHabits) habits.count { it.isDone } else uiState.completedHabitCount)
            val totalActivities = (if (hasRealQuests) occurrences.size else uiState.quests.size) +
                (if (hasRealHabits) habits.size else uiState.habits.size)

            item(key = "footer") {
                DynamicDashboardFooter(
                    completedCount = completedActivities,
                    totalCount = totalActivities,
                )
            }
        }
    }
}

@Composable
fun DynamicDashboardFooter(
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    val messages = listOf(
        stringResource(R.string.dashboard_footer),
        stringResource(R.string.dashboard_footer_offline),
        stringResource(R.string.dashboard_footer_private),
        stringResource(R.string.dashboard_footer_sovereignty),
        if (totalCount > 0) {
            stringResource(R.string.dashboard_footer_progress, completedCount)
        } else {
            stringResource(R.string.dashboard_footer_offline)
        },
    )

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(messages) {
        while (true) {
            delay(5000L)
            currentIndex = (currentIndex + 1) % messages.size
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { currentIndex = (currentIndex + 1) % messages.size },
            )
            .padding(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .alpha(dotAlpha)
                    .background(VrddhihColors.Electric),
            )
            Spacer(Modifier.width(VrddhihSpacing.sm))
            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400)) { height -> height / 2 })
                        .togetherWith(fadeOut(animationSpec = tween(400)) + slideOutVertically(animationSpec = tween(400)) { height -> -height / 2 })
                },
                label = "dashboardFooterAnimation",
            ) { targetIndex ->
                Text(
                    text = messages[targetIndex % messages.size],
                    style = MaterialTheme.typography.bodySmall,
                    color = VrddhihColors.TextMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
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
