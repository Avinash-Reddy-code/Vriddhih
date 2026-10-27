package com.vrddhih.app.features.quests.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vrddhih.app.R
import com.vrddhih.app.core.data.local.entity.QuestRecurrence
import com.vrddhih.app.core.data.quest.QuestOccurrence
import com.vrddhih.app.core.theme.VrddhihBrushes
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.SystemPanel
import com.vrddhih.app.core.ui.components.SystemTag
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestItemRow(
    occurrence: QuestOccurrence,
    today: LocalDate,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    category: com.vrddhih.app.core.data.local.entity.CategoryEntity? = null,
) {
    val isOverdue = remember(occurrence, today) { occurrence.isOverdue(today) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()) }

    SystemPanel(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        accent = if (isOverdue) VrddhihColors.Danger else VrddhihColors.OutlineBright,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(VrddhihRadii.sm))
                    .toggleable(
                        value = occurrence.isCompleted,
                        role = Role.Checkbox,
                        onValueChange = { onToggle() },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                QuestCheckbox(checked = occurrence.isCompleted)
            }

            Spacer(Modifier.width(VrddhihSpacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = occurrence.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (occurrence.isCompleted) VrddhihColors.TextMuted else VrddhihColors.TextPrimary,
                        textDecoration = if (occurrence.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    if (occurrence.quest.xpReward > 0) {
                        Spacer(Modifier.width(VrddhihSpacing.xs))
                        Text(
                            text = "+${occurrence.quest.xpReward} XP",
                            style = VrddhihTextStyles.NumericSmall,
                            color = VrddhihColors.Ice,
                        )
                    }
                }

                if (occurrence.notes.isNotBlank()) {
                    Spacer(Modifier.height(VrddhihSpacing.xxs))
                    Text(
                        text = occurrence.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = VrddhihColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(VrddhihSpacing.xs))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xxs),
                ) {
                    TaskPriorityTag(priority = occurrence.priority)

                    if (isOverdue) {
                        SystemTag(
                            text = stringResource(R.string.quests_overdue_tag),
                            color = VrddhihColors.Danger,
                        )
                    }

                    if (occurrence.dueTime != null) {
                        SystemTag(
                            text = timeFormatter.format(occurrence.dueTime),
                            color = VrddhihColors.Ice,
                        )
                    }

                    if (occurrence.date != today) {
                        SystemTag(
                            text = dateFormatter.format(occurrence.date),
                            color = VrddhihColors.TextSecondary,
                        )
                    }

                    if (occurrence.isRecurring) {
                        val recurrenceLabel = when (occurrence.quest.recurrence) {
                            QuestRecurrence.DAILY -> stringResource(R.string.quests_recurring_daily)
                            QuestRecurrence.WEEKDAYS -> stringResource(R.string.quests_recurring_weekdays)
                            QuestRecurrence.WEEKLY -> stringResource(R.string.quests_recurring_weekly)
                            QuestRecurrence.MONTHLY -> stringResource(R.string.quests_recurring_monthly)
                            QuestRecurrence.NONE -> ""
                        }
                        if (recurrenceLabel.isNotEmpty()) {
                            SystemTag(
                                text = recurrenceLabel,
                                color = VrddhihColors.Electric,
                            )
                        }
                    }

                    if (category != null) {
                        com.vrddhih.app.core.ui.components.CategoryPill(category = category)
                    } else if (occurrence.quest.category.isNotBlank()) {
                        SystemTag(
                            text = occurrence.quest.category,
                            color = VrddhihColors.Frost,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(VrddhihRadii.xs)
    val fill by animateColorAsState(
        targetValue = if (checked) VrddhihColors.Electric else Color.Transparent,
        label = "checkboxFill",
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) VrddhihColors.Ice else VrddhihColors.OutlineBright,
        label = "checkboxBorder",
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
