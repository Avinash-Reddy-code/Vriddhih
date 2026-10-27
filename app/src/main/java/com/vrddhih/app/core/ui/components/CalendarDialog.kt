package com.vrddhih.app.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihBrushes
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.time.CalendarMath
import com.vrddhih.app.core.ui.gesture.swipeDirection
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle

private val CalendarMaxWidth = 420.dp

/** Compact calendar icon button used to open [VrddhihCalendarDialog]. */
@Composable
fun CalendarToggleButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier.size(48.dp)) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(VrddhihColors.Electric.copy(alpha = 0.12f))
                .border(1.dp, VrddhihColors.Electric.copy(alpha = 0.55f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = stringResource(R.string.calendar_open),
                tint = VrddhihColors.Ice,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Month calendar in the status-window style. The displayed month is temporary UI state: browsing
 * months never changes the selection. Swiping horizontally or tapping arrow controls changes the displayed
 * month. Only tapping a day ([onDateSelected]) or Today ([onTodaySelected]) reports a new date;
 * Close ([onDismiss]) leaves the selection untouched.
 */
@Composable
fun VrddhihCalendarDialog(
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onTodaySelected: () -> Unit,
    onDismiss: () -> Unit,
) {
    var displayedMonth by rememberSaveable { mutableStateOf(YearMonth.from(selectedDate)) }
    var totalDrag by remember { mutableFloatStateOf(0f) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        SystemPanel(
            modifier = Modifier
                .padding(horizontal = VrddhihSpacing.xl)
                .widthIn(max = CalendarMaxWidth)
                .fillMaxWidth(),
            glow = true,
        ) {
            CalendarHeader(
                month = displayedMonth,
                onPrevious = { displayedMonth = displayedMonth.minusMonths(1) },
                onNext = { displayedMonth = displayedMonth.plusMonths(1) },
            )
            Spacer(Modifier.height(VrddhihSpacing.lg))
            WeekdayHeaderRow()
            Spacer(Modifier.height(VrddhihSpacing.sm))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(displayedMonth) {
                        detectHorizontalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onDragEnd = {
                                if (totalDrag < -40.dp.toPx()) {
                                    displayedMonth = displayedMonth.plusMonths(1)
                                } else if (totalDrag > 40.dp.toPx()) {
                                    displayedMonth = displayedMonth.minusMonths(1)
                                }
                                totalDrag = 0f
                            },
                            onDragCancel = { totalDrag = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                totalDrag += dragAmount
                            },
                        )
                    },
            ) {
                AnimatedContent(
                    targetState = displayedMonth,
                    transitionSpec = { fadeIn(tween(140)) togetherWith fadeOut(tween(140)) },
                    label = "calendar-month",
                ) { month ->
                    MonthGrid(month = month, selectedDate = selectedDate, today = today, onDateSelected = onDateSelected)
                }
            }
            Spacer(Modifier.height(VrddhihSpacing.lg))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(VrddhihBrushes.Divider),
            )
            Spacer(Modifier.height(VrddhihSpacing.md))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.calendar_close), color = VrddhihColors.TextSecondary)
                }
                Spacer(Modifier.weight(1f))
                OutlinedButton(
                    onClick = onTodaySelected,
                    border = BorderStroke(1.dp, VrddhihColors.Ice.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = VrddhihColors.Electric.copy(alpha = 0.14f),
                        contentColor = VrddhihColors.Ice,
                    ),
                ) {
                    Text(text = stringResource(R.string.calendar_today))
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader(month: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    val locale = currentLocale()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.calendar_previous_month),
                tint = VrddhihColors.Ice,
            )
        }
        val monthName = month.month.getDisplayName(JavaTextStyle.FULL_STANDALONE, locale)
            .replaceFirstChar { it.titlecase(locale) }
        val year = month.year.toString()
        Column(
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) {
                    heading()
                    liveRegion = LiveRegionMode.Polite
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = monthName, style = MaterialTheme.typography.titleLarge, color = VrddhihColors.TextPrimary)
            Text(text = year, style = VrddhihTextStyles.NumericSmall, color = VrddhihColors.Ice)
        }
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.calendar_next_month),
                tint = VrddhihColors.Ice,
            )
        }
    }
}

@Composable
private fun WeekdayHeaderRow() {
    val locale = currentLocale()
    Row(modifier = Modifier.fillMaxWidth()) {
        CalendarMath.weekdays().forEach { day ->
            Text(
                text = day.getDisplayName(JavaTextStyle.SHORT, locale).uppercase(locale),
                style = VrddhihTextStyles.TagLabel,
                color = VrddhihColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics { },
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    val labelFormatter = rememberFullDateFormatter()
    val weeks = CalendarMath.monthGrid(month)
    val paddedWeeks = if (weeks.size < 6) {
        weeks + List(6 - weeks.size) { List<LocalDate?>(7) { null } }
    } else {
        weeks
    }
    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xxs)) {
        paddedWeeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (date != null) {
                            CalendarDayCell(
                                date = date,
                                accessibilityLabel = labelFormatter.format(date),
                                isSelected = date == selectedDate,
                                isToday = date == today,
                                onClick = { onDateSelected(date) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    accessibilityLabel: String,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val stateLabel = dateStateLabel(isSelected = isSelected, isToday = isToday)
    val textColor = when {
        isSelected -> VrddhihColors.Void
        isToday -> VrddhihColors.Ice
        else -> VrddhihColors.TextPrimary
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(3.dp)
            .clip(CircleShape)
            .then(if (isSelected) Modifier.background(VrddhihBrushes.Energy) else Modifier)
            .then(
                when {
                    isSelected && isToday -> Modifier.border(2.dp, VrddhihColors.Frost, CircleShape)
                    isToday -> Modifier.border(1.5.dp, VrddhihColors.Ice.copy(alpha = 0.8f), CircleShape)
                    else -> Modifier
                },
            )
            .selectable(selected = isSelected, role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = accessibilityLabel
                if (stateLabel != null) stateDescription = stateLabel
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = VrddhihTextStyles.NumericSmall,
            color = textColor,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}
