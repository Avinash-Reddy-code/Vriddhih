package com.vrddhih.app.features.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.vrddhih.app.core.ui.components.MockDataBadge
import com.vrddhih.app.core.ui.components.ProgressRing
import com.vrddhih.app.core.ui.components.SectionHeader
import com.vrddhih.app.core.ui.components.SystemPanel
import com.vrddhih.app.core.ui.components.SystemProgressBar
import com.vrddhih.app.core.ui.components.SystemTag

// region Player status

@Composable
internal fun PlayerStatusPanel(player: PlayerStatus, modifier: Modifier = Modifier) {
    SystemPanel(modifier = modifier, glow = true) {
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
internal fun DailySummaryPanel(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    SystemPanel(modifier = modifier, accent = VrddhihColors.OutlineBright) {
        SectionHeader(title = stringResource(R.string.dashboard_summary_title), trailing = { MockDataBadge() })
        Spacer(Modifier.height(VrddhihSpacing.lg))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryStat(
                value = uiState.completedQuestCount.toString(),
                label = stringResource(R.string.dashboard_summary_completed),
                color = VrddhihColors.Success,
                modifier = Modifier.weight(1f),
            )
            SummaryDivider()
            SummaryStat(
                value = uiState.remainingQuestCount.toString(),
                label = stringResource(R.string.dashboard_summary_remaining),
                color = VrddhihColors.Warning,
                modifier = Modifier.weight(1f),
            )
            SummaryDivider()
            SummaryStat(
                value = stringResource(R.string.dashboard_quest_xp, uiState.previewXpEarned),
                label = stringResource(R.string.dashboard_summary_xp),
                color = VrddhihColors.Ice,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

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

// region Today's quests

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
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(VrddhihRadii.sm))
            .toggleable(value = quest.isCompleted, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = VrddhihSpacing.xs, vertical = VrddhihSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuestCheck(checked = quest.isCompleted)
        Spacer(Modifier.width(VrddhihSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = quest.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (quest.isCompleted) VrddhihColors.TextMuted else VrddhihColors.TextPrimary,
                textDecoration = if (quest.isCompleted) TextDecoration.LineThrough else null,
            )
            Spacer(Modifier.height(VrddhihSpacing.xs))
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
    }
}

@Composable
private fun QuestCheck(checked: Boolean) {
    val shape = RoundedCornerShape(VrddhihRadii.xs)
    val fill by animateColorAsState(
        targetValue = if (checked) VrddhihColors.Electric else Color.Transparent,
        label = "questCheckFill",
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) VrddhihColors.Ice else VrddhihColors.OutlineBright,
        label = "questCheckBorder",
    )
    Box(
        modifier = Modifier
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
        QuestPriority.HIGH -> R.string.priority_high to VrddhihColors.Danger
        QuestPriority.MEDIUM -> R.string.priority_medium to VrddhihColors.Warning
        QuestPriority.LOW -> R.string.priority_low to VrddhihColors.Success
    }
    SystemTag(text = stringResource(labelRes), color = color)
}

// endregion

// region Habits

@Composable
internal fun HabitProgressPanel(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    SystemPanel(modifier = modifier, accent = VrddhihColors.Ice) {
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
        Icon(
            imageVector = if (habit.isDoneToday) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (habit.isDoneToday) VrddhihColors.Success else VrddhihColors.TextMuted,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(VrddhihSpacing.sm))
        Text(
            text = habit.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (habit.isDoneToday) VrddhihColors.TextPrimary else VrddhihColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// endregion

// region Goals

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
        Row(verticalAlignment = Alignment.Bottom) {
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
    SystemPanel(modifier = modifier, accent = VrddhihColors.Warning) {
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
