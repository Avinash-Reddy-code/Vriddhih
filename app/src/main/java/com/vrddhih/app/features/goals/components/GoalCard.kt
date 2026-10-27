package com.vrddhih.app.features.goals.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.KeyboardArrowDown
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.vrddhih.app.R
import com.vrddhih.app.core.data.local.dao.GoalWithAllConfigs
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.data.local.entity.GoalStatus
import com.vrddhih.app.core.data.local.entity.GoalType
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.CuteCategoryBadge
import com.vrddhih.app.core.ui.components.SystemPanel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun GoalCard(
    goalWithConfigs: GoalWithAllConfigs,
    categories: List<CategoryEntity>,
    onEdit: () -> Unit,
    onToggleMilestone: (goalId: Long, milestoneId: Long, completed: Boolean) -> Unit,
    onUpdateNumericProgress: (goalId: Long, newValue: Long) -> Unit,
    onUpdateCompletionCount: (goalId: Long, newCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val goal = goalWithConfigs.goal
    val category = categories.find { it.id == goal.categoryId }
    val isCompleted = goal.status == GoalStatus.COMPLETED
    var isExpanded by remember { mutableStateOf(false) }

    // Calculate progress ratio
    val progress = when (goal.type) {
        GoalType.MILESTONE_BASED -> {
            val total = goalWithConfigs.milestones.size
            val completed = goalWithConfigs.milestones.count { it.completedAt != null }
            if (total > 0) completed.toFloat() / total.toFloat() else 0f
        }
        GoalType.NUMERIC_TARGET -> {
            val target = goalWithConfigs.numericTarget
            if (target != null && target.targetValue > 0) {
                target.currentValue.toFloat() / target.targetValue.toFloat()
            } else {
                0f
            }
        }
        GoalType.COMPLETION_BASED -> {
            val target = goalWithConfigs.completionTarget
            if (target != null && target.targetCount > 0) {
                target.currentCount.toFloat() / target.targetCount.toFloat()
            } else {
                0f
            }
        }
    }.coerceIn(0f, 1f)

    SystemPanel(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        glow = isCompleted,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
        ) {
            // Header Row: Category Badge, Title, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
            ) {
                if (category != null) {
                    CuteCategoryBadge(category = category, size = 32.dp, iconSize = 16.dp)
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(VrddhihRadii.xs))
                            .background(VrddhihColors.PanelRaised)
                            .border(1.dp, VrddhihColors.Outline, RoundedCornerShape(VrddhihRadii.xs)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = VrddhihColors.Ice,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isCompleted) VrddhihColors.TextSecondary else VrddhihColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    )
                    if (goal.description.isNotBlank()) {
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = VrddhihColors.TextMuted,
                            maxLines = 2,
                        )
                    }
                }

                // Status Badge
                StatusBadge(status = goal.status)
            }

            // Meta Row: Goal Type, Target Date countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = when (goal.type) {
                        GoalType.MILESTONE_BASED -> stringResource(R.string.goal_type_milestone).uppercase()
                        GoalType.NUMERIC_TARGET -> stringResource(R.string.goal_type_numeric).uppercase()
                        GoalType.COMPLETION_BASED -> stringResource(R.string.goal_type_completion).uppercase()
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = VrddhihColors.Ice,
                    fontFamily = FontFamily.Monospace,
                )

                goal.targetDate?.let { date ->
                    TargetDateBadge(date = date)
                }
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isCompleted) VrddhihColors.Ice else VrddhihColors.Electric,
                trackColor = VrddhihColors.PanelRaised,
            )

            // Progress Readout Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (goal.type) {
                    GoalType.MILESTONE_BASED -> {
                        val total = goalWithConfigs.milestones.size
                        val completed = goalWithConfigs.milestones.count { it.completedAt != null }
                        Text(
                            text = "$completed / $total milestones",
                            style = MaterialTheme.typography.bodySmall,
                            color = VrddhihColors.TextSecondary,
                        )
                    }
                    GoalType.NUMERIC_TARGET -> {
                        val target = goalWithConfigs.numericTarget
                        val current = target?.currentValue ?: 0L
                        val goalVal = target?.targetValue ?: 0L
                        val unit = target?.unit ?: ""
                        Text(
                            text = "$current / $goalVal $unit",
                            style = MaterialTheme.typography.bodySmall,
                            color = VrddhihColors.TextSecondary,
                        )
                    }
                    GoalType.COMPLETION_BASED -> {
                        val target = goalWithConfigs.completionTarget
                        val current = target?.currentCount ?: 0
                        val goalVal = target?.targetCount ?: 0
                        Text(
                            text = "$current / $goalVal completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = VrddhihColors.TextSecondary,
                        )
                    }
                }

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCompleted) VrddhihColors.Ice else VrddhihColors.Electric,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Type-Specific Inline Interactive Controls
            when (goal.type) {
                GoalType.MILESTONE_BASED -> {
                    if (goalWithConfigs.milestones.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(VrddhihRadii.xs))
                                .clickable { isExpanded = !isExpanded }
                                .padding(vertical = VrddhihSpacing.xs),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (isExpanded) "Hide milestones" else "View & check milestones (${goalWithConfigs.milestones.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = VrddhihColors.Ice,
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowDown else Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                contentDescription = null,
                                tint = VrddhihColors.Ice,
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = VrddhihSpacing.xs),
                                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                            ) {
                                goalWithConfigs.milestones.sortedBy { it.position }.forEach { milestone ->
                                    val isMCompleted = milestone.completedAt != null
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(VrddhihRadii.xs))
                                            .background(VrddhihColors.PanelRaised)
                                            .toggleable(
                                                value = isMCompleted,
                                                role = Role.Checkbox,
                                                onValueChange = { checked ->
                                                    onToggleMilestone(goal.id, milestone.id, checked)
                                                },
                                            )
                                            .padding(
                                                horizontal = VrddhihSpacing.sm,
                                                vertical = VrddhihSpacing.sm,
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (isMCompleted) VrddhihColors.Electric else Color.Transparent,
                                                )
                                                .border(
                                                    width = 1.5.dp,
                                                    color = if (isMCompleted) VrddhihColors.Electric else VrddhihColors.Outline,
                                                    shape = RoundedCornerShape(4.dp),
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (isMCompleted) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Check,
                                                    contentDescription = null,
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(14.dp),
                                                )
                                            }
                                        }

                                        Text(
                                            text = milestone.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isMCompleted) VrddhihColors.TextMuted else VrddhihColors.TextPrimary,
                                            textDecoration = if (isMCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                GoalType.NUMERIC_TARGET -> {
                    val target = goalWithConfigs.numericTarget
                    if (target != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = VrddhihSpacing.xs),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StepperButton(
                                text = "−1",
                                onClick = {
                                    val next = (target.currentValue - 1).coerceAtLeast(0)
                                    onUpdateNumericProgress(goal.id, next)
                                },
                            )
                            Spacer(Modifier.width(VrddhihSpacing.sm))
                            StepperButton(
                                text = "+1",
                                onClick = {
                                    val next = target.currentValue + 1
                                    onUpdateNumericProgress(goal.id, next)
                                },
                            )
                            Spacer(Modifier.width(VrddhihSpacing.sm))
                            StepperButton(
                                text = "+5",
                                onClick = {
                                    val next = target.currentValue + 5
                                    onUpdateNumericProgress(goal.id, next)
                                },
                            )
                        }
                    }
                }

                GoalType.COMPLETION_BASED -> {
                    val target = goalWithConfigs.completionTarget
                    if (target != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = VrddhihSpacing.xs),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StepperButton(
                                text = "−1",
                                onClick = {
                                    val next = (target.currentCount - 1).coerceAtLeast(0)
                                    onUpdateCompletionCount(goal.id, next)
                                },
                            )
                            Spacer(Modifier.width(VrddhihSpacing.sm))
                            StepperButton(
                                text = "+1",
                                onClick = {
                                    val next = target.currentCount + 1
                                    onUpdateCompletionCount(goal.id, next)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: GoalStatus) {
    val (label, bg, fg) = when (status) {
        GoalStatus.ACTIVE -> Triple("ACTIVE", VrddhihColors.Electric.copy(alpha = 0.15f), VrddhihColors.Ice)
        GoalStatus.COMPLETED -> Triple("COMPLETED", VrddhihColors.Ice.copy(alpha = 0.2f), VrddhihColors.Ice)
        GoalStatus.PAUSED -> Triple("PAUSED", VrddhihColors.TextMuted.copy(alpha = 0.15f), VrddhihColors.TextMuted)
        GoalStatus.NOT_STARTED -> Triple("NOT STARTED", VrddhihColors.TextMuted.copy(alpha = 0.1f), VrddhihColors.TextMuted)
        GoalStatus.ARCHIVED -> Triple("ARCHIVED", Color.Transparent, VrddhihColors.TextMuted)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(VrddhihRadii.xs))
            .background(bg)
            .border(0.5.dp, fg.copy(alpha = 0.4f), RoundedCornerShape(VrddhihRadii.xs))
            .padding(horizontal = VrddhihSpacing.xs, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = fg,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TargetDateBadge(date: LocalDate) {
    val today = LocalDate.now()
    val daysUntil = ChronoUnit.DAYS.between(today, date)
    val formatted = date.format(DateTimeFormatter.ofPattern("MMM d"))

    val (badgeText, badgeColor) = when {
        daysUntil < 0 -> "OVERDUE • $formatted" to VrddhihColors.Danger
        daysUntil == 0L -> "D-0 • TODAY" to VrddhihColors.Danger
        daysUntil <= 7 -> "D-$daysUntil • $formatted" to VrddhihColors.Electric
        else -> "D-$daysUntil • $formatted" to VrddhihColors.TextMuted
    }

    Text(
        text = badgeText,
        style = MaterialTheme.typography.labelSmall,
        fontFamily = FontFamily.Monospace,
        color = badgeColor,
    )
}

@Composable
private fun StepperButton(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(VrddhihRadii.xs))
            .background(VrddhihColors.PanelRaised)
            .border(1.dp, VrddhihColors.Outline, RoundedCornerShape(VrddhihRadii.xs))
            .clickable(onClick = onClick)
            .padding(horizontal = VrddhihSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            color = VrddhihColors.Ice,
            fontWeight = FontWeight.Bold,
        )
    }
}
