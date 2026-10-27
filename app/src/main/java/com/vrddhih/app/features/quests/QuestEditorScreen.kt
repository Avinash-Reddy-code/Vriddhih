package com.vrddhih.app.features.quests

import android.text.format.DateFormat
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vrddhih.app.R
import com.vrddhih.app.core.data.local.entity.CATEGORY_OTHER_ID
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.data.local.entity.QuestRecurrence
import com.vrddhih.app.core.data.local.entity.TaskPriority
import com.vrddhih.app.core.data.quest.QuestDraft
import com.vrddhih.app.core.data.quest.QuestDraftError
import com.vrddhih.app.core.data.quest.QuestScope
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.CategorySelectionTile
import com.vrddhih.app.core.ui.components.CreateCustomCategoryTile
import com.vrddhih.app.core.ui.components.SystemPanel
import com.vrddhih.app.core.ui.components.VrddhihCalendarDialog
import com.vrddhih.app.features.categories.CategoryEditorDialog
import com.vrddhih.app.features.categories.CategoryEditorState
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class CalendarPickerTarget { SCHEDULED_DATE, RECURRENCE_END_DATE }

/**
 * Dedicated full-screen Quest Creation and Editing screen.
 * Follows the clear, streamlined layout:
 * 1. Title
 * 2. Optional description
 * 3. Category selector
 * 4. Priority selector
 * 5. Scheduled date
 * 6. Recurrence settings
 * 7. Optional recurrence end date
 * 8. Optional due time
 * 9. Primary Create Quest / Save action
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuestEditorScreen(
    draft: QuestDraft?,
    isEdit: Boolean,
    scope: QuestScope,
    errors: Set<QuestDraftError>,
    categories: List<CategoryEntity>,
    onDraftChange: (QuestDraft) -> Unit,
    onScopeChange: (QuestScope) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onCreateCustomCategory: suspend (String, String, Int) -> CategoryEntity?,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val context = LocalContext.current
    val is24h = DateFormat.is24HourFormat(context)
    val coroutineScope = rememberCoroutineScope()

    var calendarTarget by remember { mutableStateOf<CalendarPickerTarget?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCustomCategoryDialog by remember { mutableStateOf(false) }
    var customCategoryState by remember {
        mutableStateOf(CategoryEditorState(isOpen = true, editingCategory = null, name = "", iconKey = "star", colorArgb = 0xFF7CD3FF.toInt()))
    }

    var showDiscardDialog by remember { mutableStateOf(false) }

    // Initial draft snapshot to detect unsaved changes
    val initialDraft = remember(draft != null) { draft }
    val isDirty = remember(draft) {
        if (draft == null) {
            false
        } else if (isEdit) {
            initialDraft != null && draft != initialDraft
        } else {
            draft.title.isNotBlank() || draft.description.isNotBlank() || draft.recurrence != QuestRecurrence.NONE || draft.dueTime != null
        }
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
    val timeFormatter = remember {
        if (is24h) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("h:mm a")
    }

    val selectedCategoryId = draft?.categoryId ?: CATEGORY_OTHER_ID

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
                            text = stringResource(R.string.quests_eyebrow).uppercase(),
                            style = VrddhihTextStyles.SystemLabel,
                            color = VrddhihColors.Ice,
                        )
                        Text(
                            text = stringResource(if (isEdit) R.string.quest_edit_title else R.string.quest_create_title),
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
                // Scrollable form content
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
                        verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.lg),
                    ) {
                    // 1. QUEST TITLE
                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                        OutlinedTextField(
                            value = draft.title,
                            onValueChange = { onDraftChange(draft.copy(title = it)) },
                            label = { Text(stringResource(R.string.quest_field_title)) },
                            placeholder = {
                                Text(
                                    text = "What is your quest?",
                                    color = VrddhihColors.TextMuted,
                                )
                            },
                            singleLine = true,
                            isError = QuestDraftError.TITLE_REQUIRED in errors || QuestDraftError.TITLE_TOO_LONG in errors,
                            supportingText = {
                                when {
                                    QuestDraftError.TITLE_REQUIRED in errors -> Text(
                                        text = stringResource(R.string.quest_error_title_required),
                                        color = VrddhihColors.Danger,
                                    )
                                    QuestDraftError.TITLE_TOO_LONG in errors -> Text(
                                        text = stringResource(R.string.quest_error_title_too_long),
                                        color = VrddhihColors.Danger,
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
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
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    // 2. OPTIONAL DESCRIPTION
                    OutlinedTextField(
                        value = draft.description,
                        onValueChange = { onDraftChange(draft.copy(description = it)) },
                        label = { Text(stringResource(R.string.quest_field_notes)) },
                        placeholder = {
                            Text(
                                text = "Optional notes, strategy, or details…",
                                color = VrddhihColors.TextMuted,
                            )
                        },
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
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
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // 3. CATEGORY SELECTOR
                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(R.string.quest_field_category).uppercase(),
                                style = VrddhihTextStyles.SystemLabel,
                                color = VrddhihColors.Ice,
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

                        // Compact horizontal scrolling row of cute royal category tiles + "+ Custom"
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
                                        colorArgb = 0xFF7CD3FF.toInt(),
                                    )
                                    showCustomCategoryDialog = true
                                },
                            )
                        }
                    }

                    // 4. PRIORITY SELECTOR
                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                        Text(
                            text = stringResource(R.string.quest_field_priority).uppercase(),
                            style = VrddhihTextStyles.SystemLabel,
                            color = VrddhihColors.Ice,
                        )

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val isNarrow = maxWidth < 340.dp
                            if (isNarrow) {
                                Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                                    ) {
                                        PriorityButton(
                                            priority = TaskPriority.LOW,
                                            isSelected = draft.priority == TaskPriority.LOW,
                                            modifier = Modifier.weight(1f),
                                            onClick = { onDraftChange(draft.copy(priority = TaskPriority.LOW)) },
                                        )
                                        PriorityButton(
                                            priority = TaskPriority.MEDIUM,
                                            isSelected = draft.priority == TaskPriority.MEDIUM,
                                            modifier = Modifier.weight(1f),
                                            onClick = { onDraftChange(draft.copy(priority = TaskPriority.MEDIUM)) },
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                                    ) {
                                        PriorityButton(
                                            priority = TaskPriority.HIGH,
                                            isSelected = draft.priority == TaskPriority.HIGH,
                                            modifier = Modifier.weight(1f),
                                            onClick = { onDraftChange(draft.copy(priority = TaskPriority.HIGH)) },
                                        )
                                        PriorityButton(
                                            priority = TaskPriority.CRITICAL,
                                            isSelected = draft.priority == TaskPriority.CRITICAL,
                                            modifier = Modifier.weight(1f),
                                            onClick = { onDraftChange(draft.copy(priority = TaskPriority.CRITICAL)) },
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                                ) {
                                    listOf(
                                        TaskPriority.LOW,
                                        TaskPriority.MEDIUM,
                                        TaskPriority.HIGH,
                                        TaskPriority.CRITICAL,
                                    ).forEach { priority ->
                                        PriorityButton(
                                            priority = priority,
                                            isSelected = draft.priority == priority,
                                            modifier = Modifier.weight(1f),
                                            onClick = { onDraftChange(draft.copy(priority = priority)) },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. SCHEDULED DATE
                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                        Text(
                            text = stringResource(R.string.quest_field_date).uppercase(),
                            style = VrddhihTextStyles.SystemLabel,
                            color = VrddhihColors.Ice,
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(VrddhihRadii.sm))
                                .background(VrddhihColors.Panel)
                                .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(VrddhihRadii.sm))
                                .clickable { calendarTarget = CalendarPickerTarget.SCHEDULED_DATE }
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
                                    text = dateFormatter.format(draft.scheduledDate),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = VrddhihColors.TextPrimary,
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                                val today = LocalDate.now()
                                if (draft.scheduledDate != today) {
                                    Text(
                                        text = "Today",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VrddhihColors.Electric,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(VrddhihColors.Electric.copy(alpha = 0.15f))
                                            .clickable { onDraftChange(draft.copy(scheduledDate = today)) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                                val tomorrow = today.plusDays(1)
                                if (draft.scheduledDate != tomorrow) {
                                    Text(
                                        text = "Tomorrow",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VrddhihColors.Ice,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(VrddhihColors.Ice.copy(alpha = 0.15f))
                                            .clickable { onDraftChange(draft.copy(scheduledDate = tomorrow)) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                        }
                    }

                    // 6. RECURRENCE SETTINGS
                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                        Text(
                            text = stringResource(R.string.quest_field_recurrence).uppercase(),
                            style = VrddhihTextStyles.SystemLabel,
                            color = VrddhihColors.Ice,
                        )

                        var recurrenceMenuExpanded by remember { mutableStateOf(false) }
                        val recurrenceLabel = when (draft.recurrence) {
                            QuestRecurrence.NONE -> stringResource(R.string.quest_recurrence_none)
                            QuestRecurrence.DAILY -> stringResource(R.string.quest_recurrence_daily)
                            QuestRecurrence.WEEKDAYS -> stringResource(R.string.quest_recurrence_weekdays)
                            QuestRecurrence.WEEKLY -> stringResource(R.string.quest_recurrence_weekly)
                            QuestRecurrence.MONTHLY -> stringResource(R.string.quest_recurrence_monthly)
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(VrddhihRadii.sm))
                                    .background(VrddhihColors.Panel)
                                    .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(VrddhihRadii.sm))
                                    .clickable { recurrenceMenuExpanded = true }
                                    .padding(horizontal = VrddhihSpacing.md, vertical = VrddhihSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = recurrenceLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = VrddhihColors.TextPrimary,
                                )
                                Icon(
                                    imageVector = Icons.Outlined.ExpandMore,
                                    contentDescription = null,
                                    tint = VrddhihColors.TextSecondary,
                                )
                            }

                            DropdownMenu(
                                expanded = recurrenceMenuExpanded,
                                onDismissRequest = { recurrenceMenuExpanded = false },
                                modifier = Modifier.background(VrddhihColors.PanelRaised),
                            ) {
                                QuestRecurrence.entries.forEach { r ->
                                    val itemLabel = when (r) {
                                        QuestRecurrence.NONE -> stringResource(R.string.quest_recurrence_none)
                                        QuestRecurrence.DAILY -> stringResource(R.string.quest_recurrence_daily)
                                        QuestRecurrence.WEEKDAYS -> stringResource(R.string.quest_recurrence_weekdays)
                                        QuestRecurrence.WEEKLY -> stringResource(R.string.quest_recurrence_weekly)
                                        QuestRecurrence.MONTHLY -> stringResource(R.string.quest_recurrence_monthly)
                                    }
                                    DropdownMenuItem(
                                        text = { Text(itemLabel, color = VrddhihColors.TextPrimary) },
                                        onClick = {
                                            recurrenceMenuExpanded = false
                                            val preservedEnd = draft.endDate ?: draft.deadline
                                            onDraftChange(
                                                draft.copy(
                                                    recurrence = r,
                                                    endDate = preservedEnd,
                                                    deadline = if (r == QuestRecurrence.NONE) preservedEnd else null,
                                                )
                                            )
                                        },
                                    )
                                }
                            }
                        }

                        // Recurrence details when not NONE
                        AnimatedVisibility(
                            visible = draft.recurrence != QuestRecurrence.NONE,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = VrddhihSpacing.xs),
                                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                            ) {
                                // Repeat interval stepper
                                val currInterval = draft.parsedInterval ?: 1
                                val unitLabel = when (draft.recurrence) {
                                    QuestRecurrence.DAILY -> if (currInterval == 1) "day" else "days"
                                    QuestRecurrence.WEEKDAYS, QuestRecurrence.WEEKLY -> if (currInterval == 1) "week" else "weeks"
                                    QuestRecurrence.MONTHLY -> if (currInterval == 1) "month" else "months"
                                    QuestRecurrence.NONE -> ""
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                                    Text(
                                        text = "Repeat every",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = VrddhihColors.TextSecondary,
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val curr = draft.parsedInterval ?: 1
                                                if (curr > 1) {
                                                    onDraftChange(draft.copy(intervalText = (curr - 1).toString()))
                                                }
                                            },
                                            enabled = (draft.parsedInterval ?: 1) > 1,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(VrddhihRadii.sm))
                                                .background(VrddhihColors.PanelRaised)
                                                .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(VrddhihRadii.sm)),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Remove,
                                                contentDescription = "Decrease interval",
                                                tint = if ((draft.parsedInterval ?: 1) > 1) VrddhihColors.TextPrimary else VrddhihColors.TextMuted,
                                            )
                                        }

                                        BasicTextField(
                                            value = draft.intervalText,
                                            onValueChange = { input ->
                                                val filtered = input.filter { it.isDigit() }.take(3)
                                                onDraftChange(draft.copy(intervalText = filtered))
                                            },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Done,
                                            ),
                                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                                color = VrddhihColors.TextPrimary,
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            cursorBrush = SolidColor(VrddhihColors.Electric),
                                            decorationBox = { innerTextField ->
                                                Box(
                                                    modifier = Modifier
                                                        .width(68.dp)
                                                        .height(44.dp)
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
                                                val curr = draft.parsedInterval ?: 1
                                                if (curr < QuestDraft.MAX_INTERVAL) {
                                                    onDraftChange(draft.copy(intervalText = (curr + 1).toString()))
                                                }
                                            },
                                            enabled = (draft.parsedInterval ?: 1) < QuestDraft.MAX_INTERVAL,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(VrddhihRadii.sm))
                                                .background(VrddhihColors.PanelRaised)
                                                .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(VrddhihRadii.sm)),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Add,
                                                contentDescription = "Increase interval",
                                                tint = if ((draft.parsedInterval ?: 1) < QuestDraft.MAX_INTERVAL) VrddhihColors.TextPrimary else VrddhihColors.TextMuted,
                                            )
                                        }

                                        Text(
                                            text = unitLabel,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = VrddhihColors.TextPrimary,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(start = VrddhihSpacing.xs),
                                        )
                                    }
                                }

                                if (QuestDraftError.INTERVAL_OUT_OF_RANGE in errors) {
                                    Text(
                                        text = stringResource(R.string.quest_error_interval_invalid),
                                        color = VrddhihColors.Danger,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }

                                // Specific weekdays selection bubbles
                                if (draft.recurrence == QuestRecurrence.WEEKDAYS) {
                                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xxs)) {
                                        Text(
                                            text = "On days:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = VrddhihColors.TextMuted,
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            DayOfWeek.entries.forEach { dayOfWeek ->
                                                val isSelected = dayOfWeek in draft.weekdays
                                                val label = dayOfWeek.name.take(3)
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) VrddhihColors.Electric else VrddhihColors.Panel)
                                                        .border(
                                                            1.dp,
                                                            if (isSelected) VrddhihColors.Ice else VrddhihColors.Outline,
                                                            CircleShape,
                                                        )
                                                        .clickable {
                                                            val updated = if (isSelected) {
                                                                draft.weekdays - dayOfWeek
                                                            } else {
                                                                draft.weekdays + dayOfWeek
                                                            }
                                                            onDraftChange(draft.copy(weekdays = updated))
                                                        },
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
                                        if (QuestDraftError.NO_WEEKDAYS in errors) {
                                            Text(
                                                text = stringResource(R.string.quest_error_no_weekdays),
                                                color = VrddhihColors.Danger,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 7. OPTIONAL END DATE (Always shown for all recurrence types)
                    val effectiveEnd = draft.endDate ?: draft.deadline
                    val isRecurring = draft.recurrence != QuestRecurrence.NONE

                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                        Text(
                            text = stringResource(R.string.quest_field_end_date).uppercase(),
                            style = VrddhihTextStyles.SystemLabel,
                            color = VrddhihColors.Ice,
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(VrddhihRadii.sm))
                                .background(VrddhihColors.Panel)
                                .border(
                                    width = 1.dp,
                                    color = if (QuestDraftError.END_BEFORE_START in errors || QuestDraftError.DEADLINE_BEFORE_DATE in errors) {
                                        VrddhihColors.Danger
                                    } else {
                                        VrddhihColors.OutlineBright
                                    },
                                    shape = RoundedCornerShape(VrddhihRadii.sm),
                                )
                                .clickable { calendarTarget = CalendarPickerTarget.RECURRENCE_END_DATE }
                                .padding(horizontal = VrddhihSpacing.md, vertical = VrddhihSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = null,
                                    tint = if (effectiveEnd != null) VrddhihColors.Ice else VrddhihColors.TextMuted,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(VrddhihSpacing.sm))
                                val placeholder = if (isRecurring) {
                                    "Never ends (optional)"
                                } else {
                                    "No end date (optional deadline)"
                                }
                                Text(
                                    text = effectiveEnd?.let { dateFormatter.format(it) } ?: placeholder,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (effectiveEnd != null) VrddhihColors.TextPrimary else VrddhihColors.TextMuted,
                                )
                            }

                            if (effectiveEnd != null) {
                                IconButton(
                                    onClick = { onDraftChange(draft.copy(endDate = null, deadline = null)) },
                                    modifier = Modifier.size(32.dp),
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

                        if (QuestDraftError.END_BEFORE_START in errors || QuestDraftError.DEADLINE_BEFORE_DATE in errors) {
                            Text(
                                text = stringResource(R.string.quest_error_end_before_start),
                                color = VrddhihColors.Danger,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    // 8. OPTIONAL DUE TIME
                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                        Text(
                            text = stringResource(R.string.quest_field_due_time).uppercase(),
                            style = VrddhihTextStyles.SystemLabel,
                            color = VrddhihColors.Ice,
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(VrddhihRadii.sm))
                                .background(VrddhihColors.Panel)
                                .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(VrddhihRadii.sm))
                                .clickable { showTimePicker = true }
                                .padding(horizontal = VrddhihSpacing.md, vertical = VrddhihSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = if (draft.dueTime != null) VrddhihColors.Ice else VrddhihColors.TextMuted,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(VrddhihSpacing.sm))
                                Text(
                                    text = draft.dueTime?.let { timeFormatter.format(it) } ?: "No due time set",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (draft.dueTime != null) VrddhihColors.TextPrimary else VrddhihColors.TextMuted,
                                )
                            }

                            if (draft.dueTime != null) {
                                IconButton(
                                    onClick = { onDraftChange(draft.copy(dueTime = null)) },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear time",
                                        tint = VrddhihColors.TextMuted,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }

                    // Scope selector if editing a recurring quest
                    if (isEdit && draft.recurrence != QuestRecurrence.NONE) {
                        Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                            Text(
                                text = stringResource(R.string.quest_scope_title).uppercase(),
                                style = VrddhihTextStyles.SystemLabel,
                                color = VrddhihColors.Ice,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                            ) {
                                QuestScope.entries.forEach { s ->
                                    val isSelected = scope == s
                                    val labelRes = when (s) {
                                        QuestScope.OCCURRENCE -> R.string.quest_scope_occurrence
                                        QuestScope.FUTURE -> R.string.quest_scope_future
                                        QuestScope.ALL -> R.string.quest_scope_all
                                    }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onScopeChange(s) },
                                        label = { Text(stringResource(labelRes), style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = VrddhihColors.Panel,
                                            labelColor = VrddhihColors.TextSecondary,
                                            selectedContainerColor = VrddhihColors.Electric.copy(alpha = 0.22f),
                                            selectedLabelColor = VrddhihColors.Ice,
                                        ),
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(VrddhihSpacing.md))

                    // 9. PRIMARY CREATE QUEST OR SAVE CHANGES ACTION
                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(VrddhihRadii.md),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VrddhihColors.Electric,
                            contentColor = VrddhihColors.Void,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    ) {
                        Text(
                            text = stringResource(if (isEdit) R.string.quest_save_confirm else R.string.quest_create_title).uppercase(),
                            style = VrddhihTextStyles.TagLabel,
                            color = VrddhihColors.Void,
                        )
                    }

                    Spacer(Modifier.height(VrddhihSpacing.xl))
                }
            }
        }
    }
}

    // Calendar Picker Dialog
    draft?.let { activeDraft ->
        calendarTarget?.let { target ->
            val initialDate = when (target) {
                CalendarPickerTarget.SCHEDULED_DATE -> activeDraft.scheduledDate
                CalendarPickerTarget.RECURRENCE_END_DATE -> activeDraft.endDate ?: activeDraft.deadline ?: activeDraft.scheduledDate
            }

            VrddhihCalendarDialog(
                selectedDate = initialDate,
                today = LocalDate.now(),
                onDateSelected = { picked ->
                    when (target) {
                        CalendarPickerTarget.SCHEDULED_DATE -> onDraftChange(activeDraft.copy(scheduledDate = picked))
                        CalendarPickerTarget.RECURRENCE_END_DATE -> {
                            onDraftChange(
                                activeDraft.copy(
                                    endDate = picked,
                                    deadline = if (activeDraft.recurrence == QuestRecurrence.NONE) picked else activeDraft.deadline,
                                )
                            )
                        }
                    }
                    calendarTarget = null
                },
                onTodaySelected = {
                    val picked = LocalDate.now()
                    when (target) {
                        CalendarPickerTarget.SCHEDULED_DATE -> onDraftChange(activeDraft.copy(scheduledDate = picked))
                        CalendarPickerTarget.RECURRENCE_END_DATE -> {
                            onDraftChange(
                                activeDraft.copy(
                                    endDate = picked,
                                    deadline = if (activeDraft.recurrence == QuestRecurrence.NONE) picked else activeDraft.deadline,
                                )
                            )
                        }
                    }
                    calendarTarget = null
                },
                onDismiss = { calendarTarget = null },
            )
        }

        // Material 3 Time Picker Dialog
        if (showTimePicker) {
            val initialTime = activeDraft.dueTime ?: LocalTime.of(9, 0)
            val timePickerState = rememberTimePickerState(
                initialHour = initialTime.hour,
                initialMinute = initialTime.minute,
                is24Hour = is24h,
            )

        Dialog(
            onDismissRequest = { showTimePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            SystemPanel(
                modifier = Modifier
                    .padding(horizontal = VrddhihSpacing.lg)
                    .widthIn(max = 420.dp),
                glow = true,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.quest_field_due_time),
                        style = MaterialTheme.typography.titleMedium,
                        color = VrddhihColors.TextPrimary,
                    )

                    Spacer(Modifier.height(VrddhihSpacing.md))

                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = VrddhihColors.PanelRaised,
                            selectorColor = VrddhihColors.Electric,
                            containerColor = VrddhihColors.Panel,
                            periodSelectorBorderColor = VrddhihColors.OutlineBright,
                            clockDialSelectedContentColor = VrddhihColors.Void,
                            clockDialUnselectedContentColor = VrddhihColors.TextPrimary,
                            timeSelectorSelectedContainerColor = VrddhihColors.Electric.copy(alpha = 0.22f),
                            timeSelectorUnselectedContainerColor = VrddhihColors.PanelRaised,
                            timeSelectorSelectedContentColor = VrddhihColors.Ice,
                            timeSelectorUnselectedContentColor = VrddhihColors.TextPrimary,
                        ),
                    )

                    Spacer(Modifier.height(VrddhihSpacing.md))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text(stringResource(R.string.quest_cancel), color = VrddhihColors.TextSecondary)
                        }
                        Spacer(Modifier.width(VrddhihSpacing.xs))
                        Button(
                            onClick = {
                                onDraftChange(activeDraft.copy(dueTime = LocalTime.of(timePickerState.hour, timePickerState.minute)))
                                showTimePicker = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VrddhihColors.Electric),
                        ) {
                            Text(stringResource(R.string.quest_save_confirm), color = VrddhihColors.Void)
                        }
                    }
                }
            }
        }
    }
}

    // Custom Category Creation Dialog
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
                        if (created != null) {
                            draft?.let { onDraftChange(it.copy(categoryId = created.id)) }
                        }
                        showCustomCategoryDialog = false
                    }
                }
            },
        )
    }

    // Discard Confirmation Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.quest_discard_title),
                    color = VrddhihColors.TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.quest_discard_message),
                    color = VrddhihColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
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
                    Text(
                        text = stringResource(R.string.quest_keep_editing),
                        color = VrddhihColors.Ice,
                    )
                }
            },
            containerColor = VrddhihColors.PanelRaised,
            shape = RoundedCornerShape(VrddhihRadii.md),
        )
    }
}

@Composable
private fun PriorityButton(
    priority: TaskPriority,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val (labelRes, accentColor) = when (priority) {
        TaskPriority.LOW -> R.string.priority_low to VrddhihColors.TextSecondary
        TaskPriority.MEDIUM -> R.string.priority_medium to VrddhihColors.Electric
        TaskPriority.HIGH -> R.string.priority_high to VrddhihColors.Warning
        TaskPriority.CRITICAL -> R.string.priority_critical to VrddhihColors.Danger
    }

    val backgroundColor = if (isSelected) accentColor.copy(alpha = 0.22f) else VrddhihColors.Panel
    val borderColor = if (isSelected) accentColor else VrddhihColors.Outline
    val textColor = if (isSelected) accentColor else VrddhihColors.TextSecondary

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(VrddhihRadii.sm))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(VrddhihRadii.sm),
            )
            .clickable(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

