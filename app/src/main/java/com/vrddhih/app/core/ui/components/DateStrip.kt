package com.vrddhih.app.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.time.format.TextStyle as JavaTextStyle

private val DateCellMinWidth = 44.dp
private val DateCellHeight = 68.dp
private val DateCellGap = 6.dp

/**
 * A horizontal row of selectable days. Cells share the available width, and the row scrolls
 * horizontally when the screen is too narrow for the minimum touch-friendly cell width.
 */
@Composable
fun DateStrip(
    days: List<LocalDate>,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = currentLocale()
    val labelFormatter = rememberFullDateFormatter()
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val count = days.size.coerceAtLeast(1)
        val cellWidth = maxOf(DateCellMinWidth, (maxWidth - DateCellGap * (count - 1)) / count)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(DateCellGap),
        ) {
            days.forEach { date ->
                key(date) {
                    DateStripCell(
                        date = date,
                        weekday = date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, locale),
                        accessibilityLabel = labelFormatter.format(date),
                        isSelected = date == selectedDate,
                        isToday = date == today,
                        width = cellWidth,
                        onClick = { onDateSelected(date) },
                    )
                }
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
            Text(text = weekday, style = VrddhihTextStyles.TagLabel, color = contentColor)
            Spacer(Modifier.height(VrddhihSpacing.xxs))
            Text(text = date.dayOfMonth.toString(), style = VrddhihTextStyles.NumericMedium, color = contentColor)
            Spacer(Modifier.height(VrddhihSpacing.xxs))
            TodayDot(visible = isToday, color = if (highlightToday) VrddhihColors.Void else VrddhihColors.Ice)
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

/** Spoken state for a day cell, or null when the day is neither today nor selected. */
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

/** Locale-aware full date, for example "Sunday, October 25, 2026" in US English. */
@Composable
fun rememberFullDateFormatter(): DateTimeFormatter {
    val locale = currentLocale()
    return remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale) }
}
