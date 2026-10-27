package com.vrddhih.app.features.habits.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vrddhih.app.R
import com.vrddhih.app.core.data.local.dao.HabitWithConfigs
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.data.local.entity.HabitEntity
import com.vrddhih.app.core.data.local.entity.HabitScheduleType
import com.vrddhih.app.core.data.local.entity.HabitType
import com.vrddhih.app.core.data.local.entity.NumericDirection
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.CuteCategoryBadge
import com.vrddhih.app.core.ui.components.SystemPanel
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitEditorDialog(
    initialHabitWithConfigs: HabitWithConfigs? = null,
    categories: List<CategoryEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        habitId: Long?,
        name: String,
        type: HabitType,
        description: String,
        scheduleType: HabitScheduleType,
        scheduleInterval: Int,
        scheduleDaysMask: Int,
        startDate: LocalDate,
        categoryId: String?,
        xpPerCompletion: Int,
        numericTargetValue: Long,
        numericUnit: String,
        numericDirection: NumericDirection,
        numericIncrement: Long,
        timerTargetMinutes: Int,
        limitDailyTarget: Long,
        limitUnit: String,
        checklistItemTitles: List<String>,
    ) -> Unit,
    onDelete: ((Long) -> Unit)? = null,
) {
    val existingHabit = initialHabitWithConfigs?.habit
    val isEdit = existingHabit != null

    var name by remember { mutableStateOf(existingHabit?.name.orEmpty()) }
    var description by remember { mutableStateOf(existingHabit?.description.orEmpty()) }
    var type by remember { mutableStateOf(existingHabit?.type ?: HabitType.YES_NO) }
    var scheduleType by remember { mutableStateOf(existingHabit?.scheduleType ?: HabitScheduleType.DAILY) }
    var scheduleInterval by remember { mutableIntStateOf(existingHabit?.scheduleInterval ?: 1) }
    var scheduleDaysMask by remember { mutableIntStateOf(existingHabit?.scheduleDaysMask ?: HabitEntity.EVERY_DAY_MASK) }
    var categoryId by remember { mutableStateOf(existingHabit?.categoryId) }
    var xpPerCompletion by remember { mutableIntStateOf(existingHabit?.xpPerCompletion ?: 5) }

    // Numeric configs
    var numericTarget by remember {
        mutableLongStateOf(initialHabitWithConfigs?.numericConfig?.targetValue ?: 8L)
    }
    var numericUnit by remember {
        mutableStateOf(initialHabitWithConfigs?.numericConfig?.unit.orEmpty())
    }
    var numericDirection by remember {
        mutableStateOf(initialHabitWithConfigs?.numericConfig?.direction ?: NumericDirection.AT_LEAST)
    }
    var numericIncrement by remember {
        mutableLongStateOf(initialHabitWithConfigs?.numericConfig?.incrementValue ?: 1L)
    }

    // Timer configs
    var timerMinutes by remember {
        val totalSecs = initialHabitWithConfigs?.timerConfig?.dailyTargetSeconds
            ?: initialHabitWithConfigs?.timerConfig?.targetDurationSeconds ?: (25 * 60)
        mutableIntStateOf(maxOf(1, totalSecs / 60))
    }

    // Limit configs
    var limitDailyTarget by remember {
        mutableLongStateOf(initialHabitWithConfigs?.limitConfig?.dailyLimit ?: 2L)
    }
    var limitUnit by remember {
        mutableStateOf(initialHabitWithConfigs?.limitConfig?.unit.orEmpty())
    }

    // Checklist configs
    val checklistTitles = remember {
        mutableStateListOf<String>().apply {
            if (initialHabitWithConfigs?.checklistItems?.isNotEmpty() == true) {
                addAll(initialHabitWithConfigs.checklistItems.map { it.title })
            }
        }
    }
    var newChecklistTitle by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .padding(VrddhihSpacing.md)
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
        ) {
            SystemPanel(
                glow = true,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 680.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.habits_eyebrow).uppercase(),
                                style = VrddhihTextStyles.SystemLabel,
                                color = VrddhihColors.Ice,
                            )
                            Text(
                                text = stringResource(if (isEdit) R.string.habits_edit_title else R.string.habits_create_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = VrddhihColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.habits_cancel_action),
                                tint = VrddhihColors.TextMuted,
                            )
                        }
                    }

                    // Habit Name
                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xxs)) {
                        Text(
                            text = stringResource(R.string.habits_name_label).uppercase(),
                            style = VrddhihTextStyles.TagLabel,
                            color = VrddhihColors.TextSecondary,
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = null
                            },
                            placeholder = { Text(stringResource(R.string.habits_name_hint), color = VrddhihColors.TextMuted) },
                            singleLine = true,
                            isError = nameError != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VrddhihColors.Electric,
                                unfocusedBorderColor = VrddhihColors.Outline,
                                focusedTextColor = VrddhihColors.TextPrimary,
                                unfocusedTextColor = VrddhihColors.TextPrimary,
                                focusedContainerColor = VrddhihColors.Void,
                                unfocusedContainerColor = VrddhihColors.Void,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (nameError != null) {
                            Text(
                                text = nameError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = VrddhihColors.Danger,
                            )
                        }
                    }

                    // Description
                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xxs)) {
                        Text(
                            text = stringResource(R.string.habits_desc_label).uppercase(),
                            style = VrddhihTextStyles.TagLabel,
                            color = VrddhihColors.TextSecondary,
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text(stringResource(R.string.habits_desc_hint), color = VrddhihColors.TextMuted) },
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VrddhihColors.Electric,
                                unfocusedBorderColor = VrddhihColors.Outline,
                                focusedTextColor = VrddhihColors.TextPrimary,
                                unfocusedTextColor = VrddhihColors.TextPrimary,
                                focusedContainerColor = VrddhihColors.Void,
                                unfocusedContainerColor = VrddhihColors.Void,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    // Habit Type Segmented Chips
                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                        Text(
                            text = stringResource(R.string.habits_type_label).uppercase(),
                            style = VrddhihTextStyles.TagLabel,
                            color = VrddhihColors.TextSecondary,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                            verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                        ) {
                            HabitType.entries.forEach { habitType ->
                                val selected = type == habitType
                                val label = when (habitType) {
                                    HabitType.YES_NO -> stringResource(R.string.habits_type_yes_no)
                                    HabitType.NUMERIC -> stringResource(R.string.habits_type_numeric)
                                    HabitType.TIMER -> stringResource(R.string.habits_type_timer)
                                    HabitType.CHECKLIST -> stringResource(R.string.habits_type_checklist)
                                    HabitType.LIMIT -> stringResource(R.string.habits_type_limit)
                                }
                                FilterChip(
                                    selected = selected,
                                    onClick = { type = habitType },
                                    label = { Text(label, style = VrddhihTextStyles.TagLabel) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = VrddhihColors.Electric.copy(alpha = 0.2f),
                                        selectedLabelColor = VrddhihColors.Electric,
                                        containerColor = VrddhihColors.PanelRaised,
                                        labelColor = VrddhihColors.TextSecondary,
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = if (selected) VrddhihColors.Electric else VrddhihColors.Outline,
                                    ),
                                )
                            }
                        }
                    }

                    // Dynamic Section: Specific Config per Habit Type
                    when (type) {
                        HabitType.YES_NO -> { /* Standard yes/no check-in */ }
                        HabitType.NUMERIC -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(VrddhihRadii.md))
                                    .background(VrddhihColors.PanelRaised)
                                    .padding(VrddhihSpacing.sm),
                                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.habits_numeric_target_label).uppercase(),
                                            style = VrddhihTextStyles.TagLabel,
                                            color = VrddhihColors.Ice,
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                                        ) {
                                            IconButton(
                                                onClick = { if (numericTarget > 1) numericTarget-- },
                                                modifier = Modifier.size(36.dp),
                                            ) {
                                                Icon(Icons.Outlined.Remove, contentDescription = "-", tint = VrddhihColors.Ice)
                                            }
                                            Text(
                                                text = "$numericTarget",
                                                style = VrddhihTextStyles.NumericLarge,
                                                color = VrddhihColors.TextPrimary,
                                            )
                                            IconButton(
                                                onClick = { numericTarget++ },
                                                modifier = Modifier.size(36.dp),
                                            ) {
                                                Icon(Icons.Outlined.Add, contentDescription = "+", tint = VrddhihColors.Ice)
                                            }
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.habits_numeric_unit_label).uppercase(),
                                            style = VrddhihTextStyles.TagLabel,
                                            color = VrddhihColors.Ice,
                                        )
                                        OutlinedTextField(
                                            value = numericUnit,
                                            onValueChange = { numericUnit = it },
                                            placeholder = { Text(stringResource(R.string.habits_numeric_unit_hint), color = VrddhihColors.TextMuted) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = VrddhihColors.Electric,
                                                unfocusedBorderColor = VrddhihColors.Outline,
                                                focusedTextColor = VrddhihColors.TextPrimary,
                                                unfocusedTextColor = VrddhihColors.TextPrimary,
                                                focusedContainerColor = VrddhihColors.Void,
                                                unfocusedContainerColor = VrddhihColors.Void,
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                        HabitType.TIMER -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(VrddhihRadii.md))
                                    .background(VrddhihColors.PanelRaised)
                                    .padding(VrddhihSpacing.sm),
                                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                            ) {
                                Text(
                                    text = stringResource(R.string.habits_timer_target_label).uppercase(),
                                    style = VrddhihTextStyles.TagLabel,
                                    color = VrddhihColors.Ice,
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                                ) {
                                    listOf(10, 15, 25, 30, 45, 60).forEach { mins ->
                                        val selected = timerMinutes == mins
                                        FilterChip(
                                            selected = selected,
                                            onClick = { timerMinutes = mins },
                                            label = { Text("${mins}m", style = VrddhihTextStyles.TagLabel) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = VrddhihColors.Electric.copy(alpha = 0.2f),
                                                selectedLabelColor = VrddhihColors.Electric,
                                                containerColor = VrddhihColors.Void,
                                                labelColor = VrddhihColors.TextSecondary,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                        HabitType.CHECKLIST -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(VrddhihRadii.md))
                                    .background(VrddhihColors.PanelRaised)
                                    .padding(VrddhihSpacing.sm),
                                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                            ) {
                                Text(
                                    text = stringResource(R.string.habits_checklist_items_label).uppercase(),
                                    style = VrddhihTextStyles.TagLabel,
                                    color = VrddhihColors.Ice,
                                )

                                checklistTitles.forEachIndexed { index, itemTitle ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = "• $itemTitle",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = VrddhihColors.TextPrimary,
                                            modifier = Modifier.weight(1f),
                                        )
                                        IconButton(
                                            onClick = { checklistTitles.removeAt(index) },
                                            modifier = Modifier.size(32.dp),
                                        ) {
                                            Icon(Icons.Outlined.Close, contentDescription = "Remove", tint = VrddhihColors.Danger)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                                ) {
                                    OutlinedTextField(
                                        value = newChecklistTitle,
                                        onValueChange = { newChecklistTitle = it },
                                        placeholder = { Text(stringResource(R.string.habits_checklist_item_hint), color = VrddhihColors.TextMuted) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = VrddhihColors.Electric,
                                            unfocusedBorderColor = VrddhihColors.Outline,
                                            focusedTextColor = VrddhihColors.TextPrimary,
                                            unfocusedTextColor = VrddhihColors.TextPrimary,
                                            focusedContainerColor = VrddhihColors.Void,
                                            unfocusedContainerColor = VrddhihColors.Void,
                                        ),
                                        modifier = Modifier.weight(1f),
                                    )
                                    IconButton(
                                        onClick = {
                                            if (newChecklistTitle.isNotBlank()) {
                                                checklistTitles.add(newChecklistTitle.trim())
                                                newChecklistTitle = ""
                                            }
                                        },
                                        modifier = Modifier.size(44.dp),
                                    ) {
                                        Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.habits_checklist_add_item), tint = VrddhihColors.Electric)
                                    }
                                }
                            }
                        }
                        HabitType.LIMIT -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(VrddhihRadii.md))
                                    .background(VrddhihColors.PanelRaised)
                                    .padding(VrddhihSpacing.sm),
                                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.habits_limit_target_label).uppercase(),
                                            style = VrddhihTextStyles.TagLabel,
                                            color = VrddhihColors.Ice,
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                                        ) {
                                            IconButton(
                                                onClick = { if (limitDailyTarget > 1) limitDailyTarget-- },
                                                modifier = Modifier.size(36.dp),
                                            ) {
                                                Icon(Icons.Outlined.Remove, contentDescription = "-", tint = VrddhihColors.Ice)
                                            }
                                            Text(
                                                text = "$limitDailyTarget",
                                                style = VrddhihTextStyles.NumericLarge,
                                                color = VrddhihColors.TextPrimary,
                                            )
                                            IconButton(
                                                onClick = { limitDailyTarget++ },
                                                modifier = Modifier.size(36.dp),
                                            ) {
                                                Icon(Icons.Outlined.Add, contentDescription = "+", tint = VrddhihColors.Ice)
                                            }
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.habits_limit_unit_label).uppercase(),
                                            style = VrddhihTextStyles.TagLabel,
                                            color = VrddhihColors.Ice,
                                        )
                                        OutlinedTextField(
                                            value = limitUnit,
                                            onValueChange = { limitUnit = it },
                                            placeholder = { Text(stringResource(R.string.habits_limit_unit_hint), color = VrddhihColors.TextMuted) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = VrddhihColors.Electric,
                                                unfocusedBorderColor = VrddhihColors.Outline,
                                                focusedTextColor = VrddhihColors.TextPrimary,
                                                unfocusedTextColor = VrddhihColors.TextPrimary,
                                                focusedContainerColor = VrddhihColors.Void,
                                                unfocusedContainerColor = VrddhihColors.Void,
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Frequency & Scheduling
                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                        Text(
                            text = stringResource(R.string.habits_frequency_label).uppercase(),
                            style = VrddhihTextStyles.TagLabel,
                            color = VrddhihColors.TextSecondary,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                        ) {
                            FilterChip(
                                selected = scheduleType == HabitScheduleType.DAILY,
                                onClick = {
                                    scheduleType = HabitScheduleType.DAILY
                                    scheduleDaysMask = HabitEntity.EVERY_DAY_MASK
                                },
                                label = { Text(stringResource(R.string.habits_freq_daily), style = VrddhihTextStyles.TagLabel) },
                            )
                            FilterChip(
                                selected = scheduleType == HabitScheduleType.SELECTED_WEEKDAYS && scheduleDaysMask == 0b0011111,
                                onClick = {
                                    scheduleType = HabitScheduleType.SELECTED_WEEKDAYS
                                    scheduleDaysMask = HabitEntity.maskOf(
                                        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
                                    )
                                },
                                label = { Text(stringResource(R.string.habits_freq_weekdays), style = VrddhihTextStyles.TagLabel) },
                            )
                            FilterChip(
                                selected = scheduleType == HabitScheduleType.SELECTED_WEEKDAYS && scheduleDaysMask != 0b0011111,
                                onClick = { scheduleType = HabitScheduleType.SELECTED_WEEKDAYS },
                                label = { Text(stringResource(R.string.habits_freq_custom_days), style = VrddhihTextStyles.TagLabel) },
                            )
                            FilterChip(
                                selected = scheduleType == HabitScheduleType.EVERY_N_DAYS,
                                onClick = { scheduleType = HabitScheduleType.EVERY_N_DAYS },
                                label = { Text(stringResource(R.string.habits_freq_interval), style = VrddhihTextStyles.TagLabel) },
                            )
                        }

                        // Day of week selector when custom days
                        if (scheduleType == HabitScheduleType.SELECTED_WEEKDAYS) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                listOf(
                                    DayOfWeek.MONDAY to "M",
                                    DayOfWeek.TUESDAY to "T",
                                    DayOfWeek.WEDNESDAY to "W",
                                    DayOfWeek.THURSDAY to "T",
                                    DayOfWeek.FRIDAY to "F",
                                    DayOfWeek.SATURDAY to "S",
                                    DayOfWeek.SUNDAY to "S",
                                ).forEach { (day, label) ->
                                    val bit = 1 shl (day.value - 1)
                                    val isDaySelected = (scheduleDaysMask and bit) != 0
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isDaySelected) VrddhihColors.Electric else VrddhihColors.PanelRaised)
                                            .clickable {
                                                scheduleDaysMask = if (isDaySelected) {
                                                    scheduleDaysMask and bit.inv()
                                                } else {
                                                    scheduleDaysMask or bit
                                                }
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = label,
                                            style = VrddhihTextStyles.TagLabel,
                                            color = if (isDaySelected) VrddhihColors.Void else VrddhihColors.TextSecondary,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                        }

                        if (scheduleType == HabitScheduleType.EVERY_N_DAYS) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                            ) {
                                Text(
                                    text = stringResource(R.string.habits_freq_interval_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VrddhihColors.TextSecondary,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(
                                    onClick = { if (scheduleInterval > 1) scheduleInterval-- },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(Icons.Outlined.Remove, contentDescription = "-", tint = VrddhihColors.Ice)
                                }
                                Text(
                                    text = "$scheduleInterval",
                                    style = VrddhihTextStyles.NumericLarge,
                                    color = VrddhihColors.TextPrimary,
                                )
                                IconButton(
                                    onClick = { scheduleInterval++ },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(Icons.Outlined.Add, contentDescription = "+", tint = VrddhihColors.Ice)
                                }
                            }
                        }
                    }

                    // Category Selector
                    if (categories.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                            Text(
                                text = stringResource(R.string.habits_category_label).uppercase(),
                                style = VrddhihTextStyles.TagLabel,
                                color = VrddhihColors.TextSecondary,
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                            ) {
                                categories.forEach { cat ->
                                    val selected = categoryId == cat.id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(VrddhihRadii.md))
                                            .background(if (selected) VrddhihColors.PanelRaised else Color.Transparent)
                                            .border(
                                                BorderStroke(
                                                    1.dp,
                                                    if (selected) VrddhihColors.Electric else Color.Transparent,
                                                ),
                                                RoundedCornerShape(VrddhihRadii.md),
                                            )
                                            .clickable { categoryId = if (selected) null else cat.id }
                                            .padding(VrddhihSpacing.xs),
                                    ) {
                                        CuteCategoryBadge(category = cat, isSelected = selected)
                                    }
                                }
                            }
                        }
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = VrddhihSpacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (existingHabit != null && onDelete != null) {
                            IconButton(
                                onClick = {
                                    onDelete(existingHabit.id)
                                    onDismiss()
                                },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.habits_delete_action),
                                    tint = VrddhihColors.Danger,
                                )
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        TextButton(onClick = onDismiss) {
                            Text(
                                text = stringResource(R.string.habits_cancel_action).uppercase(),
                                style = VrddhihTextStyles.TagLabel,
                                color = VrddhihColors.TextMuted,
                            )
                        }

                        Button(
                            onClick = {
                                if (name.isBlank()) {
                                    nameError = "Habit name cannot be blank"
                                    return@Button
                                }
                                onSave(
                                    existingHabit?.id,
                                    name.trim(),
                                    type,
                                    description.trim(),
                                    scheduleType,
                                    scheduleInterval,
                                    scheduleDaysMask,
                                    existingHabit?.startDate ?: LocalDate.now(),
                                    categoryId,
                                    xpPerCompletion,
                                    numericTarget,
                                    numericUnit,
                                    numericDirection,
                                    numericIncrement,
                                    timerMinutes,
                                    limitDailyTarget,
                                    limitUnit,
                                    checklistTitles.toList(),
                                )
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VrddhihColors.Electric,
                                contentColor = VrddhihColors.Void,
                            ),
                            shape = RoundedCornerShape(VrddhihRadii.sm),
                        ) {
                            Text(
                                text = stringResource(R.string.habits_save_action).uppercase(),
                                style = VrddhihTextStyles.TagLabel,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}
