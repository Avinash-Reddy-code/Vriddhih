package com.vrddhih.app.features.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import com.vrddhih.app.R
import com.vrddhih.app.core.data.repository.DayActivity
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.time.DateSelectionState
import com.vrddhih.app.core.ui.components.SectionHeader
import com.vrddhih.app.core.ui.components.SystemPanel
import com.vrddhih.app.core.ui.components.SystemTag
import com.vrddhih.app.core.ui.components.rememberFullDateFormatter
import kotlin.math.absoluteValue

/** Shows the selected day and the real records stored on this device for it. */
@Composable
fun DayLogPanel(
    dateState: DateSelectionState,
    dayLog: DayLogUiState,
    dayLogData: com.vrddhih.app.core.data.quest.DayLog? = null,
    modifier: Modifier = Modifier,
) {
    val formatter = rememberFullDateFormatter()
    val timeFormatter = remember { java.time.format.DateTimeFormatter.ofPattern("HH:mm", java.util.Locale.getDefault()) }

    val isFuture = dateState.offsetFromToday > 0

    SystemPanel(modifier = modifier) {
        SectionHeader(title = stringResource(R.string.day_log_title)) {
            SystemTag(text = stringResource(R.string.day_log_history_badge), color = VrddhihColors.Ice)
        }
        Spacer(Modifier.height(VrddhihSpacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatter.format(dateState.selectedDate),
                style = MaterialTheme.typography.titleMedium,
                color = VrddhihColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(VrddhihSpacing.sm))
            SystemTag(
                text = relativeDayLabel(dateState.offsetFromToday),
                color = if (dateState.isTodaySelected) VrddhihColors.Ice else VrddhihColors.TextSecondary,
            )
        }
        Spacer(Modifier.height(VrddhihSpacing.md))

        if (isFuture) {
            DayLogMessage(stringResource(R.string.day_log_future), VrddhihColors.TextMuted)
        } else if (dayLogData != null) {
            if (dayLogData.isEmpty) {
                DayLogMessage(stringResource(R.string.day_log_empty))
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DayCount(value = dayLogData.completedCount, label = stringResource(R.string.day_log_quests))
                    DayCount(value = dayLogData.reopenedCount, label = stringResource(R.string.day_log_reopened))
                }

                if (dayLogData.events.isNotEmpty()) {
                    Spacer(Modifier.height(VrddhihSpacing.sm))
                    dayLogData.events.takeLast(5).forEach { event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = VrddhihSpacing.xxs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val isCompleted = event.type == com.vrddhih.app.core.data.local.entity.ActivityType.QUEST_COMPLETED
                            val tagColor = if (isCompleted) VrddhihColors.Success else VrddhihColors.Warning
                            val tagText = if (isCompleted) stringResource(R.string.habit_state_done) else stringResource(R.string.day_log_reopened)

                            SystemTag(text = tagText, color = tagColor)
                            Spacer(Modifier.width(VrddhihSpacing.xs))
                            Text(
                                text = event.questTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = VrddhihColors.TextPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        } else {
            when (dayLog) {
                DayLogUiState.Loading -> DayLogMessage(stringResource(R.string.day_log_loading))
                DayLogUiState.Unavailable -> DayLogMessage(stringResource(R.string.day_log_unavailable), VrddhihColors.Danger)
                is DayLogUiState.Loaded ->
                    if (dayLog.activity.isEmpty) {
                        DayLogMessage(stringResource(R.string.day_log_empty))
                    } else {
                        DayActivityCounts(dayLog.activity)
                    }
            }
        }
        Spacer(Modifier.height(VrddhihSpacing.sm))
        Text(
            text = stringResource(R.string.day_log_note),
            style = MaterialTheme.typography.bodySmall,
            color = VrddhihColors.TextMuted,
        )
    }
}

@Composable
private fun DayLogMessage(text: String, color: androidx.compose.ui.graphics.Color = VrddhihColors.TextSecondary) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = color)
}

@Composable
private fun DayActivityCounts(activity: DayActivity) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        DayCount(value = activity.questCompletions, label = stringResource(R.string.day_log_quests))
        DayCount(value = activity.habitCheckIns, label = stringResource(R.string.day_log_habits))
        DayCount(value = activity.transactions, label = stringResource(R.string.day_log_transactions))
    }
}

@Composable
private fun DayCount(value: Int, label: String) {
    Column(modifier = Modifier.semantics(mergeDescendants = true) { }) {
        Text(text = value.toString(), style = VrddhihTextStyles.NumericMedium, color = VrddhihColors.Frost)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = VrddhihColors.TextSecondary)
    }
}

@Composable
private fun relativeDayLabel(offsetFromToday: Long): String {
    val days = offsetFromToday.absoluteValue.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    return when {
        offsetFromToday == 0L -> stringResource(R.string.date_relative_today)
        offsetFromToday == -1L -> stringResource(R.string.date_relative_yesterday)
        offsetFromToday == 1L -> stringResource(R.string.date_relative_tomorrow)
        offsetFromToday < 0 -> pluralStringResource(R.plurals.date_relative_days_ago, days, days)
        else -> pluralStringResource(R.plurals.date_relative_days_ahead, days, days)
    }
}
