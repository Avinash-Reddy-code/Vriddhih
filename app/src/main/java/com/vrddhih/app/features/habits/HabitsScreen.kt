package com.vrddhih.app.features.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrddhih.app.R
import com.vrddhih.app.core.data.habit.HabitScheduler
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.AnchoredPrimaryAction
import com.vrddhih.app.core.ui.components.CalendarToggleButton
import com.vrddhih.app.core.ui.components.ScreenHeader
import com.vrddhih.app.core.ui.components.VrddhihCalendarDialog
import com.vrddhih.app.features.habits.components.HabitEditorDialog
import com.vrddhih.app.features.habits.components.HabitItemCard

@Composable
fun HabitsScreen(
    modifier: Modifier = Modifier,
    viewModel: HabitsViewModel = viewModel(factory = HabitsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dateState by viewModel.dateState.collectAsStateWithLifecycle()

    var showCalendar by rememberSaveable { mutableStateOf(false) }

    LifecycleResumeEffect(viewModel) {
        viewModel.refreshToday()
        onPauseOrDispose { }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = VrddhihColors.Void,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = VrddhihDimens.ContentMaxWidth)
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = VrddhihSpacing.lg,
                    end = VrddhihSpacing.lg,
                    top = VrddhihSpacing.xl,
                    bottom = 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.lg),
            ) {
                // Header
                item(key = "header") {
                    ScreenHeader(
                        eyebrow = stringResource(R.string.habits_eyebrow),
                        title = stringResource(R.string.habits_title),
                        subtitle = stringResource(R.string.habits_description),
                        action = { CalendarToggleButton(onClick = { showCalendar = true }) },
                    )
                }

                // Habit Count Bar
                item(key = "action-bar") {
                    Text(
                        text = "${uiState.habitItems.size} HABITS",
                        style = VrddhihTextStyles.SystemLabel,
                        color = VrddhihColors.TextMuted,
                    )
                }

                // Loading Spinner or Habit Items
                if (uiState.isLoading) {
                    item(key = "loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = VrddhihColors.Electric)
                        }
                    }
                } else if (uiState.habitItems.isEmpty()) {
                    item(key = "empty") {
                        EmptyHabitsState()
                    }
                } else {
                    items(
                        items = uiState.habitItems,
                        key = { it.habit.id },
                    ) { habitWithConfigs ->
                        val habit = habitWithConfigs.habit
                        val category = uiState.categories.find { it.id == habit.categoryId }
                        val progress = uiState.progressMap[habit.id]
                        val checklistProgress = uiState.checklistProgressMap[habit.id] ?: emptyMap()
                        val isScheduledToday = HabitScheduler.isScheduledOn(habit, dateState.selectedDate)

                        HabitItemCard(
                            habitWithConfigs = habitWithConfigs,
                            progress = progress,
                            category = category,
                            checklistProgress = checklistProgress,
                            isScheduledToday = isScheduledToday,
                            onComplete = viewModel::completeHabit,
                            onUndo = viewModel::undoCompletion,
                            onUpdateNumeric = viewModel::updateNumericProgress,
                            onLogTimerSeconds = viewModel::logTimerSeconds,
                            onUpdateLimit = viewModel::updateLimitProgress,
                            onToggleChecklistItem = viewModel::toggleChecklistItem,
                            onEdit = viewModel::openEditHabit,
                        )
                    }
                }
            }

            AnchoredPrimaryAction(
                text = stringResource(R.string.habits_new_action),
                onClick = viewModel::openCreateHabit,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    if (showCalendar) {
        VrddhihCalendarDialog(
            selectedDate = dateState.selectedDate,
            today = dateState.today,
            onDateSelected = { date ->
                viewModel.selectDate(date)
                showCalendar = false
            },
            onTodaySelected = {
                viewModel.selectToday()
                showCalendar = false
            },
            onDismiss = { showCalendar = false },
        )
    }

    if (uiState.isEditorOpen) {
        HabitEditorDialog(
            initialHabitWithConfigs = uiState.editingHabit,
            categories = uiState.categories,
            onDismiss = viewModel::closeEditor,
            onSave = viewModel::saveHabit,
            onDelete = viewModel::deleteHabit,
        )
    }
}

@Composable
private fun EmptyHabitsState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = VrddhihSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
    ) {
        Icon(
            imageVector = Icons.Outlined.TrackChanges,
            contentDescription = null,
            tint = VrddhihColors.TextMuted,
            modifier = Modifier.size(56.dp),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.habits_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = VrddhihColors.TextSecondary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.habits_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = VrddhihColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.xxs),
            )
        }
    }
}
