package com.vrddhih.app.features.habits

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrddhih.app.R
import com.vrddhih.app.core.data.habit.HabitDraft
import com.vrddhih.app.core.data.habit.HabitDraftError
import com.vrddhih.app.core.data.habit.HabitExtraGoalDraft
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.data.local.entity.HabitScheduleType
import com.vrddhih.app.core.data.local.entity.HabitType
import com.vrddhih.app.core.data.local.entity.NumericDirection
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.CategoryColors
import com.vrddhih.app.core.ui.components.CategorySelectionTile
import com.vrddhih.app.core.ui.components.CreateCustomCategoryTile
import com.vrddhih.app.core.ui.components.SystemPanel
import com.vrddhih.app.core.ui.components.VrddhihCalendarDialog
import com.vrddhih.app.features.categories.CategoryEditorDialog
import com.vrddhih.app.features.categories.CategoryEditorState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class HabitCalendarTarget { START_DATE, END_DATE }

/**
 * Dedicated full-screen Habit Creation and Editing screen.
 * Follows the clear, streamlined Solo Leveling hunter architecture:
 * 1. Step 1: Category selection (determines color, icon, and Dashboard representation)
 * 2. Habit Name & Optional Notes
 * 3. Step 2: Habit Type selection & Evaluation Condition (Yes/No, Numeric, Timer, Checklist)
 *    - Extra Goals section for Numeric, Timer, and Checklist
 * 4. Step 3: Habit Frequency ("How often do you want to do it?")
 *    - Every day, Specific days of the week, Specific days of the month, Every N days
 * 5. Start Date & optional End Date
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitEditorScreen(
    draft: HabitDraft?,
    isEdit: Boolean,
    errors: Set<HabitDraftError>,
    categories: List<CategoryEntity>,
    onDraftChange: (HabitDraft) -> Unit,
    onSave: () -> Unit,
    onSaveAndNew: () -> Unit = {},
    onDelete: () -> Unit = {},
    onCancel: () -> Unit,
    onCreateCustomCategory: suspend (String, String, Int) -> CategoryEntity?,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val coroutineScope = rememberCoroutineScope()
    var calendarTarget by remember { mutableStateOf<HabitCalendarTarget?>(null) }
    var showCustomCategoryDialog by remember { mutableStateOf(false) }
    var customCategoryState by remember {
        mutableStateOf(CategoryEditorState(isOpen = true, editingCategory = null, name = "", iconKey = "star", colorArgb = CategoryColors.DEFAULT_COLOR_ARGB))
    }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val initialDraft = remember(draft != null) { draft }
    val isDirty = remember(draft) {
        if (draft == null) false
        else if (isEdit) initialDraft != null && draft != initialDraft
        else draft.name.isNotBlank() || draft.description.isNotBlank() || draft.type != HabitType.YES_NO
    }

    val attemptBack: () -> Unit = {
        if (isDirty) {
            showDiscardDialog = true
        } else {
            onCancel()
        }
    }

    BackHandler(onBack = attemptBack)

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault()) }
    val selectedCategoryId = draft?.categoryId

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        color = VrddhihColors.Void,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = VrddhihSpacing.md, vertical = VrddhihSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = attemptBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.quest_cancel),
                            tint = VrddhihColors.TextPrimary,
                        )
                    }
                    Spacer(Modifier.width(VrddhihSpacing.xs))
                    Column {
                        Text(
                            text = stringResource(R.string.habits_eyebrow).uppercase(),
                            style = VrddhihTextStyles.SystemLabel,
                            color = VrddhihColors.Ice,
                        )
                        Text(
                            text = if (isEdit) stringResource(R.string.habits_edit_title) else stringResource(R.string.habits_create_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = VrddhihColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                TextButton(
                    onClick = onSave,
                    enabled = !isLoading && draft != null,
                ) {
                    Text(
                        text = stringResource(R.string.quest_save_confirm),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (!isLoading && draft != null) VrddhihColors.Electric else VrddhihColors.TextMuted,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (isLoading || draft == null) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = VrddhihColors.Electric,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = VrddhihDimens.ContentMaxWidth)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xl),
                    ) {
                        // ==========================================
                        // STEP 1: CATEGORY SELECTION (FIRST STEP)
                        // ==========================================
                        Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "1. CHOOSE CATEGORY *",
                                    style = VrddhihTextStyles.SystemLabel,
                                    color = if (HabitDraftError.CATEGORY_REQUIRED in errors) VrddhihColors.Danger else VrddhihColors.Ice,
                                    fontWeight = FontWeight.Bold,
                                )
                                val activeCategory = categories.find { it.id == selectedCategoryId }
                                if (activeCategory != null) {
                                    Text(
                                        text = activeCategory.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(activeCategory.colorArgb),
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                categories.forEach { category ->
                                    val isSelected = category.id == selectedCategoryId
                                    CategorySelectionTile(
                                        category = category,
                                        isSelected = isSelected,
                                        onClick = { onDraftChange(draft.copy(categoryId = category.id)) },
                                    )
                                }

                                CreateCustomCategoryTile(
                                    onClick = {
                                        customCategoryState = CategoryEditorState(
                                            isOpen = true,
                                            editingCategory = null,
                                            name = "",
                                            iconKey = "star",
                                            colorArgb = CategoryColors.DEFAULT_COLOR_ARGB,
                                        )
                                        showCustomCategoryDialog = true
                                    },
                                )
                            }

                            if (HabitDraftError.CATEGORY_REQUIRED in errors) {
                                Text(
                                    text = "Please select a category for this habit",
                                    color = VrddhihColors.Danger,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }

                        // ==========================================
                        // HABIT NAME & OPTIONAL DESCRIPTION
                        // ==========================================
                        Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md)) {
                            OutlinedTextField(
                                value = draft.name,
                                onValueChange = { onDraftChange(draft.copy(name = it)) },
                                label = { Text(stringResource(R.string.habits_name_label)) },
                                placeholder = {
                                    Text(text = "e.g. Read 20 pages, Morning Run…", color = VrddhihColors.TextMuted)
                                },
                                singleLine = true,
                                isError = HabitDraftError.NAME_REQUIRED in errors,
                                supportingText = {
                                    if (HabitDraftError.NAME_REQUIRED in errors) {
                                        Text(text = "Habit name is required", color = VrddhihColors.Danger)
                                    }
                                },
                                colors = outlinedColors(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            OutlinedTextField(
                                value = draft.description,
                                onValueChange = { onDraftChange(draft.copy(description = it)) },
                                label = { Text(stringResource(R.string.habits_desc_label)) },
                                placeholder = {
                                    Text(text = "Optional strategy, notes, or routine trigger…", color = VrddhihColors.TextMuted)
                                },
                                minLines = 2,
                                maxLines = 4,
                                colors = outlinedColors(),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        // ==========================================
                        // STEP 2: HABIT TYPE & EVALUATION PROCESS
                        // ==========================================
                        Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm)) {
                            Text(
                                text = "2. HABIT TYPE & EVALUATION",
                                style = VrddhihTextStyles.SystemLabel,
                                color = VrddhihColors.Ice,
                                fontWeight = FontWeight.Bold,
                            )

                            // 4 Habit Type Selection Cards
                            Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                                HabitTypeCard(
                                    title = "Yes / No",
                                    icon = Icons.Outlined.CheckCircle,
                                    isSelected = draft.type == HabitType.YES_NO,
                                    onClick = { onDraftChange(draft.copy(type = HabitType.YES_NO)) },
                                )

                                HabitTypeCard(
                                    title = "Numeric",
                                    icon = Icons.Outlined.Pin,
                                    isSelected = draft.type == HabitType.NUMERIC,
                                    onClick = { onDraftChange(draft.copy(type = HabitType.NUMERIC)) },
                                )

                                HabitTypeCard(
                                    title = "Timer",
                                    icon = Icons.Outlined.Timer,
                                    isSelected = draft.type == HabitType.TIMER,
                                    onClick = { onDraftChange(draft.copy(type = HabitType.TIMER)) },
                                )

                                HabitTypeCard(
                                    title = "Checklist",
                                    icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                                    isSelected = draft.type == HabitType.CHECKLIST,
                                    onClick = { onDraftChange(draft.copy(type = HabitType.CHECKLIST)) },
                                )
                            }

                            // Dynamic Configuration per Type
                            AnimatedVisibility(
                                visible = draft.type != HabitType.YES_NO,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically(),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = VrddhihSpacing.sm),
                                    verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
                                ) {
                                    when (draft.type) {
                                        HabitType.NUMERIC -> {
                                            NumericConfigurationSection(
                                                draft = draft,
                                                onDraftChange = onDraftChange,
                                            )
                                        }
                                        HabitType.TIMER -> {
                                            TimerConfigurationSection(
                                                draft = draft,
                                                onDraftChange = onDraftChange,
                                            )
                                        }
                                        HabitType.CHECKLIST -> {
                                            ChecklistConfigurationSection(
                                                draft = draft,
                                                errors = errors,
                                                onDraftChange = onDraftChange,
                                            )
                                        }
                                        else -> Unit
                                    }
                                }
                            }
                        }

                        // ==========================================
                        // STEP 3: HABIT FREQUENCY
                        // ==========================================
                        Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm)) {
                            Text(
                                text = "3. HOW OFTEN DO YOU WANT TO DO IT?",
                                style = VrddhihTextStyles.SystemLabel,
                                color = VrddhihColors.Ice,
                                fontWeight = FontWeight.Bold,
                            )

                            // 4 Frequency Options
                            Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                                FrequencyOptionCard(
                                    title = "Every day",
                                    isSelected = draft.scheduleType == HabitScheduleType.DAILY,
                                    onClick = { onDraftChange(draft.copy(scheduleType = HabitScheduleType.DAILY)) },
                                )

                                FrequencyOptionCard(
                                    title = "Specific days of the week",
                                    isSelected = draft.scheduleType == HabitScheduleType.SELECTED_WEEKDAYS,
                                    onClick = { onDraftChange(draft.copy(scheduleType = HabitScheduleType.SELECTED_WEEKDAYS)) },
                                )

                                AnimatedVisibility(visible = draft.scheduleType == HabitScheduleType.SELECTED_WEEKDAYS) {
                                    WeekdaysPickerRow(
                                        selectedWeekdays = draft.weekdays,
                                        onToggleDay = { day ->
                                            val updated = if (day in draft.weekdays) draft.weekdays - day else draft.weekdays + day
                                             onDraftChange(draft.copy(weekdays = updated))
                                        },
                                        hasError = HabitDraftError.NO_WEEKDAYS in errors,
                                    )
                                }

                                FrequencyOptionCard(
                                    title = "Specific days of the month",
                                    isSelected = draft.scheduleType == HabitScheduleType.MONTHLY_DAYS,
                                    onClick = { onDraftChange(draft.copy(scheduleType = HabitScheduleType.MONTHLY_DAYS)) },
                                )

                                AnimatedVisibility(visible = draft.scheduleType == HabitScheduleType.MONTHLY_DAYS) {
                                    MonthDaysPickerGrid(
                                        selectedDays = draft.monthDays,
                                        onToggleDay = { day ->
                                            val updated = if (day in draft.monthDays) draft.monthDays - day else draft.monthDays + day
                                            onDraftChange(draft.copy(monthDays = updated))
                                        },
                                        hasError = HabitDraftError.NO_MONTH_DAYS in errors,
                                    )
                                }

                                FrequencyOptionCard(
                                    title = "Every N days",
                                    isSelected = draft.scheduleType == HabitScheduleType.EVERY_N_DAYS,
                                    onClick = { onDraftChange(draft.copy(scheduleType = HabitScheduleType.EVERY_N_DAYS)) },
                                )

                                AnimatedVisibility(visible = draft.scheduleType == HabitScheduleType.EVERY_N_DAYS) {
                                    IntervalStepperRow(
                                        intervalText = draft.intervalText,
                                        onIntervalChange = { onDraftChange(draft.copy(intervalText = it)) },
                                    )
                                }
                            }
                        }

                        // ==========================================
                        // DATES: START DATE & OPTIONAL END DATE
                        // ==========================================
                        Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                            Text(
                                text = "START & END DATES",
                                style = VrddhihTextStyles.SystemLabel,
                                color = VrddhihColors.Ice,
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(VrddhihRadii.sm))
                                    .background(VrddhihColors.Panel)
                                    .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(VrddhihRadii.sm))
                                    .clickable { calendarTarget = HabitCalendarTarget.START_DATE }
                                    .padding(horizontal = VrddhihSpacing.md, vertical = VrddhihSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = VrddhihColors.Ice,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(VrddhihSpacing.sm))
                                    Text(
                                        text = "Start: ${dateFormatter.format(draft.startDate)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = VrddhihColors.TextPrimary,
                                    )
                                }
                            }

                            // Optional End Date
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(VrddhihRadii.sm))
                                    .background(VrddhihColors.Panel)
                                    .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(VrddhihRadii.sm))
                                    .clickable { calendarTarget = HabitCalendarTarget.END_DATE }
                                    .padding(horizontal = VrddhihSpacing.md, vertical = VrddhihSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = if (draft.endDate != null) VrddhihColors.Ice else VrddhihColors.TextMuted,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(VrddhihSpacing.sm))
                                    Text(
                                        text = draft.endDate?.let { "End: ${dateFormatter.format(it)}" } ?: "No end date (indefinite)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (draft.endDate != null) VrddhihColors.TextPrimary else VrddhihColors.TextMuted,
                                    )
                                }

                                if (draft.endDate != null) {
                                    IconButton(
                                        onClick = { onDraftChange(draft.copy(endDate = null)) },
                                        modifier = Modifier.size(28.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Clear end date",
                                            tint = VrddhihColors.TextMuted,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom Action Buttons
                        if (!isEdit) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
                            ) {
                                Button(
                                    onClick = onSave,
                                    enabled = !isLoading,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VrddhihColors.Electric,
                                        contentColor = VrddhihColors.Void,
                                    ),
                                    shape = RoundedCornerShape(VrddhihRadii.sm),
                                ) {
                                    Text(
                                        text = "SAVE HABIT",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }

                                androidx.compose.material3.OutlinedButton(
                                    onClick = onSaveAndNew,
                                    enabled = !isLoading,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                        contentColor = VrddhihColors.Ice,
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, VrddhihColors.Ice),
                                    shape = RoundedCornerShape(VrddhihRadii.sm),
                                ) {
                                    Text(
                                        text = "SAVE & NEW",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
                            ) {
                                Button(
                                    onClick = onSave,
                                    enabled = !isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VrddhihColors.Electric,
                                        contentColor = VrddhihColors.Void,
                                    ),
                                    shape = RoundedCornerShape(VrddhihRadii.sm),
                                ) {
                                    Text(
                                        text = "SAVE CHANGES",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }

                                androidx.compose.material3.OutlinedButton(
                                    onClick = { showDeleteDialog = true },
                                    enabled = !isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                        contentColor = VrddhihColors.Danger,
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, VrddhihColors.Danger),
                                    shape = RoundedCornerShape(VrddhihRadii.sm),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete Habit",
                                        tint = VrddhihColors.Danger,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(Modifier.width(VrddhihSpacing.sm))
                                    Text(
                                        text = "DELETE HABIT",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = VrddhihColors.Danger,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(VrddhihSpacing.xxl))
                    }
                }
            }
        }
    }

    // Calendar Picker Dialog
    val currentTarget = calendarTarget
    if (currentTarget != null && draft != null) {
        val initialSelected = when (currentTarget) {
            HabitCalendarTarget.START_DATE -> draft.startDate
            HabitCalendarTarget.END_DATE -> draft.endDate ?: draft.startDate
        }
        VrddhihCalendarDialog(
            selectedDate = initialSelected,
            today = LocalDate.now(),
            onDateSelected = { picked ->
                when (currentTarget) {
                    HabitCalendarTarget.START_DATE -> onDraftChange(draft.copy(startDate = picked))
                    HabitCalendarTarget.END_DATE -> onDraftChange(draft.copy(endDate = picked))
                }
                calendarTarget = null
            },
            onTodaySelected = {
                val today = LocalDate.now()
                when (currentTarget) {
                    HabitCalendarTarget.START_DATE -> onDraftChange(draft.copy(startDate = today))
                    HabitCalendarTarget.END_DATE -> onDraftChange(draft.copy(endDate = today))
                }
                calendarTarget = null
            },
            onDismiss = { calendarTarget = null },
        )
    }

    // Custom Category Dialog
    if (showCustomCategoryDialog) {
        CategoryEditorDialog(
            state = customCategoryState,
            onSetName = { customCategoryState = customCategoryState.copy(name = it) },
            onSetIconKey = { customCategoryState = customCategoryState.copy(iconKey = it) },
            onSetColor = { customCategoryState = customCategoryState.copy(colorArgb = it) },
            onDismiss = { showCustomCategoryDialog = false },
            onSave = {
                val trimmedName = customCategoryState.name.trim()
                if (trimmedName.isNotBlank()) {
                    coroutineScope.launch {
                        val created = onCreateCustomCategory(
                            trimmedName,
                            customCategoryState.iconKey,
                            customCategoryState.colorArgb,
                        )
                        if (created != null && draft != null) {
                            onDraftChange(draft.copy(categoryId = created.id))
                        }
                        showCustomCategoryDialog = false
                    }
                }
            },
        )
    }

    // Discard Unsaved Changes Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor = VrddhihColors.PanelRaised,
            title = {
                Text(
                    text = stringResource(R.string.quest_discard_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = VrddhihColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.quest_discard_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VrddhihColors.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onCancel()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.quest_discard),
                        color = VrddhihColors.Danger,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(text = stringResource(R.string.quest_keep_editing), color = VrddhihColors.TextSecondary)
                }
            },
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = VrddhihColors.PanelRaised,
            title = {
                Text(
                    text = "DELETE HABIT",
                    style = MaterialTheme.typography.titleMedium,
                    color = VrddhihColors.Danger,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${draft?.name ?: "this habit"}\"? All completion records for this habit will be permanently deleted. Quests, goals and other records will not be affected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VrddhihColors.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                ) {
                    Text(
                        text = "DELETE",
                        color = VrddhihColors.Danger,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "CANCEL", color = VrddhihColors.TextSecondary)
                }
            },
        )
    }
}

// ==========================================
// HABIT TYPE & CONFIGURATION COMPONENTS
// ==========================================

@Composable
private fun HabitTypeCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(VrddhihRadii.sm)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isSelected) VrddhihColors.Electric.copy(alpha = 0.12f) else VrddhihColors.Panel)
            .border(
                1.dp,
                if (isSelected) VrddhihColors.Electric else VrddhihColors.OutlineBright,
                shape,
            )
            .clickable(onClick = onClick)
            .padding(VrddhihSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSelected) VrddhihColors.Electric else VrddhihColors.PanelRaised),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) VrddhihColors.Void else VrddhihColors.Ice,
                modifier = Modifier.size(20.dp),
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) VrddhihColors.Electric else VrddhihColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ConditionSelectorChips(
    selectedCondition: NumericDirection,
    onSelectCondition: (NumericDirection) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xxs)) {
        Text(
            text = "EVALUATION CONDITION",
            style = VrddhihTextStyles.TagLabel,
            color = VrddhihColors.TextSecondary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
        ) {
            listOf(
                NumericDirection.AT_LEAST to "At least",
                NumericDirection.AT_MOST to "Less than",
                NumericDirection.EXACTLY to "Exactly",
                NumericDirection.ANY_VALUE to "Any value",
            ).forEach { (dir, label) ->
                val isSelected = selectedCondition == dir
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(VrddhihRadii.xs))
                        .background(if (isSelected) VrddhihColors.Electric else VrddhihColors.PanelRaised)
                        .border(
                            1.dp,
                            if (isSelected) VrddhihColors.Ice else VrddhihColors.Outline,
                            RoundedCornerShape(VrddhihRadii.xs),
                        )
                        .clickable { onSelectCondition(dir) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) VrddhihColors.Void else VrddhihColors.TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun NumericConfigurationSection(
    draft: HabitDraft,
    onDraftChange: (HabitDraft) -> Unit,
) {
    SystemPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm)) {
            Text(
                text = "NUMERIC GOAL CONFIGURATION",
                style = VrddhihTextStyles.SystemLabel,
                color = VrddhihColors.Ice,
            )

            OutlinedTextField(
                value = draft.numericTarget,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(6)
                    onDraftChange(draft.copy(numericTarget = digits))
                },
                label = { Text("Goal Value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = outlinedColors(),
                modifier = Modifier.fillMaxWidth(),
            )

            ConditionSelectorChips(
                selectedCondition = draft.numericCondition,
                onSelectCondition = { onDraftChange(draft.copy(numericCondition = it)) },
            )
        }
    }
}

@Composable
private fun TimerConfigurationSection(
    draft: HabitDraft,
    onDraftChange: (HabitDraft) -> Unit,
) {
    SystemPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm)) {
            Text(
                text = "TIMER DURATION",
                style = VrddhihTextStyles.SystemLabel,
                color = VrddhihColors.Ice,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = draft.timerHours,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(2)
                        onDraftChange(draft.copy(timerHours = digits))
                    },
                    label = { Text("Hours") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = outlinedColors(),
                    modifier = Modifier.weight(1f),
                )

                Text(
                    text = "h",
                    style = MaterialTheme.typography.titleMedium,
                    color = VrddhihColors.TextSecondary,
                )

                OutlinedTextField(
                    value = draft.timerMinutes,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(2)
                        onDraftChange(draft.copy(timerMinutes = digits))
                    },
                    label = { Text("Minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = outlinedColors(),
                    modifier = Modifier.weight(1f),
                )

                Text(
                    text = "m",
                    style = MaterialTheme.typography.titleMedium,
                    color = VrddhihColors.TextSecondary,
                )
            }

            ConditionSelectorChips(
                selectedCondition = draft.timerCondition,
                onSelectCondition = { onDraftChange(draft.copy(timerCondition = it)) },
            )
        }
    }
}

@Composable
private fun ChecklistConfigurationSection(
    draft: HabitDraft,
    errors: Set<HabitDraftError>,
    onDraftChange: (HabitDraft) -> Unit,
) {
    SystemPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "CHECKLIST ITEMS",
                    style = VrddhihTextStyles.SystemLabel,
                    color = VrddhihColors.Ice,
                )
                TextButton(
                    onClick = { onDraftChange(draft.copy(checklistItems = draft.checklistItems + "")) },
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = VrddhihColors.Electric, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Item", color = VrddhihColors.Electric, style = MaterialTheme.typography.labelMedium)
                }
            }

            draft.checklistItems.forEachIndexed { index, itemText ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                ) {
                    OutlinedTextField(
                        value = itemText,
                        onValueChange = { updated ->
                            val list = draft.checklistItems.toMutableList()
                            list[index] = updated
                            onDraftChange(draft.copy(checklistItems = list))
                        },
                        placeholder = { Text("Item ${index + 1}…", color = VrddhihColors.TextMuted) },
                        singleLine = true,
                        colors = outlinedColors(),
                        modifier = Modifier.weight(1f),
                    )

                    if (draft.checklistItems.size > 1) {
                        IconButton(
                            onClick = {
                                val list = draft.checklistItems.toMutableList()
                                list.removeAt(index)
                                onDraftChange(draft.copy(checklistItems = list))
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = "Remove item", tint = VrddhihColors.TextMuted)
                        }
                    }
                }
            }

            if (HabitDraftError.CHECKLIST_EMPTY in errors) {
                Text(
                    text = "Add at least one checklist item",
                    color = VrddhihColors.Danger,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

// ==========================================
// FREQUENCY SELECTION COMPONENTS
// ==========================================

@Composable
private fun FrequencyOptionCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(VrddhihRadii.sm)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isSelected) VrddhihColors.Electric.copy(alpha = 0.12f) else VrddhihColors.Panel)
            .border(
                1.dp,
                if (isSelected) VrddhihColors.Electric else VrddhihColors.OutlineBright,
                shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = VrddhihSpacing.md, vertical = VrddhihSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) VrddhihColors.Electric else VrddhihColors.TextPrimary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )

        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isSelected) VrddhihColors.Electric else Color.Transparent)
                .border(1.5.dp, if (isSelected) VrddhihColors.Ice else VrddhihColors.OutlineBright, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = VrddhihColors.Void,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun WeekdaysPickerRow(
    selectedWeekdays: Set<DayOfWeek>,
    onToggleDay: (DayOfWeek) -> Unit,
    hasError: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = VrddhihSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
    ) {
        Text(
            text = "SELECT DAYS:",
            style = VrddhihTextStyles.TagLabel,
            color = if (hasError) VrddhihColors.Danger else VrddhihColors.TextMuted,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            DayOfWeek.entries.forEach { dayOfWeek ->
                val isSelected = dayOfWeek in selectedWeekdays
                val label = dayOfWeek.name.take(3)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) VrddhihColors.Electric else VrddhihColors.PanelRaised)
                        .border(
                            1.dp,
                            if (isSelected) VrddhihColors.Ice else VrddhihColors.Outline,
                            CircleShape,
                        )
                        .clickable { onToggleDay(dayOfWeek) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) VrddhihColors.Void else VrddhihColors.TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
        if (hasError) {
            Text(
                text = "Select at least one day of the week",
                color = VrddhihColors.Danger,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonthDaysPickerGrid(
    selectedDays: Set<Int>,
    onToggleDay: (Int) -> Unit,
    hasError: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = VrddhihSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "SELECT DAYS OF MONTH (1–31):",
                style = VrddhihTextStyles.TagLabel,
                color = if (hasError) VrddhihColors.Danger else VrddhihColors.TextMuted,
            )
            Text(
                text = "${selectedDays.size} selected",
                style = VrddhihTextStyles.NumericSmall,
                color = VrddhihColors.Ice,
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            (1..31).forEach { day ->
                val isSelected = day in selectedDays
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) VrddhihColors.Electric else VrddhihColors.PanelRaised)
                        .border(
                            1.dp,
                            if (isSelected) VrddhihColors.Ice else VrddhihColors.Outline,
                            RoundedCornerShape(8.dp),
                        )
                        .clickable { onToggleDay(day) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = day.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) VrddhihColors.Void else VrddhihColors.TextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }

        if (hasError) {
            Text(
                text = "Select at least one day of the month",
                color = VrddhihColors.Danger,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun IntervalStepperRow(
    intervalText: String,
    onIntervalChange: (String) -> Unit,
) {
    val currInterval = intervalText.toIntOrNull() ?: 1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = VrddhihSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
    ) {
        Text(
            text = "Repeat every",
            style = MaterialTheme.typography.bodyMedium,
            color = VrddhihColors.TextSecondary,
        )

        IconButton(
            onClick = {
                if (currInterval > 1) {
                    onIntervalChange((currInterval - 1).toString())
                }
            },
            enabled = currInterval > 1,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(VrddhihRadii.sm))
                .background(VrddhihColors.PanelRaised)
                .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(VrddhihRadii.sm)),
        ) {
            Icon(Icons.Outlined.Remove, contentDescription = "Decrease", tint = if (currInterval > 1) VrddhihColors.TextPrimary else VrddhihColors.TextMuted)
        }

        BasicTextField(
            value = intervalText,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }.take(3)
                onIntervalChange(filtered)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = VrddhihColors.TextPrimary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            ),
            cursorBrush = SolidColor(VrddhihColors.Electric),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(VrddhihRadii.sm))
                        .background(VrddhihColors.PanelRaised)
                        .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(VrddhihRadii.sm)),
                    contentAlignment = Alignment.Center,
                ) {
                    innerTextField()
                }
            },
        )

        IconButton(
            onClick = {
                if (currInterval < 365) {
                    onIntervalChange((currInterval + 1).toString())
                }
            },
            enabled = currInterval < 365,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(VrddhihRadii.sm))
                .background(VrddhihColors.PanelRaised)
                .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(VrddhihRadii.sm)),
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Increase", tint = if (currInterval < 365) VrddhihColors.TextPrimary else VrddhihColors.TextMuted)
        }

        Text(
            text = if (currInterval == 1) "day" else "days",
            style = MaterialTheme.typography.bodyMedium,
            color = VrddhihColors.TextPrimary,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = VrddhihColors.TextPrimary,
    unfocusedTextColor = VrddhihColors.TextPrimary,
    focusedContainerColor = VrddhihColors.PanelRaised,
    unfocusedContainerColor = VrddhihColors.Panel,
    cursorColor = VrddhihColors.Electric,
    focusedBorderColor = VrddhihColors.Electric,
    unfocusedBorderColor = VrddhihColors.OutlineBright,
    focusedLabelColor = VrddhihColors.Ice,
    unfocusedLabelColor = VrddhihColors.TextSecondary,
    focusedPlaceholderColor = VrddhihColors.TextMuted,
    unfocusedPlaceholderColor = VrddhihColors.TextMuted,
    errorBorderColor = VrddhihColors.Danger,
    errorLabelColor = VrddhihColors.Danger,
    errorTextColor = VrddhihColors.TextPrimary,
)
