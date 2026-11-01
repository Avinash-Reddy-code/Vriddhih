package com.vrddhih.app.features.habits.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrddhih.app.core.data.local.dao.HabitWithConfigs
import com.vrddhih.app.core.data.local.entity.HabitType
import com.vrddhih.app.core.data.local.entity.NumericDirection
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.CuteCategoryBadge
import com.vrddhih.app.core.ui.components.SystemPanel
import com.vrddhih.app.core.ui.components.SystemTag
import com.vrddhih.app.features.habits.HabitItemDisplay
import com.vrddhih.app.features.habits.HabitWeeklyDayStatus
import java.time.format.TextStyle
import java.util.Locale

/**
 * Habit item card for the Habits Screen.
 *
 * Requirements:
 * 1. Habit name shown only once on the left; category icon on the RIGHT in alignment with name.
 *    View Statistics removed; Edit pencil icon and 3-dots menu moved to the bottom right.
 * 2. Frequency shown below habit name.
 * 3. Habit type NOT shown on the card.
 * 4. Dates of the week shown clearly along with day labels, with current date at the end.
 * 5. NO habit completion controls (completion is exclusively on Home Dashboard).
 */
@Composable
fun HabitItemCard(
    display: HabitItemDisplay,
    onEdit: (HabitWithConfigs) -> Unit,
    onDelete: (HabitWithConfigs) -> Unit,
    onShowStatistics: ((HabitItemDisplay) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val habitWithConfigs = display.habitWithConfigs
    val habit = habitWithConfigs.habit
    val category = display.category

    val resolvedIconKey = category?.iconKey ?: habit.iconKey ?: "star"
    val resolvedColorArgb = category?.colorArgb ?: habit.colorArgb ?: VrddhihColors.Cyan.toArgb()
    val categoryColor = Color(resolvedColorArgb)
    var showMenu by remember { mutableStateOf(false) }

    SystemPanel(
        accent = categoryColor.copy(alpha = 0.4f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
        ) {
            // ==========================================
            // 1. HEADER ROW: NAME ON LEFT, CATEGORY ICON ON RIGHT
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Habit Name (Left)
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = VrddhihColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                Spacer(Modifier.width(VrddhihSpacing.sm))

                // Category Icon on the RIGHT
                CuteCategoryBadge(
                    iconKey = resolvedIconKey,
                    colorArgb = resolvedColorArgb,
                    size = 36.dp,
                    iconSize = 19.dp,
                    contentDescription = habit.name,
                )
            }

            // Clean Notes if any
            if (display.cleanNotes.isNotBlank()) {
                Text(
                    text = display.cleanNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = VrddhihColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // ==========================================
            // 2. PROGRESS ACCORDING TO HABIT TYPE
            // ==========================================
            HabitTypeProgressSection(display = display)

            // Extra Goals (if configured)
            if (display.extraGoals.isNotEmpty()) {
                ExtraGoalsPillRow(extraGoals = display.extraGoals, habitType = habit.type)
            }

            // ==========================================
            // 3. WEEKLY PROGRESS WITH DAYS & DATES
            // ==========================================
            WeeklyProgressRow(display = display)

            // ==========================================
            // 4. FOOTER: STREAK (LEFT) + EDIT & 3-DOTS (RIGHT)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left: Streak with flame icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalFireDepartment,
                        contentDescription = null,
                        tint = VrddhihColors.Warning,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = "${display.currentStreak} day streak",
                        style = MaterialTheme.typography.labelSmall,
                        color = VrddhihColors.Warning,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // Right: Edit pencil icon and 3 dots overflow menu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    IconButton(
                        onClick = { onEdit(habitWithConfigs) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Habit",
                            tint = VrddhihColors.TextSecondary,
                            modifier = Modifier.size(17.dp),
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "More Options",
                                tint = VrddhihColors.TextSecondary,
                                modifier = Modifier.size(17.dp),
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .background(VrddhihColors.PanelRaised)
                                .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(VrddhihRadii.xs)),
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Delete Habit",
                                        color = VrddhihColors.Danger,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = VrddhihColors.Danger,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete(habitWithConfigs)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders type-specific progress indicators clearly distinguishing today's value from the configured target.
 * Note: Habit type is deliberately omitted from UI per user requirements.
 */
@Composable
private fun HabitTypeProgressSection(display: HabitItemDisplay) {
    val habitWithConfigs = display.habitWithConfigs
    val habit = habitWithConfigs.habit
    val todayProgress = display.todayProgress

    when (habit.type) {
        HabitType.YES_NO -> {
            val isDone = todayProgress?.completedAt != null
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Today's value: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = VrddhihColors.TextSecondary,
                    )
                    Text(
                        text = if (isDone) "Completed" else if (display.isScheduledToday) "Pending" else "Not scheduled",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDone) VrddhihColors.NeonGreen else if (display.isScheduledToday) VrddhihColors.Ice else VrddhihColors.TextMuted,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = "Target: 1 completion",
                    style = MaterialTheme.typography.labelSmall,
                    color = VrddhihColors.TextMuted,
                )
            }
        }

        HabitType.NUMERIC -> {
            val config = habitWithConfigs.numericConfig
            val target = config?.targetValue ?: 1L
            val unit = config?.unit.orEmpty()
            val recorded = todayProgress?.numericValue ?: 0L
            val direction = config?.direction ?: NumericDirection.AT_LEAST

            val targetConditionText = when (direction) {
                NumericDirection.AT_LEAST -> "Target: At least $target $unit"
                NumericDirection.AT_MOST -> "Target: At most $target $unit"
                NumericDirection.EXACTLY -> "Target: Exactly $target $unit"
                NumericDirection.ANY_VALUE -> "Target: Any value"
            }.trim()

            val isDone = when (direction) {
                NumericDirection.AT_LEAST -> recorded >= target
                NumericDirection.AT_MOST -> recorded in 1..target
                NumericDirection.EXACTLY -> recorded == target
                NumericDirection.ANY_VALUE -> recorded > 0
            }

            val progressFraction = if (target > 0) (recorded.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false),
                    ) {
                        Text(
                            text = "Today's value: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = VrddhihColors.TextSecondary,
                        )
                        Text(
                            text = "$recorded / $target $unit".trim(),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDone) VrddhihColors.NeonGreen else VrddhihColors.Ice,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = targetConditionText,
                        style = MaterialTheme.typography.labelSmall,
                        color = VrddhihColors.TextMuted,
                    )
                }

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (isDone) VrddhihColors.NeonGreen else VrddhihColors.Electric,
                    trackColor = VrddhihColors.PanelRaised,
                )
            }
        }

        HabitType.TIMER -> {
            val config = habitWithConfigs.timerConfig
            val targetSec = config?.targetDurationSeconds ?: 0
            val recordedSec = todayProgress?.timerSeconds ?: 0
            val isDone = targetSec > 0 && recordedSec >= targetSec
            val progressFraction = if (targetSec > 0) (recordedSec.toFloat() / targetSec.toFloat()).coerceIn(0f, 1f) else 0f

            val targetFormatted = formatSecondsToDuration(targetSec)
            val recordedFormatted = formatSecondsToDuration(recordedSec)

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false),
                    ) {
                        Text(
                            text = "Today's value: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = VrddhihColors.TextSecondary,
                        )
                        Text(
                            text = "$recordedFormatted / $targetFormatted",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDone) VrddhihColors.NeonGreen else VrddhihColors.Ice,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = "Target: At least $targetFormatted",
                        style = MaterialTheme.typography.labelSmall,
                        color = VrddhihColors.TextMuted,
                    )
                }

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (isDone) VrddhihColors.NeonGreen else VrddhihColors.Electric,
                    trackColor = VrddhihColors.PanelRaised,
                )
            }
        }

        HabitType.CHECKLIST -> {
            val items = habitWithConfigs.checklistItems
            val completedCount = items.count { display.checklistItemProgress[it.id] == true }
            val isDone = items.isNotEmpty() && completedCount == items.size
            val progressFraction = if (items.isNotEmpty()) (completedCount.toFloat() / items.size.toFloat()).coerceIn(0f, 1f) else 0f

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false),
                    ) {
                        Text(
                            text = "Today's value: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = VrddhihColors.TextSecondary,
                        )
                        Text(
                            text = "$completedCount / ${items.size} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDone) VrddhihColors.NeonGreen else VrddhihColors.Ice,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = "Target: All ${items.size} items",
                        style = MaterialTheme.typography.labelSmall,
                        color = VrddhihColors.TextMuted,
                    )
                }

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (isDone) VrddhihColors.NeonGreen else VrddhihColors.Electric,
                    trackColor = VrddhihColors.PanelRaised,
                )
            }
        }

        else -> Unit
    }
}

/**
 * Separate row for extra goals.
 */
@Composable
private fun ExtraGoalsPillRow(
    extraGoals: List<com.vrddhih.app.core.data.habit.HabitExtraGoalDraft>,
    habitType: HabitType,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Flag,
            contentDescription = null,
            tint = VrddhihColors.Ice,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = "Extra Goals:",
            style = VrddhihTextStyles.TagLabel,
            color = VrddhihColors.TextMuted,
        )
        extraGoals.take(2).forEach { goal ->
            val targetStr = when (habitType) {
                HabitType.NUMERIC -> "${goal.targetValue}"
                HabitType.TIMER -> "${goal.durationMinutes}m"
                HabitType.CHECKLIST -> "${goal.targetValue} items"
                else -> ""
            }
            SystemTag(
                text = "${goal.name}: $targetStr".trim(),
                color = VrddhihColors.Ice,
            )
        }
        if (extraGoals.size > 2) {
            SystemTag(
                text = "+${extraGoals.size - 2}",
                color = VrddhihColors.TextMuted,
            )
        }
    }
}

/**
 * Weekly progress row displaying day of week labels, calendar dates, and status indicators,
 * with the current date positioned at the end.
 */
@Composable
private fun WeeklyProgressRow(display: HabitItemDisplay) {
    // 7 compact day indicators showing Day Label and Date inside a circular rectangle
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        display.weeklyDays.forEach { dayStatus ->
            DayStatusIndicator(dayStatus = dayStatus)
        }
    }
}

/**
 * Individual day column showing day abbreviation and the calendar date inside a circular rectangle,
 * color-coded by state: Finished, In Progress, Pending, Unfinished, or Unscheduled/Future.
 */
@Composable
private fun DayStatusIndicator(dayStatus: HabitWeeklyDayStatus) {
    val dayLabel = if (dayStatus.isToday) "TODAY" else dayStatus.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3).uppercase()

    val (bgColor, borderColor, textColor) = when {
        // Finished
        dayStatus.isCompleted -> Triple(
            VrddhihColors.NeonGreen.copy(alpha = 0.20f),
            VrddhihColors.NeonGreen,
            VrddhihColors.NeonGreen,
        )
        // In Progress
        dayStatus.isInProgress -> Triple(
            VrddhihColors.Cyan.copy(alpha = 0.18f),
            VrddhihColors.Cyan,
            VrddhihColors.Cyan,
        )
        // Pending (scheduled today awaiting completion)
        dayStatus.isToday && dayStatus.isScheduled -> Triple(
            VrddhihColors.Amber.copy(alpha = 0.18f),
            VrddhihColors.Amber,
            VrddhihColors.Amber,
        )
        // Unfinished (past scheduled day missed)
        !dayStatus.isFuture && !dayStatus.isToday && dayStatus.isScheduled -> Triple(
            VrddhihColors.Danger.copy(alpha = 0.18f),
            VrddhihColors.Danger,
            VrddhihColors.Danger,
        )
        // Future or Unscheduled
        else -> Triple(
            VrddhihColors.Panel,
            VrddhihColors.Outline,
            VrddhihColors.TextMuted,
        )
    }

    val shape = RoundedCornerShape(8.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // 1. Day of Week label (e.g. MON, TUE, ... TODAY)
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = if (dayStatus.isToday) 8.sp else 9.sp),
            color = if (dayStatus.isToday) VrddhihColors.Electric else VrddhihColors.TextSecondary,
            fontWeight = if (dayStatus.isToday) FontWeight.Bold else FontWeight.Normal,
        )

        // 2. Date in circular rectangle with semantic status coloring
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(shape)
                .background(bgColor)
                .border(1.dp, borderColor, shape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = dayStatus.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = textColor,
                fontWeight = if (dayStatus.isCompleted || dayStatus.isInProgress || dayStatus.isToday) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}

private fun formatSecondsToDuration(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return when {
        h > 0 -> "${h}h ${m}m"
        m > 0 -> "${m}m"
        else -> "${s}s"
    }
}
