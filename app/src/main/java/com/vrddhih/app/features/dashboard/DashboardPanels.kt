package com.vrddhih.app.features.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.vrddhih.app.core.data.local.entity.HabitType
import com.vrddhih.app.core.data.local.entity.NumericDirection
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihBrushes
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.CuteCategoryBadge
import com.vrddhih.app.core.ui.components.MockDataBadge
import com.vrddhih.app.core.ui.components.ProgressRing
import com.vrddhih.app.core.ui.components.SectionHeader
import com.vrddhih.app.core.ui.components.SystemPanel
import com.vrddhih.app.core.ui.components.SystemProgressBar
import com.vrddhih.app.core.ui.components.SystemTag

// region Player status

@Composable
internal fun PlayerStatusPanel(player: PlayerStatus, modifier: Modifier = Modifier) {
    SystemPanel(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LevelEmblem(level = player.level)
            Spacer(Modifier.width(VrddhihSpacing.lg))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dashboard_player_label).uppercase(),
                    style = VrddhihTextStyles.SystemLabel,
                    color = VrddhihColors.TextMuted,
                )
                Spacer(Modifier.height(VrddhihSpacing.xxs))
                Text(text = player.title, style = MaterialTheme.typography.titleLarge, color = VrddhihColors.TextPrimary)
                Text(text = player.rank, style = MaterialTheme.typography.labelLarge, color = VrddhihColors.Ice)
            }
            MockDataBadge()
        }
        Spacer(Modifier.height(VrddhihSpacing.xl))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.dashboard_xp_label).uppercase(),
                style = VrddhihTextStyles.SystemLabel,
                color = VrddhihColors.TextMuted,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.dashboard_xp_value, player.currentXp, player.xpForNextLevel),
                style = VrddhihTextStyles.NumericSmall,
                color = VrddhihColors.TextPrimary,
            )
        }
        Spacer(Modifier.height(VrddhihSpacing.sm))
        SystemProgressBar(progress = player.levelProgress, height = 10.dp)
        Spacer(Modifier.height(VrddhihSpacing.sm))
        Text(
            text = stringResource(R.string.dashboard_xp_to_next, player.xpToNextLevel, player.level + 1),
            style = MaterialTheme.typography.bodySmall,
            color = VrddhihColors.TextSecondary,
        )
    }
}

@Composable
private fun LevelEmblem(level: Int, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(VrddhihRadii.md)
    val description = stringResource(R.string.dashboard_level_content_description, level)
    Column(
        modifier = modifier
            .size(68.dp)
            .clip(shape)
            .background(VrddhihBrushes.Emblem)
            .border(VrddhihDimens.BorderWidth, VrddhihBrushes.Energy, shape)
            .clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = stringResource(R.string.dashboard_level_short), style = VrddhihTextStyles.SystemLabel, color = VrddhihColors.Ice)
        Text(text = level.toString(), style = VrddhihTextStyles.NumericLarge, color = VrddhihColors.Frost)
    }
}

// endregion

// region Daily summary

@Composable
internal fun DailySummaryPanel(
    completedCount: Int,
    remainingCount: Int,
    xpEarned: Int,
    isSample: Boolean = false,
    modifier: Modifier = Modifier,
) {
    SystemPanel(modifier = modifier) {
        SectionHeader(
            title = stringResource(R.string.dashboard_summary_title),
            trailing = if (isSample) { { MockDataBadge() } } else null,
        )
        Spacer(Modifier.height(VrddhihSpacing.lg))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryStat(
                value = completedCount.toString(),
                label = stringResource(R.string.dashboard_summary_completed),
                color = VrddhihColors.Success,
                modifier = Modifier.weight(1f),
            )
            SummaryDivider()
            SummaryStat(
                value = remainingCount.toString(),
                label = stringResource(R.string.dashboard_summary_remaining),
                color = VrddhihColors.Warning,
                modifier = Modifier.weight(1f),
            )
            SummaryDivider()
            SummaryStat(
                value = stringResource(R.string.dashboard_quest_xp, xpEarned),
                label = stringResource(R.string.dashboard_summary_xp),
                color = VrddhihColors.Ice,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun DailySummaryPanel(uiState: DashboardUiState, modifier: Modifier = Modifier) =
    DailySummaryPanel(
        completedCount = uiState.completedQuestCount,
        remainingCount = uiState.remainingQuestCount,
        xpEarned = uiState.previewXpEarned,
        isSample = true,
        modifier = modifier,
    )

@Composable
private fun SummaryStat(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = value, style = VrddhihTextStyles.NumericMedium, color = color, maxLines = 1)
        Spacer(Modifier.height(VrddhihSpacing.xs))
        Text(
            text = label.uppercase(),
            style = VrddhihTextStyles.TagLabel,
            color = VrddhihColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SummaryDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = VrddhihSpacing.xs)
            .width(VrddhihDimens.BorderWidth)
            .background(VrddhihColors.Outline),
    )
}

// endregion

// region Unified Daily Activities

enum class ActivityFilter { ALL, HABITS, QUESTS, GOALS }

@Composable
internal fun UnifiedDailyActivitiesPanel(
    date: java.time.LocalDate,
    isToday: Boolean,
    habits: List<DashboardHabitItem>,
    occurrences: List<com.vrddhih.app.core.data.quest.QuestOccurrence>,
    goals: List<DashboardGoalItem>,
    onToggleQuest: (com.vrddhih.app.core.data.quest.QuestOccurrence) -> Unit,
    onToggleHabit: (Long) -> Unit,
    onIncrementHabitNumeric: (Long, Long) -> Unit,
    onSetHabitNumeric: (Long, Long, Boolean?) -> Unit,
    onLogHabitTimer: (Long, Int) -> Unit,
    onSetHabitTimer: (Long, Int) -> Unit = { _, _ -> },
    onToggleChecklistItem: (Long, Long, Boolean) -> Unit,
    hasRealQuests: Boolean,
    hasRealHabits: Boolean,
    hasRealGoals: Boolean,
    uiState: DashboardUiState,
    onToggleQuestPreview: (String) -> Unit,
    categories: List<com.vrddhih.app.core.data.local.entity.CategoryEntity> = emptyList(),
    modifier: Modifier = Modifier,
) {
    var selectedFilter by remember { mutableStateOf(ActivityFilter.ALL) }
    var numericDialogHabitId by remember { mutableStateOf<Long?>(null) }
    var timerDialogHabitId by remember { mutableStateOf<Long?>(null) }
    var checklistDialogHabitId by remember { mutableStateOf<Long?>(null) }

    val activeNumericHabit = habits.find { it.id == numericDialogHabitId }
    val activeTimerHabit = habits.find { it.id == timerDialogHabitId }
    val activeChecklistHabit = habits.find { it.id == checklistDialogHabitId }

    val totalHabits = if (hasRealHabits) habits.size else uiState.habits.size
    val totalQuests = if (hasRealQuests) occurrences.size else uiState.quests.size
    val totalGoals = if (hasRealGoals) goals.size else uiState.goals.size
    val totalAll = totalHabits + totalQuests + totalGoals

    val formatter = remember { java.time.format.DateTimeFormatter.ofPattern("MMM d", java.util.Locale.getDefault()) }
    val title = if (isToday) {
        stringResource(R.string.dashboard_activities_title)
    } else {
        stringResource(R.string.dashboard_activities_title) + " · " + formatter.format(date)
    }

    SystemPanel(modifier = modifier) {
        SectionHeader(
            title = title,
            trailing = {
                if (!hasRealHabits && !hasRealQuests && !hasRealGoals) {
                    MockDataBadge()
                } else {
                    SystemTag(text = stringResource(R.string.quests_actionable_badge), color = VrddhihColors.Electric)
                }
            },
        )
        Spacer(Modifier.height(VrddhihSpacing.md))

        // Filter chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
        ) {
            ActivityFilterChip(
                label = stringResource(R.string.dashboard_activities_filter_all),
                count = totalAll,
                isSelected = selectedFilter == ActivityFilter.ALL,
                onClick = { selectedFilter = ActivityFilter.ALL },
                modifier = Modifier.weight(1f),
            )
            ActivityFilterChip(
                label = stringResource(R.string.dashboard_activities_filter_habits),
                count = totalHabits,
                isSelected = selectedFilter == ActivityFilter.HABITS,
                onClick = { selectedFilter = ActivityFilter.HABITS },
                modifier = Modifier.weight(1f),
            )
            ActivityFilterChip(
                label = stringResource(R.string.dashboard_activities_filter_quests),
                count = totalQuests,
                isSelected = selectedFilter == ActivityFilter.QUESTS,
                onClick = { selectedFilter = ActivityFilter.QUESTS },
                modifier = Modifier.weight(1f),
            )
            ActivityFilterChip(
                label = stringResource(R.string.dashboard_activities_filter_goals),
                count = totalGoals,
                isSelected = selectedFilter == ActivityFilter.GOALS,
                onClick = { selectedFilter = ActivityFilter.GOALS },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(VrddhihSpacing.md))

        val showHabits = selectedFilter == ActivityFilter.ALL || selectedFilter == ActivityFilter.HABITS
        val showQuests = selectedFilter == ActivityFilter.ALL || selectedFilter == ActivityFilter.QUESTS
        val showGoals = selectedFilter == ActivityFilter.ALL || selectedFilter == ActivityFilter.GOALS

        val hasAnyVisibleItems = when (selectedFilter) {
            ActivityFilter.ALL -> totalAll > 0
            ActivityFilter.HABITS -> totalHabits > 0
            ActivityFilter.QUESTS -> totalQuests > 0
            ActivityFilter.GOALS -> totalGoals > 0
        }

        if (!hasAnyVisibleItems) {
            Text(
                text = stringResource(R.string.dashboard_activities_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = VrddhihColors.TextMuted,
                modifier = Modifier.padding(vertical = VrddhihSpacing.md),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
            ) {
                // Real Habits
                if (showHabits) {
                    if (hasRealHabits) {
                        val sortedHabits = remember(habits) { habits.sortedBy { it.isDone } }
                        sortedHabits.forEach { habit ->
                            DashboardHabitRow(
                                habit = habit,
                                onTap = {
                                    when (habit.type) {
                                        HabitType.YES_NO,
                                        HabitType.LIMIT -> {
                                            onToggleHabit(habit.id)
                                        }
                                        HabitType.NUMERIC -> {
                                            numericDialogHabitId = habit.id
                                        }
                                        HabitType.TIMER -> {
                                            timerDialogHabitId = habit.id
                                        }
                                        HabitType.CHECKLIST -> {
                                            checklistDialogHabitId = habit.id
                                        }
                                    }
                                },
                            )
                        }
                    } else {
                        val sortedPreviewHabits = remember(uiState.habits) { uiState.habits.sortedBy { it.isDoneToday } }
                        sortedPreviewHabits.forEach { habit -> HabitLine(habit = habit) }
                    }
                }

                // Quests
                if (showQuests) {
                    if (hasRealQuests) {
                        occurrences.forEach { occurrence ->
                            val category = categories.find { it.id == occurrence.quest.categoryId }
                            RealQuestRow(
                                occurrence = occurrence,
                                category = category,
                                onToggle = { onToggleQuest(occurrence) },
                            )
                        }
                    } else {
                        uiState.quests.forEach { quest ->
                            QuestRow(
                                quest = quest,
                                onToggle = { onToggleQuestPreview(quest.id) },
                            )
                        }
                    }
                }

                // Goals
                if (showGoals) {
                    if (hasRealGoals) {
                        goals.forEach { goal ->
                            RealGoalRow(goal = goal)
                        }
                    } else {
                        uiState.goals.forEach { goal ->
                            GoalRow(goal = goal)
                        }
                    }
                }
            }
        }
    }

    // Numeric Log Dialog
    activeNumericHabit?.let { habit ->
        NumericProgressDialog(
            habit = habit,
            onSave = { value, isCompleted ->
                onSetHabitNumeric(habit.id, value, isCompleted)
            },
            onDismiss = { numericDialogHabitId = null },
        )
    }

    // Timer Log Dialog
    activeTimerHabit?.let { habit ->
        TimerTrackingDialog(
            habit = habit,
            onSaveDuration = { totalSecs ->
                onSetHabitTimer(habit.id, totalSecs)
            },
            onDismiss = { timerDialogHabitId = null },
        )
    }

    // Checklist Dialog
    activeChecklistHabit?.let { habit ->
        ChecklistTrackingDialog(
            habit = habit,
            onToggleItem = { itemId, completed ->
                onToggleChecklistItem(habit.id, itemId, completed)
            },
            onDismiss = { checklistDialogHabitId = null },
        )
    }
}

@Composable
private fun ActivityFilterChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(VrddhihRadii.sm)
    val bg = if (isSelected) VrddhihColors.Electric.copy(alpha = 0.16f) else VrddhihColors.PanelRaised
    val border = if (isSelected) VrddhihColors.Electric else VrddhihColors.Outline
    val textColor = if (isSelected) VrddhihColors.Electric else VrddhihColors.TextMuted

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(shape)
            .background(bg)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = VrddhihSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$label ($count)",
            style = VrddhihTextStyles.TagLabel,
            color = textColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun DashboardHabitRow(
    habit: DashboardHabitItem,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val habitColor = habit.colorArgb?.let { Color(it) } ?: VrddhihColors.Cyan
    val subtitle = when (habit.type) {
        HabitType.YES_NO, HabitType.LIMIT -> null
        HabitType.NUMERIC -> {
            val condText = when (habit.numericDirection) {
                NumericDirection.AT_LEAST -> "≥ ${habit.numericTarget}"
                NumericDirection.AT_MOST -> "< ${habit.numericTarget}"
                NumericDirection.EXACTLY -> "= ${habit.numericTarget}"
                NumericDirection.ANY_VALUE -> "Any"
            }
            val unitStr = if (habit.numericUnit.isNotBlank()) " ${habit.numericUnit}" else ""
            "${habit.numericCurrent} / $condText$unitStr"
        }
        HabitType.TIMER -> {
            val targetMins = habit.timerTargetSeconds / 60
            val elapsedMins = habit.timerElapsedSeconds / 60
            if (targetMins > 0) "${elapsedMins}m / ${targetMins}m" else "${elapsedMins}m"
        }
        HabitType.CHECKLIST -> {
            val doneCount = habit.checklistItems.count { it.isCompleted }
            val totalCount = habit.checklistItems.size
            "$doneCount / $totalCount items"
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(VrddhihRadii.sm))
            .clickable(onClick = onTap)
            .padding(horizontal = VrddhihSpacing.xs, vertical = VrddhihSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HabitCategoryIconBadge(iconKey = habit.iconKey, color = habitColor)
        Spacer(Modifier.width(VrddhihSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (habit.isDone) VrddhihColors.TextMuted else VrddhihColors.TextPrimary,
                textDecoration = if (habit.isDone) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(VrddhihSpacing.xxs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SystemTag(
                    text = when (habit.type) {
                        HabitType.NUMERIC -> "Numeric"
                        HabitType.TIMER -> "Timer"
                        HabitType.CHECKLIST -> "Checklist"
                        else -> "Habit"
                    },
                    color = habitColor,
                )
                if (subtitle != null) {
                    Spacer(Modifier.width(VrddhihSpacing.xs))
                    Text(
                        text = "•  $subtitle",
                        style = VrddhihTextStyles.NumericSmall,
                        color = if (habit.isDone) VrddhihColors.Success else VrddhihColors.Ice,
                    )
                }
            }
        }
        Spacer(Modifier.width(VrddhihSpacing.sm))
        QuestCheck(checked = habit.isDone)
    }
}

@Composable
private fun NumericProgressDialog(
    habit: DashboardHabitItem,
    onSave: (Long, Boolean?) -> Unit,
    onDismiss: () -> Unit,
) {
    var inputVal by remember(habit.id, habit.numericCurrent) {
        mutableStateOf(habit.numericCurrent.toString())
    }
    var markCompleted by remember(habit.id, habit.isDone) {
        mutableStateOf(habit.isDone)
    }

    val goalText = when (habit.numericDirection) {
        NumericDirection.AT_LEAST ->
            stringResource(R.string.dashboard_habit_goal_at_least, habit.numericTarget, habit.numericUnit)
        NumericDirection.AT_MOST ->
            stringResource(R.string.dashboard_habit_goal_less_than, habit.numericTarget, habit.numericUnit)
        NumericDirection.EXACTLY ->
            stringResource(R.string.dashboard_habit_goal_exactly, habit.numericTarget, habit.numericUnit)
        NumericDirection.ANY_VALUE ->
            stringResource(R.string.dashboard_habit_goal_any)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VrddhihColors.PanelRaised,
        shape = RoundedCornerShape(VrddhihRadii.md),
        title = {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium,
                color = VrddhihColors.TextPrimary,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm)) {
                // Configured goal & evaluation condition
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(VrddhihRadii.xs))
                        .background(VrddhihColors.Void)
                        .padding(horizontal = VrddhihSpacing.sm, vertical = VrddhihSpacing.xs),
                ) {
                    Text(
                        text = goalText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VrddhihColors.Ice,
                    )
                }

                // Current recorded value
                Text(
                    text = stringResource(
                        R.string.dashboard_habit_current_progress,
                        habit.numericCurrent,
                        habit.numericUnit,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = VrddhihColors.TextSecondary,
                )

                // Input field
                OutlinedTextField(
                    value = inputVal,
                    onValueChange = { inputVal = it.filter { ch -> ch.isDigit() } },
                    label = { Text(stringResource(R.string.dashboard_habit_enter_value)) },
                    suffix = if (habit.numericUnit.isNotBlank()) {
                        { Text(habit.numericUnit, color = VrddhihColors.TextMuted) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // For ANY_VALUE: explicit completion checkbox
                if (habit.numericDirection == NumericDirection.ANY_VALUE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(VrddhihRadii.xs))
                            .clickable { markCompleted = !markCompleted }
                            .padding(vertical = VrddhihSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        QuestCheck(checked = markCompleted)
                        Spacer(Modifier.width(VrddhihSpacing.sm))
                        Text(
                            text = stringResource(R.string.dashboard_habit_mark_completed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = VrddhihColors.TextPrimary,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val num = inputVal.toLongOrNull() ?: 0L
                    val isComp = if (habit.numericDirection == NumericDirection.ANY_VALUE) markCompleted else null
                    onSave(num, isComp)
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.action_save), color = VrddhihColors.Electric)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.habits_cancel_action), color = VrddhihColors.TextMuted)
            }
        },
    )
}

@Composable
private fun TimerTrackingDialog(
    habit: DashboardHabitItem,
    onSaveDuration: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var timerRunning by remember { mutableStateOf(false) }
    var sessionSeconds by remember { mutableStateOf(0) }
    var manualMinutesInput by remember { mutableStateOf("") }

    LaunchedEffect(timerRunning) {
        while (isActive && timerRunning) {
            delay(1000L)
            sessionSeconds++
        }
    }

    val targetMins = habit.timerTargetSeconds / 60
    val recordedMins = habit.timerElapsedSeconds / 60
    val totalSeconds = habit.timerElapsedSeconds + sessionSeconds
    val formattedSessionTime = remember(sessionSeconds) {
        val m = sessionSeconds / 60
        val s = sessionSeconds % 60
        String.format(java.util.Locale.getDefault(), "%02d:%02d", m, s)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VrddhihColors.PanelRaised,
        shape = RoundedCornerShape(VrddhihRadii.md),
        title = {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium,
                color = VrddhihColors.TextPrimary,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Target and today's progress banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(VrddhihRadii.xs))
                        .background(VrddhihColors.Void)
                        .padding(horizontal = VrddhihSpacing.sm, vertical = VrddhihSpacing.xs),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xxs)) {
                        if (targetMins > 0) {
                            Text(
                                text = stringResource(R.string.dashboard_timer_target_label, "${targetMins}m"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = VrddhihColors.Ice,
                            )
                        }
                        Text(
                            text = stringResource(R.string.dashboard_timer_recorded_today, "${recordedMins}m"),
                            style = MaterialTheme.typography.bodySmall,
                            color = VrddhihColors.TextSecondary,
                        )
                    }
                }

                Spacer(Modifier.height(VrddhihSpacing.xs))

                // Digital Timer Readout
                Text(
                    text = formattedSessionTime,
                    style = VrddhihTextStyles.NumericLarge,
                    color = if (timerRunning) VrddhihColors.Electric else VrddhihColors.TextPrimary,
                )
                Text(
                    text = stringResource(R.string.dashboard_timer_session_elapsed),
                    style = MaterialTheme.typography.bodySmall,
                    color = VrddhihColors.TextMuted,
                )

                Spacer(Modifier.height(VrddhihSpacing.xs))

                // Controls: Start, Pause, Resume, Finish
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                ) {
                    if (!timerRunning && sessionSeconds == 0) {
                        Button(
                            onClick = { timerRunning = true },
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VrddhihColors.Electric),
                            shape = RoundedCornerShape(VrddhihRadii.sm),
                        ) {
                            Text(stringResource(R.string.dashboard_timer_start), color = VrddhihColors.Void)
                        }
                    } else if (timerRunning) {
                        Button(
                            onClick = { timerRunning = false },
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VrddhihColors.Ice),
                            shape = RoundedCornerShape(VrddhihRadii.sm),
                        ) {
                            Text(stringResource(R.string.dashboard_timer_pause), color = VrddhihColors.Void)
                        }
                        Button(
                            onClick = {
                                timerRunning = false
                                onSaveDuration(totalSeconds)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VrddhihColors.Success),
                            shape = RoundedCornerShape(VrddhihRadii.sm),
                        ) {
                            Text(stringResource(R.string.dashboard_timer_finish), color = VrddhihColors.TextPrimary)
                        }
                    } else {
                        Button(
                            onClick = { timerRunning = true },
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VrddhihColors.Electric),
                            shape = RoundedCornerShape(VrddhihRadii.sm),
                        ) {
                            Text(stringResource(R.string.dashboard_timer_resume), color = VrddhihColors.Void)
                        }
                        Button(
                            onClick = {
                                onSaveDuration(totalSeconds)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VrddhihColors.Success),
                            shape = RoundedCornerShape(VrddhihRadii.sm),
                        ) {
                            Text(stringResource(R.string.dashboard_timer_finish), color = VrddhihColors.TextPrimary)
                        }
                    }
                }

                if (!timerRunning && sessionSeconds == 0) {
                    Spacer(Modifier.height(VrddhihSpacing.xs))
                    OutlinedTextField(
                        value = manualMinutesInput,
                        onValueChange = { manualMinutesInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text(stringResource(R.string.dashboard_timer_manual_entry)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            if (!timerRunning && sessionSeconds == 0 && manualMinutesInput.isNotBlank()) {
                TextButton(
                    onClick = {
                        val enteredMins = manualMinutesInput.toIntOrNull() ?: 0
                        if (enteredMins > 0) {
                            onSaveDuration(habit.timerElapsedSeconds + (enteredMins * 60))
                        }
                        onDismiss()
                    },
                ) {
                    Text(stringResource(R.string.action_save), color = VrddhihColors.Electric)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.habits_cancel_action), color = VrddhihColors.TextMuted)
            }
        },
    )
}

@Composable
private fun ChecklistTrackingDialog(
    habit: DashboardHabitItem,
    onToggleItem: (Long, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val completedCount = habit.checklistItems.count { it.isCompleted }
    val totalCount = habit.checklistItems.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val allDone = totalCount > 0 && completedCount == totalCount

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VrddhihColors.PanelRaised,
        shape = RoundedCornerShape(VrddhihRadii.md),
        title = {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium,
                color = VrddhihColors.TextPrimary,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
            ) {
                // Progress summary banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(VrddhihRadii.xs))
                        .background(VrddhihColors.Void)
                        .padding(horizontal = VrddhihSpacing.sm, vertical = VrddhihSpacing.xs),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xxs)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.dashboard_checklist_progress_detail, completedCount, totalCount),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (allDone) VrddhihColors.Success else VrddhihColors.Ice,
                            )
                            if (allDone) {
                                SystemTag(text = stringResource(R.string.dashboard_checklist_all_done), color = VrddhihColors.Success)
                            }
                        }
                        Spacer(Modifier.height(VrddhihSpacing.xxs))
                        SystemProgressBar(progress = progress, height = 4.dp)
                    }
                }

                Spacer(Modifier.height(VrddhihSpacing.xxs))

                // Checklist items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xxs),
                ) {
                    items(habit.checklistItems, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(VrddhihRadii.xs))
                                .clickable { onToggleItem(item.id, !item.isCompleted) }
                                .padding(horizontal = VrddhihSpacing.xs, vertical = VrddhihSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            QuestCheck(checked = item.isCompleted)
                            Spacer(Modifier.width(VrddhihSpacing.sm))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (item.isCompleted) VrddhihColors.TextMuted else VrddhihColors.TextPrimary,
                                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_done), color = VrddhihColors.Electric)
            }
        },
    )
}

@Composable
private fun HabitCategoryIconBadge(iconKey: String?, color: Color) {
    CuteCategoryBadge(
        iconKey = iconKey,
        colorArgb = color.toArgb(),
        size = 36.dp,
        iconSize = 18.dp,
    )
}

// endregion

// region Today's quests

@Composable
internal fun RealQuestsPanel(
    date: java.time.LocalDate,
    isToday: Boolean,
    occurrences: List<com.vrddhih.app.core.data.quest.QuestOccurrence>,
    onToggleQuest: (com.vrddhih.app.core.data.quest.QuestOccurrence) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = remember { java.time.format.DateTimeFormatter.ofPattern("MMM d", java.util.Locale.getDefault()) }
    val title = if (isToday) {
        stringResource(R.string.dashboard_quests_title)
    } else {
        stringResource(R.string.dashboard_quests_title_date, formatter.format(date))
    }

    SystemPanel(modifier = modifier) {
        SectionHeader(
            title = title,
            trailing = {
                SystemTag(text = stringResource(R.string.quests_actionable_badge), color = VrddhihColors.Electric)
            },
        )
        Spacer(Modifier.height(VrddhihSpacing.sm))
        if (occurrences.isEmpty()) {
            Text(
                text = stringResource(R.string.dashboard_quests_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = VrddhihColors.TextMuted,
                modifier = Modifier.padding(vertical = VrddhihSpacing.sm),
            )
        } else {
            occurrences.forEachIndexed { index, occurrence ->
                if (index > 0) HorizontalDivider(color = VrddhihColors.Outline)
                RealQuestRow(
                    occurrence = occurrence,
                    onToggle = { onToggleQuest(occurrence) },
                )
            }
        }
    }
}

@Composable
private fun RealQuestRow(
    occurrence: com.vrddhih.app.core.data.quest.QuestOccurrence,
    category: com.vrddhih.app.core.data.local.entity.CategoryEntity? = null,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormatter = remember { java.time.format.DateTimeFormatter.ofPattern("HH:mm", java.util.Locale.getDefault()) }
    val catColor = category?.let { Color(it.colorArgb) } ?: VrddhihColors.Cyan
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(VrddhihRadii.sm))
            .clickable(onClick = onToggle)
            .padding(horizontal = VrddhihSpacing.xs, vertical = VrddhihSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CuteCategoryBadge(
            category = category,
            size = 36.dp,
            iconSize = 18.dp,
        )
        Spacer(Modifier.width(VrddhihSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = occurrence.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (occurrence.isCompleted) VrddhihColors.TextMuted else VrddhihColors.TextPrimary,
                textDecoration = if (occurrence.isCompleted) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(VrddhihSpacing.xxs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SystemTag(text = category?.name ?: "Quest", color = catColor)
                Spacer(Modifier.width(VrddhihSpacing.xs))
                com.vrddhih.app.features.quests.components.TaskPriorityTag(priority = occurrence.priority)
                if (occurrence.dueTime != null) {
                    Spacer(Modifier.width(VrddhihSpacing.xs))
                    SystemTag(text = timeFormatter.format(occurrence.dueTime), color = VrddhihColors.Ice)
                }
                if (occurrence.quest.xpReward > 0) {
                    Spacer(Modifier.width(VrddhihSpacing.xs))
                    Text(
                        text = "+${occurrence.quest.xpReward} XP",
                        style = MaterialTheme.typography.labelMedium,
                        color = VrddhihColors.Ice,
                    )
                }
            }
        }
        Spacer(Modifier.width(VrddhihSpacing.sm))
        QuestCheck(checked = occurrence.isCompleted)
    }
}

@Composable
internal fun TodaysQuestsPanel(
    quests: List<QuestPreview>,
    onToggleQuest: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SystemPanel(modifier = modifier) {
        SectionHeader(title = stringResource(R.string.dashboard_quests_title), trailing = { MockDataBadge() })
        Spacer(Modifier.height(VrddhihSpacing.xs))
        Text(
            text = stringResource(R.string.dashboard_quests_preview_note),
            style = MaterialTheme.typography.bodySmall,
            color = VrddhihColors.TextMuted,
        )
        Spacer(Modifier.height(VrddhihSpacing.sm))
        quests.forEachIndexed { index, quest ->
            if (index > 0) HorizontalDivider(color = VrddhihColors.Outline)
            QuestRow(quest = quest, onToggle = { onToggleQuest(quest.id) })
        }
    }
}

@Composable
private fun QuestRow(quest: QuestPreview, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(VrddhihRadii.sm))
            .clickable(onClick = onToggle)
            .padding(horizontal = VrddhihSpacing.xs, vertical = VrddhihSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CuteCategoryBadge(
            category = null,
            size = 36.dp,
            iconSize = 18.dp,
        )
        Spacer(Modifier.width(VrddhihSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = quest.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (quest.isCompleted) VrddhihColors.TextMuted else VrddhihColors.TextPrimary,
                textDecoration = if (quest.isCompleted) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(VrddhihSpacing.xxs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PriorityTag(priority = quest.priority)
                Spacer(Modifier.width(VrddhihSpacing.sm))
                Text(
                    text = stringResource(R.string.dashboard_quest_xp, quest.xpReward),
                    style = MaterialTheme.typography.labelMedium,
                    color = VrddhihColors.Ice,
                )
            }
        }
        Spacer(Modifier.width(VrddhihSpacing.sm))
        QuestCheck(checked = quest.isCompleted)
    }
}

@Composable
private fun QuestCheck(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = CircleShape
    val fill by animateColorAsState(
        targetValue = if (checked) VrddhihColors.Electric else Color.Transparent,
        label = "questCheckFill",
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) VrddhihColors.Ice else VrddhihColors.OutlineBright,
        label = "questCheckBorder",
    )
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(shape)
            .background(fill)
            .border(1.5.dp, borderColor, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = VrddhihColors.Void,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun PriorityTag(priority: QuestPriority) {
    val (labelRes, color) = when (priority) {
        QuestPriority.HIGH -> R.string.priority_high to VrddhihColors.Warning
        QuestPriority.MEDIUM -> R.string.priority_medium to VrddhihColors.Electric
        QuestPriority.LOW -> R.string.priority_low to VrddhihColors.TextSecondary
    }
    SystemTag(text = stringResource(labelRes), color = color)
}

// endregion

// region Habits

@Composable
internal fun RealHabitProgressPanel(
    date: java.time.LocalDate,
    isToday: Boolean,
    habits: List<DashboardHabitItem>,
    onToggleHabit: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val completedCount = habits.count { it.isDone }
    val totalCount = habits.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    SystemPanel(modifier = modifier) {
        SectionHeader(
            title = stringResource(R.string.dashboard_habits_title),
            trailing = {
                SystemTag(text = stringResource(R.string.quests_actionable_badge), color = VrddhihColors.Ice)
            },
        )
        Spacer(Modifier.height(VrddhihSpacing.lg))
        if (habits.isEmpty()) {
            Text(
                text = stringResource(R.string.dashboard_habits_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = VrddhihColors.TextMuted,
                modifier = Modifier.padding(vertical = VrddhihSpacing.sm),
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(progress = progress, diameter = 96.dp) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.dashboard_habits_ratio, completedCount, totalCount),
                            style = VrddhihTextStyles.NumericMedium,
                            color = VrddhihColors.Frost,
                        )
                        Text(
                            text = (if (isToday) stringResource(R.string.dashboard_habits_today) else date.format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))).uppercase(),
                            style = VrddhihTextStyles.TagLabel,
                            color = VrddhihColors.TextMuted,
                        )
                    }
                }
                Spacer(Modifier.width(VrddhihSpacing.xl))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                ) {
                    habits.forEach { habit ->
                        RealHabitLine(
                            habit = habit,
                            onToggle = { onToggleHabit(habit.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RealHabitLine(
    habit: DashboardHabitItem,
    onToggle: () -> Unit,
) {
    val state = stringResource(if (habit.isDone) R.string.habit_state_done else R.string.habit_state_pending)
    val habitColor = habit.colorArgb?.let { Color(it) } ?: VrddhihColors.Cyan

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(VrddhihRadii.sm))
            .toggleable(
                value = habit.isDone,
                role = Role.Checkbox,
                onValueChange = { onToggle() },
            )
            .padding(horizontal = VrddhihSpacing.xs, vertical = VrddhihSpacing.xxs)
            .semantics(mergeDescendants = true) { stateDescription = state },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (habit.iconKey != null) {
            CuteCategoryBadge(
                iconKey = habit.iconKey,
                colorArgb = habitColor.toArgb(),
                size = 24.dp,
                iconSize = 13.dp,
            )
            Spacer(Modifier.width(VrddhihSpacing.xs))
        }
        Text(
            text = habit.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (habit.isDone) VrddhihColors.TextPrimary else VrddhihColors.TextSecondary,
            textDecoration = if (habit.isDone) TextDecoration.LineThrough else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(VrddhihSpacing.sm))
        QuestCheck(checked = habit.isDone)
    }
}

@Composable
internal fun HabitProgressPanel(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    SystemPanel(modifier = modifier) {
        SectionHeader(title = stringResource(R.string.dashboard_habits_title), trailing = { MockDataBadge() })
        Spacer(Modifier.height(VrddhihSpacing.lg))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(progress = uiState.habitProgress, diameter = 96.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.dashboard_habits_ratio, uiState.completedHabitCount, uiState.habits.size),
                        style = VrddhihTextStyles.NumericMedium,
                        color = VrddhihColors.Frost,
                    )
                    Text(
                        text = stringResource(R.string.dashboard_habits_today).uppercase(),
                        style = VrddhihTextStyles.TagLabel,
                        color = VrddhihColors.TextMuted,
                    )
                }
            }
            Spacer(Modifier.width(VrddhihSpacing.xl))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
            ) {
                uiState.habits.forEach { habit -> HabitLine(habit = habit) }
            }
        }
        Spacer(Modifier.height(VrddhihSpacing.md))
        Text(
            text = stringResource(R.string.dashboard_habits_streak_note),
            style = MaterialTheme.typography.bodySmall,
            color = VrddhihColors.TextMuted,
        )
    }
}

@Composable
private fun HabitLine(habit: HabitPreview) {
    val state = stringResource(if (habit.isDoneToday) R.string.habit_state_done else R.string.habit_state_pending)
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) { stateDescription = state },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = habit.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (habit.isDoneToday) VrddhihColors.TextPrimary else VrddhihColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(VrddhihSpacing.sm))
        QuestCheck(checked = habit.isDoneToday)
    }
}

// endregion

// region Goals

@Composable
internal fun RealGoalsPanel(
    goals: List<DashboardGoalItem>,
    modifier: Modifier = Modifier,
) {
    SystemPanel(modifier = modifier) {
        SectionHeader(
            title = stringResource(R.string.dashboard_goals_title),
            trailing = {
                SystemTag(text = "Active", color = VrddhihColors.Electric)
            },
        )
        Spacer(Modifier.height(VrddhihSpacing.md))
        if (goals.isEmpty()) {
            Text(
                text = stringResource(R.string.dashboard_goals_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = VrddhihColors.TextMuted,
                modifier = Modifier.padding(vertical = VrddhihSpacing.sm),
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.lg)) {
                goals.forEach { goal -> RealGoalRow(goal = goal) }
            }
        }
    }
}

@Composable
private fun RealGoalRow(goal: DashboardGoalItem) {
    val goalColor = goal.colorArgb?.let { Color(it) } ?: VrddhihColors.Cyan
    Column(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CuteCategoryBadge(
                iconKey = goal.iconKey,
                colorArgb = goal.colorArgb,
                size = 36.dp,
                iconSize = 18.dp,
            )
            Spacer(Modifier.width(VrddhihSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.category.uppercase(),
                    style = VrddhihTextStyles.TagLabel,
                    color = goalColor,
                )
                Spacer(Modifier.height(VrddhihSpacing.xxs))
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = VrddhihColors.TextPrimary,
                )
            }
            Spacer(Modifier.width(VrddhihSpacing.sm))
            Text(
                text = stringResource(R.string.dashboard_goal_percent, goal.percent),
                style = VrddhihTextStyles.NumericSmall,
                color = VrddhihColors.Ice,
            )
        }
        Spacer(Modifier.height(VrddhihSpacing.sm))
        SystemProgressBar(progress = goal.progress, height = 6.dp)
    }
}

@Composable
internal fun GoalsPanel(goals: List<GoalPreview>, modifier: Modifier = Modifier) {
    SystemPanel(modifier = modifier) {
        SectionHeader(title = stringResource(R.string.dashboard_goals_title), trailing = { MockDataBadge() })
        Spacer(Modifier.height(VrddhihSpacing.md))
        Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.lg)) {
            goals.forEach { goal -> GoalRow(goal = goal) }
        }
    }
}

@Composable
private fun GoalRow(goal: GoalPreview) {
    Column(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CuteCategoryBadge(
                category = null,
                size = 36.dp,
                iconSize = 18.dp,
            )
            Spacer(Modifier.width(VrddhihSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = goal.category.uppercase(), style = VrddhihTextStyles.TagLabel, color = VrddhihColors.TextMuted)
                Spacer(Modifier.height(VrddhihSpacing.xxs))
                Text(text = goal.title, style = MaterialTheme.typography.titleSmall, color = VrddhihColors.TextPrimary)
            }
            Spacer(Modifier.width(VrddhihSpacing.sm))
            Text(
                text = stringResource(R.string.dashboard_goal_percent, goal.percent),
                style = VrddhihTextStyles.NumericSmall,
                color = VrddhihColors.Ice,
            )
        }
        Spacer(Modifier.height(VrddhihSpacing.sm))
        SystemProgressBar(progress = goal.progress, height = 6.dp)
    }
}

// endregion

// region Deadlines

@Composable
internal fun DeadlinesPanel(deadlines: List<DeadlinePreview>, modifier: Modifier = Modifier) {
    SystemPanel(modifier = modifier) {
        SectionHeader(title = stringResource(R.string.dashboard_deadlines_title), trailing = { MockDataBadge() })
        Spacer(Modifier.height(VrddhihSpacing.md))
        Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md)) {
            deadlines.forEach { deadline -> DeadlineRow(deadline = deadline) }
        }
    }
}

@Composable
private fun DeadlineRow(deadline: DeadlinePreview) {
    val color = when (deadline.urgency) {
        DeadlineUrgency.CRITICAL -> VrddhihColors.Danger
        DeadlineUrgency.SOON -> VrddhihColors.Warning
        DeadlineUrgency.SCHEDULED -> VrddhihColors.Ice
    }
    val dueLabel = when (deadline.daysRemaining) {
        0 -> stringResource(R.string.deadline_due_today)
        1 -> stringResource(R.string.deadline_due_tomorrow)
        else -> pluralStringResource(R.plurals.deadline_due_in_days, deadline.daysRemaining, deadline.daysRemaining)
    }
    val chipShape = RoundedCornerShape(VrddhihRadii.md)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(chipShape)
                .background(color.copy(alpha = 0.12f))
                .border(VrddhihDimens.BorderWidth, color.copy(alpha = 0.5f), chipShape)
                .clearAndSetSemantics {},
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.deadline_countdown, deadline.daysRemaining),
                style = VrddhihTextStyles.NumericSmall,
                color = color,
            )
        }
        Spacer(Modifier.width(VrddhihSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = deadline.title, style = MaterialTheme.typography.titleSmall, color = VrddhihColors.TextPrimary)
            Spacer(Modifier.height(VrddhihSpacing.xxs))
            Text(text = dueLabel, style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}

// endregion
