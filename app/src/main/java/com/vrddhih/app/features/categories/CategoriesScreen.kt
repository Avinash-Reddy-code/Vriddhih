package com.vrddhih.app.features.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.CategoryColors
import com.vrddhih.app.core.ui.components.CategoryIcons
import com.vrddhih.app.core.ui.components.CuteCategoryBadge
import com.vrddhih.app.core.ui.components.SystemPanel

@Composable
fun CategoriesRoute(
    onBack: () -> Unit,
    viewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CategoriesScreen(
        uiState = uiState,
        onBack = onBack,
        onOpenCreate = viewModel::openCreate,
        onOpenEdit = viewModel::openEdit,
        onRequestDelete = viewModel::requestDelete,
        onDismissDelete = viewModel::dismissDelete,
        onConfirmDelete = viewModel::confirmDelete,
        onSetName = viewModel::setName,
        onSetIconKey = viewModel::setIconKey,
        onSetColor = viewModel::setColor,
        onCloseEditor = viewModel::closeEditor,
        onSaveEditor = viewModel::saveEditor,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    uiState: CategoriesUiState,
    onBack: () -> Unit,
    onOpenCreate: () -> Unit,
    onOpenEdit: (CategoryEntity) -> Unit,
    onRequestDelete: (CategoryEntity) -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onSetName: (String) -> Unit,
    onSetIconKey: (String) -> Unit,
    onSetColor: (Int) -> Unit,
    onCloseEditor: () -> Unit,
    onSaveEditor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val builtIn = uiState.categories.filter { it.isBuiltIn }
    val custom = uiState.categories.filter { !it.isBuiltIn }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.md),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = VrddhihColors.TextPrimary,
                    )
                }
                Spacer(Modifier.width(VrddhihSpacing.xs))
                Column {
                    Text(
                        text = "System · Categories",
                        style = VrddhihTextStyles.SystemLabel,
                        color = VrddhihColors.Ice,
                    )
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.headlineMedium,
                        color = VrddhihColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Button(
                onClick = onOpenCreate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VrddhihColors.Electric,
                    contentColor = VrddhihColors.Void,
                ),
                contentPadding = PaddingValues(horizontal = VrddhihSpacing.md, vertical = VrddhihSpacing.xs),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(VrddhihSpacing.xs))
                Text("New", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(Modifier.height(VrddhihSpacing.lg))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm)) {
            if (builtIn.isNotEmpty()) {
                item {
                    Text(
                        text = "Built-in",
                        style = VrddhihTextStyles.SystemLabel,
                        color = VrddhihColors.TextMuted,
                    )
                    Spacer(Modifier.height(VrddhihSpacing.xs))
                }
                items(builtIn, key = { it.id }) { category ->
                    CategoryRow(
                        category = category,
                        onEdit = null,
                        onDelete = null,
                    )
                }
            }

            if (custom.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(VrddhihSpacing.md))
                    Text(
                        text = "Custom",
                        style = VrddhihTextStyles.SystemLabel,
                        color = VrddhihColors.TextMuted,
                    )
                    Spacer(Modifier.height(VrddhihSpacing.xs))
                }
                items(custom, key = { it.id }) { category ->
                    CategoryRow(
                        category = category,
                        onEdit = { onOpenEdit(category) },
                        onDelete = { onRequestDelete(category) },
                    )
                }
            }

            if (custom.isEmpty()) {
                item {
                    Spacer(Modifier.height(VrddhihSpacing.md))
                    Text(
                        text = "No custom categories yet. Tap New to create one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VrddhihColors.TextMuted,
                    )
                }
            }
        }
    }

    // Category editor dialog
    if (uiState.editor.isOpen) {
        CategoryEditorDialog(
            state = uiState.editor,
            onSetName = onSetName,
            onSetIconKey = onSetIconKey,
            onSetColor = onSetColor,
            onDismiss = onCloseEditor,
            onSave = onSaveEditor,
        )
    }

    // Delete confirmation
    uiState.deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Delete category?", color = VrddhihColors.TextPrimary) },
            text = {
                Text(
                    text = "\"${target.name}\" will be deleted. Any quests in this category will be moved to Other.",
                    color = VrddhihColors.TextSecondary,
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = VrddhihColors.Danger),
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text("Cancel", color = VrddhihColors.TextSecondary)
                }
            },
            containerColor = VrddhihColors.Panel,
            titleContentColor = VrddhihColors.TextPrimary,
        )
    }
}

@Composable
private fun CategoryRow(
    category: CategoryEntity,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val iconColor = Color(category.colorArgb)
    val iconVector = CategoryIcons.byKey(category.iconKey)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VrddhihRadii.md))
            .background(VrddhihColors.Panel)
            .border(VrddhihDimens.BorderWidth, VrddhihColors.Outline, RoundedCornerShape(VrddhihRadii.md))
            .padding(horizontal = VrddhihSpacing.md, vertical = VrddhihSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Cute royal category badge
        CuteCategoryBadge(
            category = category,
            size = 44.dp,
            iconSize = 22.dp,
        )

        Spacer(Modifier.width(VrddhihSpacing.md))

        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge,
            color = VrddhihColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )

        if (category.isBuiltIn) {
            Text(
                text = "Built-in",
                style = MaterialTheme.typography.labelSmall,
                color = VrddhihColors.TextMuted,
            )
        } else {
            onEdit?.let {
                IconButton(onClick = it, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = VrddhihColors.Ice, modifier = Modifier.size(18.dp))
                }
            }
            onDelete?.let {
                IconButton(onClick = it, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = VrddhihColors.Danger, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorDialog(
    state: CategoryEditorState,
    onSetName: (String) -> Unit,
    onSetIconKey: (String) -> Unit,
    onSetColor: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    var showIconPicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        SystemPanel(
            modifier = Modifier
                .padding(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.xl)
                .fillMaxWidth(),
            glow = true,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (state.editingCategory == null) "New Category" else "Edit Category",
                    style = MaterialTheme.typography.titleLarge,
                    color = VrddhihColors.TextPrimary,
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = VrddhihColors.TextSecondary)
                }
            }

            Spacer(Modifier.height(VrddhihSpacing.md))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
            ) {
                // Name field
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onSetName,
                    label = { Text("Category name *") },
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { err -> { Text(err, color = VrddhihColors.Danger) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = darkFieldColors(),
                )

                // Icon picker button
                Text("Icon", style = VrddhihTextStyles.SystemLabel, color = VrddhihColors.TextMuted)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(VrddhihRadii.sm))
                        .background(VrddhihColors.PanelRaised)
                        .border(VrddhihDimens.BorderWidth, VrddhihColors.Outline, RoundedCornerShape(VrddhihRadii.sm))
                        .clickable { showIconPicker = !showIconPicker }
                        .padding(VrddhihSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val color = Color(state.colorArgb)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.2f))
                            .border(1.dp, color.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = CategoryIcons.byKey(state.iconKey),
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(VrddhihSpacing.sm))
                    Text(
                        text = CategoryIcons.labelFor(state.iconKey),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VrddhihColors.TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Text("Tap to change", style = MaterialTheme.typography.labelSmall, color = VrddhihColors.TextMuted)
                }

                if (showIconPicker) {
                    IconPickerGrid(
                        selectedKey = state.iconKey,
                        onSelect = { key ->
                            onSetIconKey(key)
                            showIconPicker = false
                        },
                    )
                }

                // Color picker
                Text("Accent Color", style = VrddhihTextStyles.SystemLabel, color = VrddhihColors.TextMuted)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                ) {
                    CategoryColors.PALETTE.forEach { (argb, label) ->
                        val color = Color(argb)
                        val isSelected = argb == state.colorArgb
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) VrddhihColors.Frost else VrddhihColors.Outline,
                                    shape = CircleShape,
                                )
                                .clickable { onSetColor(argb) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(VrddhihSpacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = VrddhihColors.TextSecondary)
                }
                Spacer(Modifier.width(VrddhihSpacing.md))
                Button(
                    onClick = onSave,
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VrddhihColors.Electric,
                        contentColor = VrddhihColors.Void,
                    ),
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconPickerGrid(
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        CategoryIcons.GROUPS.forEach { group ->
            val icons = CategoryIcons.ALL.filter { it.group == group }
            if (icons.isEmpty()) return@forEach
            Text(
                text = group,
                style = VrddhihTextStyles.SystemLabel,
                color = VrddhihColors.TextMuted,
                modifier = Modifier.padding(bottom = VrddhihSpacing.xs),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                modifier = Modifier.padding(bottom = VrddhihSpacing.md),
            ) {
                icons.forEach { entry ->
                    val isSelected = entry.key == selectedKey
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(VrddhihRadii.sm))
                            .background(if (isSelected) VrddhihColors.Electric.copy(alpha = 0.2f) else VrddhihColors.PanelRaised)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) VrddhihColors.Ice else VrddhihColors.Outline,
                                shape = RoundedCornerShape(VrddhihRadii.sm),
                            )
                            .clickable { onSelect(entry.key) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = entry.vector,
                            contentDescription = entry.label,
                            tint = if (isSelected) VrddhihColors.Ice else VrddhihColors.TextSecondary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun darkFieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedTextColor = VrddhihColors.TextPrimary,
    unfocusedTextColor = VrddhihColors.TextPrimary,
    focusedBorderColor = VrddhihColors.Ice,
    unfocusedBorderColor = VrddhihColors.Outline,
    focusedLabelColor = VrddhihColors.Ice,
    unfocusedLabelColor = VrddhihColors.TextMuted,
    cursorColor = VrddhihColors.Ice,
)
