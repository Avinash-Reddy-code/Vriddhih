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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vrddhih.app.core.data.local.entity.HabitType
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.CuteCategoryBadge
import com.vrddhih.app.core.ui.components.SystemPanel
import com.vrddhih.app.core.ui.components.SystemTag
import com.vrddhih.app.features.habits.HabitItemDisplay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitStatisticsDialog(
    display: HabitItemDisplay,
    onDismiss: () -> Unit,
) {
    val habit = display.habitWithConfigs.habit
    val category = display.category
    val categoryColor = category?.let { Color(it.colorArgb) } ?: VrddhihColors.Cyan
    val stats = display.statistics

    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ) {
        SystemPanel(
            accent = categoryColor,
            glow = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(VrddhihSpacing.md),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
            ) {
                // Header with category badge, habit name, and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CuteCategoryBadge(
                        category = category,
                        size = 40.dp,
                        iconSize = 20.dp,
                    )

                    Spacer(Modifier.width(VrddhihSpacing.sm))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = VrddhihColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = category?.name ?: "HABIT",
                                style = MaterialTheme.typography.labelSmall,
                                color = categoryColor,
                                fontWeight = FontWeight.SemiBold,
                            )
                            SystemTag(
                                text = habit.type.name.replace("_", " "),
                                color = VrddhihColors.Ice,
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = VrddhihColors.TextMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Text(
                    text = "HABIT PERFORMANCE & STATS",
                    style = VrddhihTextStyles.SystemLabel,
                    color = VrddhihColors.Ice,
                )

                // Streak Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                ) {
                    StatCard(
                        title = "CURRENT STREAK",
                        value = "${stats?.streak?.current ?: display.currentStreak} Days",
                        icon = Icons.Outlined.LocalFireDepartment,
                        tint = VrddhihColors.Warning,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        title = "BEST STREAK",
                        value = "${stats?.streak?.best ?: display.currentStreak} Days",
                        icon = Icons.Outlined.MilitaryTech,
                        tint = VrddhihColors.NeonGreen,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Completion Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                ) {
                    val ratePercent = ((stats?.completionRate ?: display.weeklyCompletionRate) * 100).roundToInt()
                    StatCard(
                        title = "COMPLETION RATE",
                        value = "$ratePercent%",
                        icon = Icons.Outlined.CheckCircle,
                        tint = if (ratePercent >= 70) VrddhihColors.NeonGreen else VrddhihColors.Ice,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        title = "TOTAL SESSIONS",
                        value = "${stats?.totalSessions ?: 0}",
                        icon = Icons.Outlined.Pin,
                        tint = VrddhihColors.Electric,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Type-Specific Statistics
                when (habit.type) {
                    HabitType.TIMER -> {
                        val totalMinutes = stats?.totalTimeMinutes ?: 0
                        val hours = totalMinutes / 60
                        val mins = totalMinutes % 60
                        val timeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                        StatCard(
                            title = "TOTAL TIME TRACKED",
                            value = timeStr,
                            icon = Icons.Outlined.Timer,
                            tint = VrddhihColors.Cyan,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    HabitType.NUMERIC -> {
                        val unit = display.habitWithConfigs.numericConfig?.unit.orEmpty()
                        val totalVal = stats?.numericTotal ?: 0L
                        val avgVal = stats?.numericAverage ?: 0f
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                        ) {
                            StatCard(
                                title = "TOTAL RECORDED",
                                value = "$totalVal $unit".trim(),
                                icon = Icons.Outlined.Pin,
                                tint = VrddhihColors.Cyan,
                                modifier = Modifier.weight(1f),
                            )
                            StatCard(
                                title = "DAILY AVERAGE",
                                value = "${"%.1f".format(avgVal)} $unit".trim(),
                                icon = Icons.Outlined.Pin,
                                tint = VrddhihColors.Ice,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    HabitType.CHECKLIST -> {
                        val checklistRate = ((stats?.checklistCompletionRate ?: 0f) * 100).roundToInt()
                        StatCard(
                            title = "ITEM COMPLETION RATE",
                            value = "$checklistRate%",
                            icon = Icons.Outlined.CheckCircle,
                            tint = VrddhihColors.NeonGreen,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    else -> Unit
                }

                Spacer(Modifier.height(VrddhihSpacing.xs))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VrddhihColors.PanelRaised,
                        contentColor = VrddhihColors.Ice,
                    ),
                    shape = RoundedCornerShape(VrddhihRadii.sm),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VrddhihColors.OutlineBright),
                ) {
                    Text("CLOSE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(VrddhihRadii.xs)
    Box(
        modifier = modifier
            .clip(shape)
            .background(VrddhihColors.PanelRaised)
            .border(1.dp, VrddhihColors.Outline, shape)
            .padding(VrddhihSpacing.sm),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = title,
                    style = VrddhihTextStyles.TagLabel,
                    color = VrddhihColors.TextMuted,
                )
            }
            Text(
                text = value,
                style = VrddhihTextStyles.NumericMedium,
                color = tint,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
