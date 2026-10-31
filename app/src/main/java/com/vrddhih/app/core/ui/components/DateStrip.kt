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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private val DateCellWidth = 46.dp
private val DateCellHeight = 60.dp
private val DateCellGap = 6.dp
private const val TIMELINE_PAST_DAYS = 180L
private const val TIMELINE_FUTURE_DAYS = 180L
private const val TOTAL_TIMELINE_DAYS = (TIMELINE_PAST_DAYS + TIMELINE_FUTURE_DAYS + 1).toInt()

/**
 * A horizontally scrollable compact date timeline inspired by HabitNow.
 *
 * Efficiently streams dates around [today] via a [LazyRow]. Ordinary finger-following scrolling
 * moves dates naturally with no sudden jumps, bounces, or page-turn animations. Tapping a date
 * updates [selectedDate] without resetting scroll position.
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

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .testTag(DATE_STRIP_TEST_TAG)
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
    val outerShape = RoundedCornerShape(14.dp)
    val insetShape = RoundedCornerShape(10.dp)

    val animatedBorderColor by animateColorAsState(
        targetValue = when {
            isSelected -> VrddhihColors.Cyan.copy(alpha = 0.55f)
            isToday -> VrddhihColors.Electric.copy(alpha = 0.5f)
            isFirstOfMonth -> VrddhihColors.OutlineBright
            else -> VrddhihColors.Outline
        },
        label = "cell-border",
    )

    val weekdayColor = when {
        isSelected -> VrddhihColors.Void.copy(alpha = 0.85f)
        isToday -> VrddhihColors.Cyan
        isFirstOfMonth -> VrddhihColors.Cyan
        else -> VrddhihColors.TextMuted
    }

    val lowerBlockBg = if (isSelected) {
        VrddhihColors.Void.copy(alpha = 0.18f)
    } else {
        VrddhihColors.PanelRaised
    }

    val dateNumberColor = when {
        isSelected -> VrddhihColors.Void
        isToday -> VrddhihColors.Cyan
        else -> VrddhihColors.TextPrimary
    }

    val todayDashColor = if (isSelected) VrddhihColors.Void.copy(alpha = 0.85f) else VrddhihColors.Cyan

    val stateLabel = dateStateLabel(isSelected = isSelected, isToday = isToday)

    Box(
        modifier = Modifier
            .width(width)
            .height(DateCellHeight)
            .then(
                if (isSelected) {
                    Modifier.neonPillGlow(VrddhihColors.Electric, spread = 3.dp, cornerRadius = 14.dp, alpha = 0.26f)
                } else {
                    Modifier
                },
            )
            .clip(outerShape)
            .then(
                if (isSelected) {
                    Modifier.background(VrddhihBrushes.Energy, outerShape)
                } else {
                    Modifier.background(VrddhihColors.Panel, outerShape)
                },
            )
            .border(1.dp, animatedBorderColor, outerShape)
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick)
            .semantics {
                contentDescription = accessibilityLabel
                if (stateLabel != null) stateDescription = stateLabel
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp)
                .clearAndSetSemantics { },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Section: Abbreviated day of the week
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(21.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = weekday,
                    style = VrddhihTextStyles.TagLabel,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = weekdayColor,
                    maxLines = 1,
                )
            }

            // Bottom Section: Inset rounded block containing the date number and today dash
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(insetShape)
                    .background(lowerBlockBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    ),
                    color = dateNumberColor,
                )

                if (isToday) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 3.dp)
                            .width(12.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(todayDashColor),
                    )
                }
            }
        }
    }
}

@Composable
internal fun TodayDot(visible: Boolean, color: Color, modifier: Modifier = Modifier) {
    TodayBar(visible = visible, color = color, modifier = modifier)
}

@Composable
internal fun TodayBar(visible: Boolean, color: Color, modifier: Modifier = Modifier) {
    if (visible) {
        Box(
            modifier = modifier
                .width(16.dp)
                .height(2.5.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(color),
        )
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

