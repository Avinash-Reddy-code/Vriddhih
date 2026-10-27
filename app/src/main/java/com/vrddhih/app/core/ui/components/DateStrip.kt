package com.vrddhih.app.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihBrushes
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.gesture.consumeHorizontalNestedScroll
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.time.format.TextStyle as JavaTextStyle

const val DATE_STRIP_TEST_TAG = "date_strip"

private val DateCellWidth = 50.dp
private val DateCellHeight = 68.dp
private val DateCellGap = 6.dp
private const val TIMELINE_PAST_DAYS = 180L
private const val TIMELINE_FUTURE_DAYS = 180L
private const val TOTAL_TIMELINE_DAYS = (TIMELINE_PAST_DAYS + TIMELINE_FUTURE_DAYS + 1).toInt()

/**
 * A horizontally scrollable date timeline inspired by HabitNow.
 *
 * Efficiently streams dates around [today] via a [LazyRow]. Ordinary finger-following scrolling
 * moves dates naturally with no sudden jumps, bounces, or page-turn animations. Tapping a date
 * updates [selectedDate] without resetting scroll position. A sleek Today quick-action button
 * appears whenever historical or future dates are active.
 */
@Composable
fun DateStrip(
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    onSelectToday: () -> Unit = {},
    days: List<LocalDate> = emptyList(),
    onShiftWindow: (direction: Int) -> Unit = {},
) {
    val locale = currentLocale()
    val labelFormatter = rememberFullDateFormatter()
    val anchorDate = remember(today) { today.minusDays(TIMELINE_PAST_DAYS) }

    val selectedIndex = remember(anchorDate, selectedDate) {
        ChronoUnit.DAYS.between(anchorDate, selectedDate).toInt().coerceIn(0, TOTAL_TIMELINE_DAYS - 1)
    }

    // Initialize list near the active selection so today is visible by default
    val listState: LazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = (selectedIndex - 2).coerceAtLeast(0),
    )

    // Smoothly scroll into view only if selected date changed externally and is off-screen
    LaunchedEffect(selectedDate) {
        val visibleIndices = listState.layoutInfo.visibleItemsInfo.map { it.index }
        if (visibleIndices.isNotEmpty() && selectedIndex !in visibleIndices) {
            listState.animateScrollToItem((selectedIndex - 2).coerceAtLeast(0))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(DATE_STRIP_TEST_TAG),
    ) {
        // Month/Year header with contextual "TODAY" return action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VrddhihSpacing.xs, vertical = VrddhihSpacing.xxs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val monthName = selectedDate.month.getDisplayName(JavaTextStyle.FULL_STANDALONE, locale)
                .replaceFirstChar { it.titlecase(locale) }
            Text(
                text = "${monthName.uppercase(locale)} ${selectedDate.year}",
                style = VrddhihTextStyles.TagLabel,
                color = VrddhihColors.TextSecondary,
            )

            if (selectedDate != today) {
                TodayReturnButton(onClick = onSelectToday)
            }
        }

        Spacer(Modifier.height(VrddhihSpacing.xs))

        // Horizontally scrollable date timeline
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .consumeHorizontalNestedScroll()
                .semantics {
                    contentDescription = "Date timeline"
                },
            horizontalArrangement = Arrangement.spacedBy(DateCellGap),
            contentPadding = PaddingValues(horizontal = VrddhihSpacing.xs),
        ) {
            items(
                count = TOTAL_TIMELINE_DAYS,
                key = { index -> anchorDate.plusDays(index.toLong()).toEpochDay() },
            ) { index ->
                val date = anchorDate.plusDays(index.toLong())
                val isSelected = date == selectedDate
                val isToday = date == today
                val isFirstOfMonth = date.dayOfMonth == 1

                val weekdayShort = if (isFirstOfMonth) {
                    date.month.getDisplayName(JavaTextStyle.SHORT, locale).uppercase(locale)
                } else {
                    date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, locale).uppercase(locale)
                }

                DateStripCell(
                    date = date,
                    weekday = weekdayShort,
                    accessibilityLabel = labelFormatter.format(date),
                    isSelected = isSelected,
                    isToday = isToday,
                    isFirstOfMonth = isFirstOfMonth,
                    width = DateCellWidth,
                    onClick = { onDateSelected(date) },
                )
            }
        }
    }
}

@Composable
private fun DateStripCell(
    date: LocalDate,
    weekday: String,
    accessibilityLabel: String,
    isSelected: Boolean,
    isToday: Boolean,
    isFirstOfMonth: Boolean,
    width: Dp,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(VrddhihRadii.md)
    val highlightToday = isSelected && isToday

    val contentColor by animateColorAsState(
        targetValue = when {
            highlightToday -> VrddhihColors.Void
            isSelected -> VrddhihColors.Frost
            isToday -> VrddhihColors.Ice
            else -> VrddhihColors.TextSecondary
        },
        label = "date-cell-content",
    )

    val borderColor = when {
        highlightToday -> VrddhihColors.Ice
        isSelected -> VrddhihColors.Electric
        isToday -> VrddhihColors.Ice.copy(alpha = 0.6f)
        isFirstOfMonth -> VrddhihColors.OutlineBright
        else -> VrddhihColors.Outline
    }

    val stateLabel = dateStateLabel(isSelected = isSelected, isToday = isToday)

    Box(
        modifier = Modifier
            .width(width)
            .height(DateCellHeight)
            .clip(shape)
            .then(
                when {
                    highlightToday -> Modifier.background(VrddhihBrushes.Energy)
                    isSelected -> Modifier.background(VrddhihColors.PanelRaised)
                    else -> Modifier.background(VrddhihColors.Panel.copy(alpha = 0.7f))
                },
            )
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, shape)
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick)
            .semantics {
                contentDescription = accessibilityLabel
                if (stateLabel != null) stateDescription = stateLabel
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.clearAndSetSemantics { },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = weekday,
                style = VrddhihTextStyles.TagLabel,
                color = if (isFirstOfMonth && !isSelected) VrddhihColors.Ice else contentColor,
                maxLines = 1,
            )
            Spacer(Modifier.height(VrddhihSpacing.xxs))
            Text(
                text = date.dayOfMonth.toString(),
                style = VrddhihTextStyles.NumericMedium,
                color = contentColor,
            )
            Spacer(Modifier.height(VrddhihSpacing.xxs))
            TodayDot(
                visible = isToday,
                color = if (highlightToday) VrddhihColors.Void else VrddhihColors.Ice,
            )
        }
    }
}

@Composable
private fun TodayReturnButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(28.dp)
            .clip(RoundedCornerShape(VrddhihRadii.lg))
            .background(VrddhihColors.Electric.copy(alpha = 0.14f))
            .border(1.dp, VrddhihColors.Ice.copy(alpha = 0.5f), RoundedCornerShape(VrddhihRadii.lg))
            .selectable(selected = false, role = Role.Button, onClick = onClick)
            .padding(horizontal = VrddhihSpacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = null,
                tint = VrddhihColors.Ice,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = stringResource(R.string.date_strip_today_action).uppercase(),
                style = VrddhihTextStyles.TagLabel,
                color = VrddhihColors.Ice,
            )
        }
    }
}

@Composable
internal fun TodayDot(visible: Boolean, color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(4.dp)) {
        if (visible) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
internal fun dateStateLabel(isSelected: Boolean, isToday: Boolean): String? = when {
    isSelected && isToday -> stringResource(R.string.date_state_today_selected)
    isSelected -> stringResource(R.string.date_state_selected)
    isToday -> stringResource(R.string.date_state_today)
    else -> null
}

@Composable
@ReadOnlyComposable
fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

@Composable
fun rememberFullDateFormatter(): DateTimeFormatter {
    val locale = currentLocale()
    return remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale) }
}

