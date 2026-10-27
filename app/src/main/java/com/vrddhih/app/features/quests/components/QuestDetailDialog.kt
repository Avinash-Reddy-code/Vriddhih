package com.vrddhih.app.features.quests.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vrddhih.app.R
import com.vrddhih.app.core.data.local.entity.QuestRecurrence
import com.vrddhih.app.core.data.quest.QuestOccurrence
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.SystemPanel
import com.vrddhih.app.core.ui.components.SystemTag
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestDetailDialog(
    occurrence: QuestOccurrence,
    today: LocalDate,
    onToggleCompletion: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    category: com.vrddhih.app.core.data.local.entity.CategoryEntity? = null,
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) }
    val isOverdue = remember(occurrence, today) { occurrence.isOverdue(today) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        SystemPanel(
            modifier = modifier
                .padding(horizontal = VrddhihSpacing.lg)
                .widthIn(max = 480.dp)
                .fillMaxWidth(),
            glow = true,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.quest_detail_title),
                    style = VrddhihTextStyles.SystemLabel,
                    color = VrddhihColors.Ice,
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.quest_close),
                        tint = VrddhihColors.TextSecondary,
                    )
                }
            }

            Spacer(Modifier.height(VrddhihSpacing.sm))

            Text(
                text = occurrence.title,
                style = MaterialTheme.typography.headlineSmall,
                color = VrddhihColors.TextPrimary,
            )

            if (occurrence.notes.isNotBlank()) {
                Spacer(Modifier.height(VrddhihSpacing.sm))
                Text(
                    text = occurrence.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VrddhihColors.TextSecondary,
                )
            }

            Spacer(Modifier.height(VrddhihSpacing.md))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xxs),
            ) {
                TaskPriorityTag(priority = occurrence.priority)

                if (occurrence.isCompleted) {
                    SystemTag(text = stringResource(R.string.habit_state_done), color = VrddhihColors.Success)
                } else if (isOverdue) {
                    SystemTag(text = stringResource(R.string.quests_overdue_tag), color = VrddhihColors.Danger)
                } else {
                    SystemTag(text = stringResource(R.string.habit_state_pending), color = VrddhihColors.Warning)
                }

                if (occurrence.isRecurring) {
                    val label = when (occurrence.quest.recurrence) {
                        QuestRecurrence.DAILY -> stringResource(R.string.quests_recurring_daily)
                        QuestRecurrence.WEEKDAYS -> stringResource(R.string.quests_recurring_weekdays)
                        QuestRecurrence.WEEKLY -> stringResource(R.string.quests_recurring_weekly)
                        QuestRecurrence.MONTHLY -> stringResource(R.string.quests_recurring_monthly)
                        QuestRecurrence.NONE -> ""
                    }
                    if (label.isNotEmpty()) {
                        SystemTag(text = label, color = VrddhihColors.Electric)
                    }
                }

                if (category != null) {
                    com.vrddhih.app.core.ui.components.CategoryPill(category = category)
                } else if (occurrence.quest.category.isNotBlank()) {
                    SystemTag(text = occurrence.quest.category, color = VrddhihColors.Frost)
                }
            }

            Spacer(Modifier.height(VrddhihSpacing.md))

            Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                DetailReadout(
                    label = stringResource(R.string.quest_field_date),
                    value = dateFormatter.format(occurrence.date),
                )
                if (occurrence.dueTime != null) {
                    DetailReadout(
                        label = stringResource(R.string.quest_field_due_time),
                        value = timeFormatter.format(occurrence.dueTime),
                    )
                }
                if (occurrence.quest.deadline != null) {
                    DetailReadout(
                        label = stringResource(R.string.quest_field_deadline),
                        value = dateFormatter.format(occurrence.quest.deadline),
                    )
                }
                if (occurrence.quest.xpReward > 0) {
                    DetailReadout(
                        label = "Reward",
                        value = "+${occurrence.quest.xpReward} XP",
                    )
                }
            }

            Spacer(Modifier.height(VrddhihSpacing.lg))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.quest_action_delete),
                        tint = VrddhihColors.Danger,
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.quest_action_edit),
                        tint = VrddhihColors.Ice,
                    )
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onToggleCompletion,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (occurrence.isCompleted) VrddhihColors.PanelRaised else VrddhihColors.Electric,
                        contentColor = if (occurrence.isCompleted) VrddhihColors.TextPrimary else VrddhihColors.Void,
                    ),
                ) {
                    Text(
                        stringResource(
                            if (occurrence.isCompleted) R.string.quest_action_reopen else R.string.quest_action_complete,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailReadout(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = VrddhihColors.TextMuted)
        Text(text = value, style = VrddhihTextStyles.NumericSmall, color = VrddhihColors.TextPrimary)
    }
}
