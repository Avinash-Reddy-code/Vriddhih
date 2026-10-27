package com.vrddhih.app.features.habits.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.vrddhih.app.R
import com.vrddhih.app.core.data.local.dao.HabitWithConfigs
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.data.local.entity.HabitCompletionState
import com.vrddhih.app.core.data.local.entity.HabitDailyProgress
import com.vrddhih.app.core.data.local.entity.HabitType
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.CuteCategoryBadge
import com.vrddhih.app.core.ui.components.SystemPanel
import com.vrddhih.app.core.ui.components.SystemProgressBar
import com.vrddhih.app.core.ui.components.SystemTag
import kotlinx.coroutines.delay

@Composable
fun HabitItemCard(
    habitWithConfigs: HabitWithConfigs,
    progress: HabitDailyProgress?,
    category: CategoryEntity?,
    checklistProgress: Map<Long, Boolean>,
    isScheduledToday: Boolean,
    onComplete: (Long) -> Unit,
    onUndo: (Long) -> Unit,
    onUpdateNumeric: (Long, Long) -> Unit,
    onLogTimerSeconds: (Long, Int) -> Unit,
    onUpdateLimit: (Long, Long) -> Unit,
    onToggleChecklistItem: (Long, Long, Boolean) -> Unit,
    onEdit: (HabitWithConfigs) -> Unit,
    modifier: Modifier = Modifier,
) {
    val habit = habitWithConfigs.habit
    val isCompleted = progress?.completionState == HabitCompletionState.COMPLETED
    var isExpanded by remember { mutableStateOf(false) }

    SystemPanel(
        glow = isCompleted,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
        ) {
            // Header Row: Category Badge + Title & Tags + Edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
            ) {
                CuteCategoryBadge(
                    category = category,
                    size = 40.dp,
                    iconSize = 22.dp,
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = VrddhihColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                    ) {
                        SystemTag(
                            text = habit.type.name.replace("_", " "),
                            color = VrddhihColors.Ice,
                        )
                        if (!isScheduledToday) {
                            SystemTag(
                                text = stringResource(R.string.habits_status_not_scheduled),
                                color = VrddhihColors.TextMuted,
                            )
                        }
                        SystemTag(
                            text = "+${habit.xpPerCompletion} XP",
                            color = VrddhihColors.Electric,
                        )
                    }
                }

                IconButton(
                    onClick = { onEdit(habitWithConfigs) },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Habit",
                        tint = VrddhihColors.TextMuted,
                    )
                }
            }

            if (habit.description.isNotBlank()) {
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = VrddhihColors.TextSecondary,
                )
            }

            // Interactive controls per habit type
            when (habit.type) {
                HabitType.YES_NO -> {
                    YesNoControls(
                        isCompleted = isCompleted,
                        onComplete = { onComplete(habit.id) },
                        onUndo = { onUndo(habit.id) },
                    )
                }
                HabitType.NUMERIC -> {
                    val target = habitWithConfigs.numericConfig?.targetValue ?: 1L
                    val unit = habitWithConfigs.numericConfig?.unit.orEmpty()
                    val current = progress?.numericValue ?: 0L
                    val step = habitWithConfigs.numericConfig?.incrementValue ?: 1L

                    NumericControls(
                        current = current,
                        target = target,
                        unit = unit,
                        step = step,
                        isCompleted = isCompleted,
                        onUpdate = { newCount -> onUpdateNumeric(habit.id, newCount) },
                    )
                }
                HabitType.TIMER -> {
                    val dailyTarget = habitWithConfigs.timerConfig?.dailyTargetSeconds ?: (15 * 60)
                    val loggedSeconds = progress?.timerSeconds ?: 0

                    TimerControls(
                        loggedSeconds = loggedSeconds,
                        targetSeconds = dailyTarget,
                        isCompleted = isCompleted,
                        onLogSeconds = { seconds -> onLogTimerSeconds(habit.id, seconds) },
                    )
                }
                HabitType.CHECKLIST -> {
                    val items = habitWithConfigs.checklistItems

                    ChecklistControls(
                        items = items,
                        checklistProgress = checklistProgress,
                        isCompleted = isCompleted,
                        isExpanded = isExpanded,
                        onToggleExpand = { isExpanded = !isExpanded },
                        onToggleItem = { itemId, completed -> onToggleChecklistItem(habit.id, itemId, completed) },
                    )
                }
                HabitType.LIMIT -> {
                    val limit = habitWithConfigs.limitConfig?.dailyLimit ?: 1L
                    val unit = habitWithConfigs.limitConfig?.unit.orEmpty()
                    val current = progress?.numericValue ?: 0L

                    LimitControls(
                        current = current,
                        limit = limit,
                        unit = unit,
                        onUpdate = { newCount -> onUpdateLimit(habit.id, newCount) },
                    )
                }
            }
        }
    }
}

@Composable
private fun YesNoControls(
    isCompleted: Boolean,
    onComplete: () -> Unit,
    onUndo: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VrddhihRadii.sm))
            .background(if (isCompleted) VrddhihColors.Electric.copy(alpha = 0.12f) else VrddhihColors.PanelRaised)
            .clickable { if (isCompleted) onUndo() else onComplete() }
            .padding(horizontal = VrddhihSpacing.md, vertical = VrddhihSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (isCompleted) stringResource(R.string.habit_state_done).uppercase() else stringResource(R.string.habit_state_pending).uppercase(),
            style = VrddhihTextStyles.TagLabel,
            color = if (isCompleted) VrddhihColors.Electric else VrddhihColors.TextSecondary,
            fontWeight = FontWeight.Bold,
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) {
                        Brush.horizontalGradient(listOf(VrddhihColors.Electric, VrddhihColors.Ice))
                    } else {
                        Brush.horizontalGradient(listOf(VrddhihColors.Void, VrddhihColors.Void))
                    },
                )
                .border(
                    width = 1.dp,
                    color = if (isCompleted) Color.Transparent else VrddhihColors.Outline,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = if (isCompleted) VrddhihColors.Void else VrddhihColors.TextMuted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun NumericControls(
    current: Long,
    target: Long,
    unit: String,
    step: Long,
    isCompleted: Boolean,
    onUpdate: (Long) -> Unit,
) {
    val progress = (current.toFloat() / maxOf(1L, target).toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$current",
                    style = VrddhihTextStyles.NumericLarge,
                    color = if (isCompleted) VrddhihColors.Electric else VrddhihColors.TextPrimary,
                )
                Text(
                    text = " / $target $unit",
                    style = VrddhihTextStyles.NumericSmall,
                    color = VrddhihColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
            ) {
                IconButton(
                    onClick = { if (current > 0) onUpdate(maxOf(0L, current - step)) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(VrddhihRadii.xs))
                        .background(VrddhihColors.PanelRaised),
                ) {
                    Icon(Icons.Outlined.Remove, contentDescription = "Decrease", tint = VrddhihColors.Ice)
                }

                IconButton(
                    onClick = { onUpdate(current + step) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(VrddhihRadii.xs))
                        .background(VrddhihColors.PanelRaised),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Increase", tint = VrddhihColors.Electric)
                }
            }
        }

        SystemProgressBar(progress = progress, height = 8.dp)
    }
}

@Composable
private fun TimerControls(
    loggedSeconds: Int,
    targetSeconds: Int,
    isCompleted: Boolean,
    onLogSeconds: (Int) -> Unit,
) {
    var isTimerRunning by remember { mutableStateOf(false) }
    var elapsedSinceStart by remember { mutableIntStateOf(0) }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (true) {
                delay(1000)
                elapsedSinceStart++
            }
        }
    }

    val totalDisplaySeconds = loggedSeconds + elapsedSinceStart
    val progress = (totalDisplaySeconds.toFloat() / maxOf(1, targetSeconds).toFloat()).coerceIn(0f, 1f)

    val currentFormatted = formatTime(totalDisplaySeconds)
    val targetFormatted = formatTime(targetSeconds)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = currentFormatted,
                    style = VrddhihTextStyles.NumericLarge,
                    color = if (isCompleted) VrddhihColors.Electric else VrddhihColors.TextPrimary,
                )
                Text(
                    text = " / $targetFormatted",
                    style = VrddhihTextStyles.NumericSmall,
                    color = VrddhihColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
            ) {
                Button(
                    onClick = {
                        if (isTimerRunning) {
                            // Stop & persist accumulated seconds
                            onLogSeconds(elapsedSinceStart)
                            elapsedSinceStart = 0
                            isTimerRunning = false
                        } else {
                            isTimerRunning = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTimerRunning) VrddhihColors.Warning else VrddhihColors.Electric,
                        contentColor = VrddhihColors.Void,
                    ),
                    shape = RoundedCornerShape(VrddhihRadii.xs),
                    contentPadding = PaddingValues(horizontal = VrddhihSpacing.sm, vertical = VrddhihSpacing.xxs),
                ) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(if (isTimerRunning) R.string.habits_timer_pause else R.string.habits_timer_start).uppercase(),
                        style = VrddhihTextStyles.TagLabel,
                    )
                }

                // Quick add buttons
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(VrddhihRadii.xs))
                        .background(VrddhihColors.PanelRaised)
                        .clickable { onLogSeconds(5 * 60) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Text(stringResource(R.string.habits_timer_quick_5m), style = VrddhihTextStyles.TagLabel, color = VrddhihColors.Ice)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(VrddhihRadii.xs))
                        .background(VrddhihColors.PanelRaised)
                        .clickable { onLogSeconds(15 * 60) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Text(stringResource(R.string.habits_timer_quick_15m), style = VrddhihTextStyles.TagLabel, color = VrddhihColors.Ice)
                }
            }
        }

        SystemProgressBar(progress = progress, height = 8.dp)
    }
}

@Composable
private fun ChecklistControls(
    items: List<com.vrddhih.app.core.data.local.entity.HabitChecklistItem>,
    checklistProgress: Map<Long, Boolean>,
    isCompleted: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleItem: (Long, Boolean) -> Unit,
) {
    val completedCount = items.count { checklistProgress[it.id] == true }
    val totalCount = items.size
    val progress = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(VrddhihRadii.xs))
                .clickable { onToggleExpand() }
                .padding(vertical = VrddhihSpacing.xxs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$completedCount / $totalCount completed",
                style = VrddhihTextStyles.NumericSmall,
                color = if (isCompleted) VrddhihColors.Electric else VrddhihColors.TextPrimary,
            )
            Icon(
                imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = VrddhihColors.TextMuted,
            )
        }

        SystemProgressBar(progress = progress, height = 6.dp)

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = VrddhihSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
            ) {
                items.forEach { item ->
                    val checked = checklistProgress[item.id] == true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(VrddhihRadii.xs))
                            .background(VrddhihColors.PanelRaised)
                            .toggleable(
                                value = checked,
                                role = Role.Checkbox,
                                onValueChange = { onToggleItem(item.id, it) },
                            )
                            .padding(horizontal = VrddhihSpacing.sm, vertical = VrddhihSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (checked) VrddhihColors.Electric else VrddhihColors.Void)
                                .border(1.dp, if (checked) Color.Transparent else VrddhihColors.Outline, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (checked) {
                                Icon(Icons.Outlined.Check, contentDescription = null, tint = VrddhihColors.Void, modifier = Modifier.size(16.dp))
                            }
                        }

                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (checked) VrddhihColors.TextMuted else VrddhihColors.TextPrimary,
                            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LimitControls(
    current: Long,
    limit: Long,
    unit: String,
    onUpdate: (Long) -> Unit,
) {
    val isWithinLimit = current <= limit
    val statusColor = if (isWithinLimit) VrddhihColors.Success else VrddhihColors.Danger
    val statusText = stringResource(if (isWithinLimit) R.string.habits_status_within_limit else R.string.habits_status_limit_exceeded)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$current",
                    style = VrddhihTextStyles.NumericLarge,
                    color = statusColor,
                )
                Text(
                    text = " / $limit $unit",
                    style = VrddhihTextStyles.NumericSmall,
                    color = VrddhihColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp),
                )
            }
            Text(
                text = statusText.uppercase(),
                style = VrddhihTextStyles.TagLabel,
                color = statusColor,
                fontWeight = FontWeight.Bold,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
        ) {
            IconButton(
                onClick = { if (current > 0) onUpdate(current - 1) },
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(VrddhihRadii.xs))
                    .background(VrddhihColors.PanelRaised),
            ) {
                Icon(Icons.Outlined.Remove, contentDescription = "Decrease", tint = VrddhihColors.Ice)
            }

            IconButton(
                onClick = { onUpdate(current + 1) },
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(VrddhihRadii.xs))
                    .background(VrddhihColors.PanelRaised),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Increase", tint = VrddhihColors.Electric)
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = (seconds / 60)
    val s = (seconds % 60)
    return String.format("%02d:%02d", m, s)
}
