package com.vrddhih.app.features.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import com.vrddhih.app.core.data.local.entity.HabitType
import com.vrddhih.app.core.data.local.entity.NumericDirection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
    onIncrementHabitNumeric: (Long, Long) -> Unit = { _, _ -> },
    onSetHabitNumeric: (Long, Long, Boolean?) -> Unit = { _, _, _ -> },
    onLogHabitTimer: (Long, Int) -> Unit = { _, _ -> },
    onSetHabitTimer: (Long, Int, Boolean?) -> Unit = { _, _, _ -> },
    onToggleChecklistItem: (Long, Long, Boolean) -> Unit = { _, _, _ -> },
    hasRealQuests: Boolean,
    hasRealHabits: Boolean,
    hasRealGoals: Boolean,
    uiState: DashboardUiState,
    onToggleQuestPreview: (String) -> Unit,
    categories: List<com.vrddhih.app.core.data.local.entity.CategoryEntity> = emptyList(),
    modifier: Modifier = Modifier,
) {
    var selectedFilter by remember { mutableStateOf(ActivityFilter.ALL) }
    var bannerHabitId by remember { mutableStateOf<Long?>(null) }
    val sortedHabits = remember(habits) { habits.sortedBy { it.isDone } }

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
            Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                // Real Habits
                if (showHabits) {
                    if (hasRealHabits) {
                        sortedHabits.forEach { habit ->
                            DashboardHabitRow(
                                habit = habit,
                                onClick = {
                                    when (habit.type) {
                                        HabitType.YES_NO,
                                        HabitType.LIMIT -> onToggleHabit(habit.id)
                                        HabitType.NUMERIC,
                                        HabitType.TIMER,
                                        HabitType.CHECKLIST -> bannerHabitId = habit.id
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

    // Habit Progress Banner for non-Yes/No habit types
    val activeHabit = habits.find { it.id == bannerHabitId }
    if (activeHabit != null) {
        HabitProgressBannerSheet(
            habit = activeHabit,
            onDismiss = { bannerHabitId = null },
            onSetNumeric = onSetHabitNumeric,
            onSetTimer = onSetHabitTimer,
            onToggleChecklistItem = onToggleChecklistItem,
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val habitColor = habit.colorArgb?.let { Color(it) } ?: VrddhihColors.Cyan
    val progressSubtitle = when (habit.type) {
        HabitType.NUMERIC -> {
            when (habit.numericDirection) {
                NumericDirection.ANY_VALUE -> "${habit.numericCurrent} ${habit.numericUnit}".trim()
                else -> "${habit.numericCurrent}/${habit.numericTarget} ${habit.numericUnit}".trim()
            }
        }
        HabitType.TIMER -> {
            val targetMins = habit.timerTargetSeconds / 60
            val elapsedMins = habit.timerElapsedSeconds / 60
            if (targetMins > 0) "${elapsedMins}m / ${targetMins}m" else "${elapsedMins}m"
        }
        HabitType.CHECKLIST -> {
            val completedCount = habit.checklistItems.count { it.isCompleted }
            val totalCount = habit.checklistItems.size
            "$completedCount/$totalCount"
        }
        HabitType.YES_NO,
        HabitType.LIMIT -> null
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(VrddhihRadii.sm))
            .clickable(onClick = onClick)
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
                SystemTag(text = "Habit", color = habitColor)
                if (!progressSubtitle.isNullOrBlank()) {
                    Spacer(Modifier.width(VrddhihSpacing.xs))
                    Text(
                        text = "•  $progressSubtitle",
                        style = VrddhihTextStyles.NumericSmall,
                        color = if (habit.isDone) VrddhihColors.Success else VrddhihColors.Ice,
                    )
                }
            }
        }
        Spacer(Modifier.width(VrddhihSpacing.sm))
        QuestCheck(
            checked = habit.isDone,
            modifier = Modifier.clickable(onClick = onClick),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitProgressBannerSheet(
    habit: DashboardHabitItem,
    onDismiss: () -> Unit,
    onSetNumeric: (Long, Long, Boolean?) -> Unit,
    onSetTimer: (Long, Int, Boolean?) -> Unit,
    onToggleChecklistItem: (Long, Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val habitColor = habit.colorArgb?.let { Color(it) } ?: VrddhihColors.Cyan

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VrddhihColors.Panel,
        contentColor = VrddhihColors.TextPrimary,
        scrimColor = VrddhihColors.Void.copy(alpha = 0.76f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(VrddhihColors.OutlineBright),
            )
        },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VrddhihSpacing.lg)
                .navigationBarsPadding()
                .padding(bottom = VrddhihSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HabitCategoryIconBadge(iconKey = habit.iconKey, color = habitColor)
                Spacer(Modifier.width(VrddhihSpacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (habit.type) {
                            HabitType.NUMERIC -> "NUMERIC DIRECTIVE"
                            HabitType.TIMER -> "TIMER DIRECTIVE"
                            HabitType.CHECKLIST -> "CHECKLIST DIRECTIVE"
                            else -> stringResource(R.string.dashboard_habit_banner_eyebrow)
                        },
                        style = VrddhihTextStyles.SystemLabel,
                        color = VrddhihColors.Ice,
                    )
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = VrddhihColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(VrddhihSpacing.sm))
                if (habit.isDone) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(VrddhihRadii.xs))
                            .background(VrddhihColors.Success.copy(alpha = 0.15f))
                            .border(1.dp, VrddhihColors.Success, RoundedCornerShape(VrddhihRadii.xs))
                            .padding(horizontal = VrddhihSpacing.xs, vertical = VrddhihSpacing.xxs),
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_habit_completed_status),
                            style = VrddhihTextStyles.TagLabel,
                            color = VrddhihColors.Success,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(VrddhihRadii.xs))
                            .background(VrddhihColors.Electric.copy(alpha = 0.15f))
                            .border(1.dp, VrddhihColors.Electric, RoundedCornerShape(VrddhihRadii.xs))
                            .padding(horizontal = VrddhihSpacing.xs, vertical = VrddhihSpacing.xxs),
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_habit_in_progress_status),
                            style = VrddhihTextStyles.TagLabel,
                            color = VrddhihColors.Electric,
                        )
                    }
                }
            }

            // Body
            when (habit.type) {
                HabitType.NUMERIC -> {
                    NumericProgressSheetContent(
                        habit = habit,
                        onSave = { value, isExplicitCompleted ->
                            onSetNumeric(habit.id, value, isExplicitCompleted)
                            onDismiss()
                        },
                        onCancel = onDismiss,
                    )
                }
                HabitType.TIMER -> {
                    TimerProgressSheetContent(
                        habit = habit,
                        onFinish = { totalSeconds ->
                            onSetTimer(habit.id, totalSeconds, null)
                            onDismiss()
                        },
                        onCancel = onDismiss,
                    )
                }
                HabitType.CHECKLIST -> {
                    ChecklistProgressSheetContent(
                        habit = habit,
                        onToggleItem = { itemId, completed -> onToggleChecklistItem(habit.id, itemId, completed) },
                        onDone = onDismiss,
                    )
                }
                HabitType.YES_NO,
                HabitType.LIMIT -> Unit
            }
        }
    }
}

@Composable
private fun NumericProgressSheetContent(
    habit: DashboardHabitItem,
    onSave: (Long, Boolean?) -> Unit,
    onCancel: () -> Unit,
) {
    val target = habit.numericTarget.coerceAtLeast(0L)
    val current = habit.numericCurrent
    val unit = habit.numericUnit
    var inputVal by remember(habit.id, current) { mutableStateOf(current.toString()) }
    var isAnyValueFinished by remember(habit.id, habit.isDone) { mutableStateOf(habit.isDone) }

    val goalText = when (habit.numericDirection) {
        NumericDirection.AT_LEAST -> stringResource(R.string.dashboard_habit_goal_at_least, target, unit)
        NumericDirection.AT_MOST -> stringResource(R.string.dashboard_habit_goal_at_most, target, unit)
        NumericDirection.EXACTLY -> stringResource(R.string.dashboard_habit_goal_exactly, target, unit)
        NumericDirection.ANY_VALUE -> stringResource(R.string.dashboard_habit_goal_any, unit)
    }

    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md)) {
        // Goal and Condition Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(VrddhihRadii.sm))
                .background(VrddhihColors.PanelRaised)
                .padding(VrddhihSpacing.md),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                Text(
                    text = goalText,
                    style = MaterialTheme.typography.titleMedium,
                    color = VrddhihColors.TextPrimary,
                )
                Text(
                    text = stringResource(R.string.dashboard_habit_current_progress, current, unit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VrddhihColors.TextSecondary,
                )
            }
        }

        // Numeric Progress Entry
        Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
            Text(
                text = stringResource(R.string.dashboard_habit_enter_progress),
                style = MaterialTheme.typography.labelMedium,
                color = VrddhihColors.TextMuted,
            )
            OutlinedTextField(
                value = inputVal,
                onValueChange = { inputVal = it.filter { ch -> ch.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VrddhihColors.Electric,
                    unfocusedBorderColor = VrddhihColors.OutlineBright,
                    focusedTextColor = VrddhihColors.TextPrimary,
                    unfocusedTextColor = VrddhihColors.TextPrimary,
                ),
            )
        }

        // For ANY_VALUE: Explicit confirmation checkbox
        if (habit.numericDirection == NumericDirection.ANY_VALUE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(VrddhihRadii.xs))
                    .clickable { isAnyValueFinished = !isAnyValueFinished }
                    .padding(vertical = VrddhihSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuestCheck(checked = isAnyValueFinished)
                Spacer(Modifier.width(VrddhihSpacing.sm))
                Text(
                    text = stringResource(R.string.dashboard_habit_any_value_complete_checkbox),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VrddhihColors.TextPrimary,
                )
            }
        }

        // Actions: Cancel and Save
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(VrddhihRadii.sm))
                    .background(VrddhihColors.PanelRaised)
                    .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(VrddhihRadii.sm))
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_habit_cancel),
                    style = MaterialTheme.typography.labelLarge,
                    color = VrddhihColors.TextMuted,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(VrddhihRadii.sm))
                    .background(VrddhihColors.Electric)
                    .clickable {
                        val num = inputVal.toLongOrNull() ?: 0L
                        val explicitDone = if (habit.numericDirection == NumericDirection.ANY_VALUE) isAnyValueFinished else null
                        onSave(num, explicitDone)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_habit_save),
                    style = MaterialTheme.typography.labelLarge,
                    color = VrddhihColors.Void,
                )
            }
        }
    }
}

@Composable
private fun TimerProgressSheetContent(
    habit: DashboardHabitItem,
    onFinish: (Int) -> Unit,
    onCancel: () -> Unit,
) {
    val targetSeconds = habit.timerTargetSeconds
    val targetMins = targetSeconds / 60
    val targetDisplay = if (targetMins > 0) "${targetMins}m" else "${targetSeconds}s"

    var isRunning by remember { mutableStateOf(false) }
    var elapsedSeconds by remember(habit.id) { mutableIntStateOf(habit.timerElapsedSeconds) }
    var customMinutesInput by remember { mutableStateOf("") }

    // Live stopwatch ticker
    LaunchedEffect(isRunning) {
        while (isRunning && isActive) {
            delay(1000L)
            elapsedSeconds += 1
        }
    }

    val isTargetSatisfied = targetSeconds > 0 && elapsedSeconds >= targetSeconds
    val progress = if (targetSeconds > 0) (elapsedSeconds.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f) else 1f

    fun formatDuration(totalSecs: Int): String {
        val hrs = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60
        return if (hrs > 0) {
            String.format(java.util.Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md)) {
        // Target display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(VrddhihRadii.sm))
                .background(VrddhihColors.PanelRaised)
                .padding(VrddhihSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_habit_timer_target, targetDisplay),
                    style = MaterialTheme.typography.titleMedium,
                    color = VrddhihColors.TextPrimary,
                )
                if (isTargetSatisfied) {
                    Text(
                        text = stringResource(R.string.dashboard_habit_completed_status),
                        style = VrddhihTextStyles.TagLabel,
                        color = VrddhihColors.Success,
                    )
                }
            }
        }

        // Large Elapsed Time Stopwatch Readout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(VrddhihRadii.md))
                .background(VrddhihColors.Void)
                .border(1.dp, if (isRunning) VrddhihColors.Electric else VrddhihColors.Outline, RoundedCornerShape(VrddhihRadii.md))
                .padding(vertical = VrddhihSpacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatDuration(elapsedSeconds),
                    style = VrddhihTextStyles.NumericLarge,
                    color = if (isTargetSatisfied) VrddhihColors.Success else VrddhihColors.Ice,
                )
                Spacer(Modifier.height(VrddhihSpacing.xs))
                Text(
                    text = when {
                        isRunning -> "TRACKING IN PROGRESS"
                        elapsedSeconds > 0 -> "SESSION PAUSED"
                        else -> "TIMER READY"
                    },
                    style = VrddhihTextStyles.TagLabel,
                    color = if (isRunning) VrddhihColors.Electric else VrddhihColors.TextMuted,
                )
            }
        }

        SystemProgressBar(
            progress = progress,
            height = 8.dp,
            brush = if (isTargetSatisfied) SolidColor(VrddhihColors.Success) else VrddhihBrushes.Energy,
        )

        // Timer Controls: Start / Pause / Resume / Finish
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!isRunning) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(VrddhihRadii.sm))
                        .background(VrddhihColors.Electric.copy(alpha = 0.2f))
                        .border(1.dp, VrddhihColors.Electric, RoundedCornerShape(VrddhihRadii.sm))
                        .clickable { isRunning = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (elapsedSeconds == 0) {
                            stringResource(R.string.dashboard_habit_timer_start)
                        } else {
                            stringResource(R.string.dashboard_habit_timer_resume)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = VrddhihColors.Electric,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(VrddhihRadii.sm))
                        .background(VrddhihColors.PanelRaised)
                        .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(VrddhihRadii.sm))
                        .clickable { isRunning = false },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_habit_timer_pause),
                        style = MaterialTheme.typography.labelLarge,
                        color = VrddhihColors.TextPrimary,
                    )
                }
            }

            // Finish action: saves duration and evaluates completion
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(VrddhihRadii.sm))
                    .background(if (isTargetSatisfied) VrddhihColors.Success else VrddhihColors.Electric)
                    .clickable {
                        isRunning = false
                        onFinish(elapsedSeconds)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_habit_timer_finish),
                    style = MaterialTheme.typography.labelLarge,
                    color = VrddhihColors.Void,
                )
            }
        }

        // Custom duration tracking entry
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = customMinutesInput,
                onValueChange = { customMinutesInput = it.filter { ch -> ch.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.dashboard_habit_timer_custom_duration)) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VrddhihColors.Electric,
                    unfocusedBorderColor = VrddhihColors.OutlineBright,
                    focusedTextColor = VrddhihColors.TextPrimary,
                    unfocusedTextColor = VrddhihColors.TextPrimary,
                ),
            )
            Box(
                modifier = Modifier
                    .height(52.dp)
                    .clip(RoundedCornerShape(VrddhihRadii.sm))
                    .background(VrddhihColors.PanelRaised)
                    .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(VrddhihRadii.sm))
                    .clickable {
                        val mins = customMinutesInput.toIntOrNull() ?: 0
                        if (mins > 0) {
                            elapsedSeconds = mins * 60
                            customMinutesInput = ""
                        }
                    }
                    .padding(horizontal = VrddhihSpacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_habit_timer_apply_custom),
                    style = MaterialTheme.typography.labelLarge,
                    color = VrddhihColors.TextPrimary,
                )
            }
        }

        // Cancel action
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(VrddhihRadii.sm))
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.dashboard_habit_cancel),
                style = MaterialTheme.typography.labelMedium,
                color = VrddhihColors.TextMuted,
            )
        }
    }
}

@Composable
private fun ChecklistProgressSheetContent(
    habit: DashboardHabitItem,
    onToggleItem: (Long, Boolean) -> Unit,
    onDone: () -> Unit,
) {
    val completedCount = habit.checklistItems.count { it.isCompleted }
    val totalCount = habit.checklistItems.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(VrddhihRadii.sm))
                .background(VrddhihColors.PanelRaised)
                .padding(VrddhihSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_habit_checklist_progress, completedCount, totalCount),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (habit.isDone) VrddhihColors.Success else VrddhihColors.Ice,
                )
                if (habit.isDone) {
                    Text(
                        text = stringResource(R.string.dashboard_habit_completed_status),
                        style = VrddhihTextStyles.TagLabel,
                        color = VrddhihColors.Success,
                    )
                }
            }
        }

        SystemProgressBar(
            progress = progress,
            height = 8.dp,
            brush = if (habit.isDone) SolidColor(VrddhihColors.Success) else VrddhihBrushes.Energy,
        )

        // Configured checklist items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(VrddhihRadii.sm))
                .background(VrddhihColors.PanelRaised)
                .padding(VrddhihSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
        ) {
            habit.checklistItems.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Done button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(VrddhihRadii.md))
                .background(VrddhihColors.PanelRaised)
                .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(VrddhihRadii.md))
                .clickable(onClick = onDone),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.dashboard_habit_done_action),
                style = MaterialTheme.typography.labelLarge,
                color = VrddhihColors.TextPrimary,
            )
        }
    }
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
