package com.vrddhih.app.features.categories

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrddhih.app.R
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.AnchoredPrimaryAction
import com.vrddhih.app.core.ui.components.CategoryColors
import com.vrddhih.app.core.ui.components.CategoryIcons
import com.vrddhih.app.core.ui.components.SystemPanel
import com.vrddhih.app.core.ui.components.VrddhihPrimaryButton
import com.vrddhih.app.core.ui.components.neonAmbientGlow
import com.vrddhih.app.core.ui.gesture.consumeHorizontalNestedScroll

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VrddhihColors.Void),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header: Black/near-black background, back arrow, title, subtle divider, status bar padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VrddhihColors.Void)
                    .statusBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = VrddhihSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = VrddhihColors.TextPrimary,
                        )
                    }
                    Spacer(Modifier.width(VrddhihSpacing.xs))
                    Text(
                        text = stringResource(R.string.categories_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = VrddhihColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                HorizontalDivider(
                    color = VrddhihColors.Outline,
                    thickness = 1.dp,
                )
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = VrddhihSpacing.lg, bottom = 96.dp),
            ) {
                // 1. Custom categories section
                Text(
                    text = stringResource(R.string.categories_custom_heading),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VrddhihColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = VrddhihSpacing.lg),
                )

                Spacer(Modifier.height(VrddhihSpacing.md))

                if (custom.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = VrddhihSpacing.lg),
                        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
                    ) {
                        custom.forEach { category ->
                            val count = uiState.entryCounts[category.id] ?: 0
                            CategoryItemCard(
                                category = category,
                                entryCount = count,
                                onClick = { onOpenEdit(category) },
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = VrddhihSpacing.lg)
                            .clip(RoundedCornerShape(VrddhihRadii.md))
                            .background(VrddhihColors.Panel)
                            .border(1.dp, VrddhihColors.Outline, RoundedCornerShape(VrddhihRadii.md))
                            .padding(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.md),
                    ) {
                        Text(
                            text = stringResource(R.string.categories_custom_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = VrddhihColors.TextMuted,
                        )
                    }
                }

                Spacer(Modifier.height(VrddhihSpacing.xl))

                // 2. Default categories section (horizontally scrollable row)
                Text(
                    text = stringResource(R.string.categories_default_heading),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VrddhihColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = VrddhihSpacing.lg),
                )

                Spacer(Modifier.height(VrddhihSpacing.md))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .consumeHorizontalNestedScroll()
                        .padding(horizontal = VrddhihSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
                ) {
                    builtIn.forEach { category ->
                        val count = uiState.entryCounts[category.id] ?: 0
                        CategoryItemCard(
                            category = category,
                            entryCount = count,
                            onClick = {},
                        )
                    }
                }
            }
        }

        // Prominent NEW CATEGORY action button pinned near the bottom
        AnchoredPrimaryAction(
            text = stringResource(R.string.categories_new_action).uppercase(),
            onClick = onOpenCreate,
            accent = VrddhihColors.NeonGreen,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
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
            onDelete = onRequestDelete,
        )
    }

    // Delete confirmation
    uiState.deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = {
                Text(
                    text = stringResource(R.string.categories_delete_confirm_title),
                    color = VrddhihColors.TextPrimary,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.categories_delete_confirm_message, target.name),
                    color = VrddhihColors.TextSecondary,
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = VrddhihColors.Danger),
                ) {
                    Text(stringResource(R.string.action_delete), color = VrddhihColors.TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text(stringResource(R.string.action_cancel), color = VrddhihColors.TextSecondary)
                }
            },
            containerColor = VrddhihColors.Panel,
            titleContentColor = VrddhihColors.TextPrimary,
        )
    }
}

/**
 * HabitNow-inspired category item:
 * 1. Large rounded-square colored icon container with restrained ambient glow
 * 2. Centered vector icon
 * 3. Category name beneath
 * 4. Associated entry count beneath
 */
@Composable
private fun CategoryItemCard(
    category: CategoryEntity,
    entryCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val countText = pluralStringResource(
        R.plurals.category_entry_count,
        entryCount,
        entryCount,
    )
    val accentColor = Color(category.colorArgb)
    val iconVector = CategoryIcons.byKey(category.iconKey)

    Column(
        modifier = modifier
            .width(84.dp)
            .clip(RoundedCornerShape(VrddhihRadii.lg))
            .clickable(onClick = onClick)
            .padding(vertical = VrddhihSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Large rounded-square colored icon container
        Box(
            modifier = Modifier
                .size(64.dp)
                .neonAmbientGlow(
                    color = accentColor,
                    radius = 10.dp,
                    alpha = 0.28f,
                )
                .clip(RoundedCornerShape(VrddhihRadii.lg))
                .background(accentColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = category.name,
                tint = VrddhihColors.Void,
                modifier = Modifier.size(32.dp),
            )
        }

        Spacer(Modifier.height(VrddhihSpacing.xs))

        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = VrddhihColors.TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = countText,
            style = MaterialTheme.typography.labelSmall,
            color = VrddhihColors.TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
        )
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
    onDelete: ((CategoryEntity) -> Unit)? = null,
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
                    text = if (state.editingCategory == null) {
                        stringResource(R.string.categories_new_action)
                    } else {
                        stringResource(R.string.categories_edit_title)
                    },
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.editingCategory != null && onDelete != null) {
                    TextButton(onClick = {
                        val target = state.editingCategory
                        onDismiss()
                        onDelete(target)
                    }) {
                        Text(stringResource(R.string.action_delete), color = VrddhihColors.Danger)
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel), color = VrddhihColors.TextSecondary)
                    }
                    Spacer(Modifier.width(VrddhihSpacing.md))
                    VrddhihPrimaryButton(
                        text = stringResource(R.string.action_save),
                        onClick = onSave,
                        accent = VrddhihColors.NeonGreen,
                    )
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
                            .then(if (isSelected) Modifier.neonAmbientGlow(VrddhihColors.Cyan, radius = 6.dp, alpha = 0.35f) else Modifier)
                            .clip(RoundedCornerShape(VrddhihRadii.sm))
                            .background(if (isSelected) VrddhihColors.Electric.copy(alpha = 0.2f) else VrddhihColors.PanelRaised)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) VrddhihColors.Cyan else VrddhihColors.Outline,
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
