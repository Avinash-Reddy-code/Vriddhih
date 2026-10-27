package com.vrddhih.app.features.goals.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vrddhih.app.R
import com.vrddhih.app.core.data.local.dao.GoalWithAllConfigs
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.data.local.entity.GoalType
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.CategorySelectionTile
import com.vrddhih.app.core.ui.components.SystemPanel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun GoalEditorDialog(
    existingGoal: GoalWithAllConfigs?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (
        goalId: Long?,
        title: String,
        description: String,
        type: GoalType,
        categoryId: String?,
        startDate: LocalDate?,
        targetDate: LocalDate?,
        milestoneTitles: List<String>,
        numericTargetValue: Long,
        numericCurrentValue: Long,
        numericUnit: String,
        completionTargetCount: Int,
        completionCurrentCount: Int,
    ) -> Unit,
    onDelete: ((Long) -> Unit)? = null,
) {
    val isEdit = existingGoal != null
    var title by remember { mutableStateOf(existingGoal?.goal?.title ?: "") }
    var description by remember { mutableStateOf(existingGoal?.goal?.description ?: "") }
    var type by remember { mutableStateOf(existingGoal?.goal?.type ?: GoalType.MILESTONE_BASED) }
    var selectedCategoryId by remember { mutableStateOf(existingGoal?.goal?.categoryId) }
    var targetDate by remember { mutableStateOf(existingGoal?.goal?.targetDate) }

    // Milestones state
    val milestoneList = remember {
        mutableStateListOf<String>().apply {
            if (existingGoal != null) {
                addAll(existingGoal.milestones.sortedBy { it.position }.map { it.title })
            }
        }
    }
    var newMilestoneInput by remember { mutableStateOf("") }

    // Numeric Target state
    var numericTargetValueStr by remember {
        mutableStateOf(existingGoal?.numericTarget?.targetValue?.toString() ?: "100")
    }
    var numericCurrentValueStr by remember {
        mutableStateOf(existingGoal?.numericTarget?.currentValue?.toString() ?: "0")
    }
    var numericUnit by remember {
        mutableStateOf(existingGoal?.numericTarget?.unit ?: "units")
    }

    // Completion Target state
    var completionTargetCountStr by remember {
        mutableStateOf(existingGoal?.completionTarget?.targetCount?.toString() ?: "1")
    }
    var completionCurrentCountStr by remember {
        mutableStateOf(existingGoal?.completionTarget?.currentCount?.toString() ?: "0")
    }

    var validationError by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(VrddhihColors.Void),
            color = VrddhihColors.Void,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(VrddhihSpacing.lg),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.goals_eyebrow).uppercase(),
                            style = VrddhihTextStyles.SystemLabel,
                            color = VrddhihColors.Ice,
                        )
                        Text(
                            text = stringResource(
                                if (isEdit) R.string.goal_editor_title_edit else R.string.goal_editor_title_new,
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = VrddhihColors.TextPrimary,
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.calendar_close),
                            tint = VrddhihColors.TextSecondary,
                        )
                    }
                }

                Spacer(Modifier.height(VrddhihSpacing.md))

                // Scrollable Form Body
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.lg),
                ) {
                    // Title Field
                    item {
                        SystemPanel {
                            Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm)) {
                                Text(
                                    text = stringResource(R.string.goal_field_title).uppercase(),
                                    style = VrddhihTextStyles.SystemLabel,
                                    color = VrddhihColors.Ice,
                                )
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = {
                                        title = it
                                        validationError = null
                                    },
                                    placeholder = {
                                        Text(
                                            text = "e.g., Read 24 Books, Run Half Marathon",
                                            color = VrddhihColors.TextMuted,
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = VrddhihColors.TextPrimary,
                                        unfocusedTextColor = VrddhihColors.TextPrimary,
                                        focusedBorderColor = VrddhihColors.Electric,
                                        unfocusedBorderColor = VrddhihColors.Outline,
                                    ),
                                )

                                Spacer(Modifier.height(VrddhihSpacing.xs))

                                Text(
                                    text = stringResource(R.string.goal_field_description).uppercase(),
                                    style = VrddhihTextStyles.SystemLabel,
                                    color = VrddhihColors.Ice,
                                )
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    placeholder = {
                                        Text(
                                            text = "Target notes, reason or strategy...",
                                            color = VrddhihColors.TextMuted,
                                        )
                                    },
                                    minLines = 2,
                                    maxLines = 4,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = VrddhihColors.TextPrimary,
                                        unfocusedTextColor = VrddhihColors.TextPrimary,
                                        focusedBorderColor = VrddhihColors.Electric,
                                        unfocusedBorderColor = VrddhihColors.Outline,
                                    ),
                                )
                            }
                        }
                    }

                    // Goal Type Selector
                    item {
                        SystemPanel {
                            Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm)) {
                                Text(
                                    text = stringResource(R.string.goal_field_type).uppercase(),
                                    style = VrddhihTextStyles.SystemLabel,
                                    color = VrddhihColors.Ice,
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                                ) {
                                    GoalTypeOption(
                                        label = stringResource(R.string.goal_type_milestone),
                                        selected = type == GoalType.MILESTONE_BASED,
                                        onClick = { type = GoalType.MILESTONE_BASED },
                                        modifier = Modifier.weight(1f),
                                    )
                                    GoalTypeOption(
                                        label = stringResource(R.string.goal_type_numeric),
                                        selected = type == GoalType.NUMERIC_TARGET,
                                        onClick = { type = GoalType.NUMERIC_TARGET },
                                        modifier = Modifier.weight(1f),
                                    )
                                    GoalTypeOption(
                                        label = stringResource(R.string.goal_type_completion),
                                        selected = type == GoalType.COMPLETION_BASED,
                                        onClick = { type = GoalType.COMPLETION_BASED },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }

                    // Type Configuration Panel
                    item {
                        when (type) {
                            GoalType.MILESTONE_BASED -> {
                                SystemPanel {
                                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm)) {
                                        Text(
                                            text = stringResource(R.string.goal_field_milestones).uppercase(),
                                            style = VrddhihTextStyles.SystemLabel,
                                            color = VrddhihColors.Ice,
                                        )

                                        // Add new milestone input row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            OutlinedTextField(
                                                value = newMilestoneInput,
                                                onValueChange = { newMilestoneInput = it },
                                                placeholder = {
                                                    Text(
                                                        stringResource(R.string.goal_field_milestone_hint),
                                                        color = VrddhihColors.TextMuted,
                                                    )
                                                },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = VrddhihColors.TextPrimary,
                                                    unfocusedTextColor = VrddhihColors.TextPrimary,
                                                    focusedBorderColor = VrddhihColors.Electric,
                                                    unfocusedBorderColor = VrddhihColors.Outline,
                                                ),
                                            )
                                            Button(
                                                onClick = {
                                                    val trimmed = newMilestoneInput.trim()
                                                    if (trimmed.isNotBlank()) {
                                                        milestoneList.add(trimmed)
                                                        newMilestoneInput = ""
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = VrddhihColors.Electric),
                                                shape = RoundedCornerShape(VrddhihRadii.sm),
                                                modifier = Modifier.height(56.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Add,
                                                    contentDescription = stringResource(R.string.goal_field_add_milestone),
                                                    tint = Color.Black,
                                                )
                                            }
                                        }

                                        // Existing milestones list
                                        if (milestoneList.isNotEmpty()) {
                                            Spacer(Modifier.height(VrddhihSpacing.xs))
                                            milestoneList.forEachIndexed { index, mTitle ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(VrddhihRadii.sm))
                                                        .background(VrddhihColors.PanelRaised)
                                                        .padding(
                                                            horizontal = VrddhihSpacing.md,
                                                            vertical = VrddhihSpacing.sm,
                                                        ),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(22.dp)
                                                                .clip(CircleShape)
                                                                .background(VrddhihColors.Electric.copy(alpha = 0.2f)),
                                                            contentAlignment = Alignment.Center,
                                                        ) {
                                                            Text(
                                                                text = "${index + 1}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontFamily = FontFamily.Monospace,
                                                                color = VrddhihColors.Ice,
                                                            )
                                                        }
                                                        Text(
                                                            text = mTitle,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = VrddhihColors.TextPrimary,
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = { milestoneList.removeAt(index) },
                                                        modifier = Modifier.size(36.dp),
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.Close,
                                                            contentDescription = "Remove",
                                                            tint = VrddhihColors.TextMuted,
                                                            modifier = Modifier.size(18.dp),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            GoalType.NUMERIC_TARGET -> {
                                SystemPanel {
                                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm)) {
                                        Text(
                                            text = stringResource(R.string.goal_type_numeric).uppercase(),
                                            style = VrddhihTextStyles.SystemLabel,
                                            color = VrddhihColors.Ice,
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = stringResource(R.string.goal_field_current_value),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = VrddhihColors.TextSecondary,
                                                )
                                                OutlinedTextField(
                                                    value = numericCurrentValueStr,
                                                    onValueChange = { numericCurrentValueStr = it },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = VrddhihColors.TextPrimary,
                                                        unfocusedTextColor = VrddhihColors.TextPrimary,
                                                        focusedBorderColor = VrddhihColors.Electric,
                                                        unfocusedBorderColor = VrddhihColors.Outline,
                                                    ),
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = stringResource(R.string.goal_field_target_value),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = VrddhihColors.TextSecondary,
                                                )
                                                OutlinedTextField(
                                                    value = numericTargetValueStr,
                                                    onValueChange = { numericTargetValueStr = it },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = VrddhihColors.TextPrimary,
                                                        unfocusedTextColor = VrddhihColors.TextPrimary,
                                                        focusedBorderColor = VrddhihColors.Electric,
                                                        unfocusedBorderColor = VrddhihColors.Outline,
                                                    ),
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(VrddhihSpacing.xs))

                                        Text(
                                            text = stringResource(R.string.goal_field_unit),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = VrddhihColors.TextSecondary,
                                        )
                                        OutlinedTextField(
                                            value = numericUnit,
                                            onValueChange = { numericUnit = it },
                                            singleLine = true,
                                            placeholder = { Text("e.g. km, kg, books, paise", color = VrddhihColors.TextMuted) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = VrddhihColors.TextPrimary,
                                                unfocusedTextColor = VrddhihColors.TextPrimary,
                                                focusedBorderColor = VrddhihColors.Electric,
                                                unfocusedBorderColor = VrddhihColors.Outline,
                                            ),
                                        )
                                    }
                                }
                            }
                            GoalType.COMPLETION_BASED -> {
                                SystemPanel {
                                    Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm)) {
                                        Text(
                                            text = stringResource(R.string.goal_type_completion).uppercase(),
                                            style = VrddhihTextStyles.SystemLabel,
                                            color = VrddhihColors.Ice,
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = stringResource(R.string.goal_field_current_value),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = VrddhihColors.TextSecondary,
                                                )
                                                OutlinedTextField(
                                                    value = completionCurrentCountStr,
                                                    onValueChange = { completionCurrentCountStr = it },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = VrddhihColors.TextPrimary,
                                                        unfocusedTextColor = VrddhihColors.TextPrimary,
                                                        focusedBorderColor = VrddhihColors.Electric,
                                                        unfocusedBorderColor = VrddhihColors.Outline,
                                                    ),
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = stringResource(R.string.goal_field_target_count),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = VrddhihColors.TextSecondary,
                                                )
                                                OutlinedTextField(
                                                    value = completionTargetCountStr,
                                                    onValueChange = { completionTargetCountStr = it },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = VrddhihColors.TextPrimary,
                                                        unfocusedTextColor = VrddhihColors.TextPrimary,
                                                        focusedBorderColor = VrddhihColors.Electric,
                                                        unfocusedBorderColor = VrddhihColors.Outline,
                                                    ),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Target Date Selector
                    item {
                        SystemPanel {
                            Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm)) {
                                Text(
                                    text = stringResource(R.string.goal_field_target_date).uppercase(),
                                    style = VrddhihTextStyles.SystemLabel,
                                    color = VrddhihColors.Ice,
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val dateText = targetDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                                        ?: stringResource(R.string.goal_field_no_date)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CalendarToday,
                                            contentDescription = null,
                                            tint = if (targetDate != null) VrddhihColors.Electric else VrddhihColors.TextMuted,
                                        )
                                        Text(
                                            text = dateText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (targetDate != null) VrddhihColors.TextPrimary else VrddhihColors.TextMuted,
                                            fontFamily = FontFamily.Monospace,
                                        )
                                    }

                                    if (targetDate != null) {
                                        TextButton(onClick = { targetDate = null }) {
                                            Text(stringResource(R.string.calendar_close), color = VrddhihColors.Danger)
                                        }
                                    }
                                }

                                // Preset Date Quick Selectors
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                                ) {
                                    val today = LocalDate.now()
                                    QuickDateButton("+1M", onClick = { targetDate = today.plusMonths(1) })
                                    QuickDateButton("+3M", onClick = { targetDate = today.plusMonths(3) })
                                    QuickDateButton("+6M", onClick = { targetDate = today.plusMonths(6) })
                                    QuickDateButton("+1Y", onClick = { targetDate = today.plusYears(1) })
                                }
                            }
                        }
                    }

                    // Category Selector
                    item {
                        SystemPanel {
                            Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm)) {
                                Text(
                                    text = stringResource(R.string.goal_field_category).uppercase(),
                                    style = VrddhihTextStyles.SystemLabel,
                                    color = VrddhihColors.Ice,
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                                ) {
                                    categories.forEach { cat ->
                                        CategorySelectionTile(
                                            category = cat,
                                            isSelected = selectedCategoryId == cat.id,
                                            onClick = {
                                                selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Validation error notice
                    if (validationError != null) {
                        item {
                            Text(
                                text = validationError ?: "",
                                color = VrddhihColors.Danger,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = VrddhihSpacing.sm),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(VrddhihSpacing.md))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
                ) {
                    if (existingGoal != null && onDelete != null) {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VrddhihColors.Danger),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VrddhihColors.Danger),
                            shape = RoundedCornerShape(VrddhihRadii.sm),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.goal_action_delete),
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(VrddhihSpacing.xs))
                            Text(
                                text = stringResource(R.string.goal_action_delete).uppercase(),
                                style = VrddhihTextStyles.SystemLabel,
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val trimmedTitle = title.trim()
                            if (trimmedTitle.isBlank()) {
                                validationError = "Goal title cannot be blank"
                                return@Button
                            }
                            if (type == GoalType.MILESTONE_BASED && milestoneList.isEmpty()) {
                                validationError = "At least one milestone is required for milestone goals"
                                return@Button
                            }

                            val nTargetVal = numericTargetValueStr.toLongOrNull() ?: 100L
                            val nCurrentVal = numericCurrentValueStr.toLongOrNull() ?: 0L
                            val cTargetCount = completionTargetCountStr.toIntOrNull() ?: 1
                            val cCurrentCount = completionCurrentCountStr.toIntOrNull() ?: 0

                            onSave(
                                existingGoal?.goal?.id,
                                trimmedTitle,
                                description.trim(),
                                type,
                                selectedCategoryId,
                                existingGoal?.goal?.startDate ?: LocalDate.now(),
                                targetDate,
                                milestoneList.toList(),
                                nTargetVal,
                                nCurrentVal,
                                numericUnit.trim().ifEmpty { "units" },
                                cTargetCount,
                                cCurrentCount,
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VrddhihColors.Electric),
                        shape = RoundedCornerShape(VrddhihRadii.sm),
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp),
                    ) {
                        Text(
                            text = stringResource(
                                if (isEdit) R.string.goal_action_update else R.string.goal_action_save,
                            ).uppercase(),
                            style = VrddhihTextStyles.SystemLabel,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && existingGoal != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = stringResource(R.string.goal_delete_confirm_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = VrddhihColors.TextPrimary,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.goal_delete_confirm_message, existingGoal.goal.title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VrddhihColors.TextSecondary,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(existingGoal.goal.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VrddhihColors.Danger),
                ) {
                    Text(
                        stringResource(R.string.goal_action_delete),
                        color = Color.White,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(
                        stringResource(R.string.calendar_close),
                        color = VrddhihColors.TextSecondary,
                    )
                }
            },
            containerColor = VrddhihColors.PanelRaised,
        )
    }
}

@Composable
private fun GoalTypeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(VrddhihRadii.sm))
            .background(if (selected) VrddhihColors.Electric.copy(alpha = 0.2f) else VrddhihColors.PanelRaised)
            .border(
                width = 1.dp,
                color = if (selected) VrddhihColors.Electric else VrddhihColors.Outline,
                shape = RoundedCornerShape(VrddhihRadii.sm),
            )
            .clickable(onClick = onClick)
            .padding(vertical = VrddhihSpacing.sm, horizontal = VrddhihSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) VrddhihColors.Ice else VrddhihColors.TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Composable
private fun QuickDateButton(
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(VrddhihRadii.xs))
            .background(VrddhihColors.PanelRaised)
            .border(1.dp, VrddhihColors.Outline, RoundedCornerShape(VrddhihRadii.xs))
            .clickable(onClick = onClick)
            .padding(horizontal = VrddhihSpacing.sm, vertical = VrddhihSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = VrddhihColors.Ice,
        )
    }
}
