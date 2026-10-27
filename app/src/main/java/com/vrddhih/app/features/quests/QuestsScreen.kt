package com.vrddhih.app.features.quests

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Menu
import com.vrddhih.app.core.data.local.entity.CATEGORY_OTHER_ID
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrddhih.app.R
import com.vrddhih.app.core.data.quest.QuestDraft
import com.vrddhih.app.core.data.quest.QuestFilter
import com.vrddhih.app.core.data.quest.QuestOccurrence
import com.vrddhih.app.core.data.quest.QuestScope
import com.vrddhih.app.core.data.quest.QuestSort
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.theme.VrddhihTheme
import com.vrddhih.app.core.ui.components.ScreenHeader
import com.vrddhih.app.core.ui.components.SystemPanel
import com.vrddhih.app.core.ui.components.SystemTag
import com.vrddhih.app.core.ui.gesture.consumeHorizontalNestedScroll
import com.vrddhih.app.features.quests.components.QuestDeleteDialog
import com.vrddhih.app.features.quests.components.QuestDetailDialog
import com.vrddhih.app.features.quests.components.QuestItemRow
import java.time.LocalDate

@Composable
fun QuestsRoute(
    modifier: Modifier = Modifier,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToCreateQuest: () -> Unit = {},
    onNavigateToEditQuest: (Long, LocalDate) -> Unit = { _, _ -> },
    viewModel: QuestsViewModel = viewModel(factory = QuestsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(viewModel) {
        viewModel.refreshToday()
        onPauseOrDispose { }
    }

    QuestsScreen(
        uiState = uiState,
        onFilterChange = viewModel::setFilter,
        onSortChange = viewModel::setSort,
        onSearchChange = viewModel::setSearchQuery,
        onToggleCompletion = viewModel::toggleCompletion,
        onOpenCreate = {
            viewModel.openCreateQuest()
            onNavigateToCreateQuest()
        },
        onOpenDetail = viewModel::openDetails,
        onCloseDetail = viewModel::closeDetails,
        onOpenEdit = { occ ->
            viewModel.closeDetails()
            viewModel.openEditQuest(occ)
            onNavigateToEditQuest(occ.questId, occ.date)
        },
        onNavigateToCategories = onNavigateToCategories,
        onDraftChange = viewModel::updateEditorDraft,
        onScopeChange = viewModel::updateEditorScope,
        onSaveDraft = viewModel::saveEditor,
        onCloseEditor = viewModel::closeEditor,
        onRequestDelete = { occ ->
            viewModel.closeDetails()
            viewModel.requestDelete(occ)
        },
        onConfirmDelete = viewModel::confirmDelete,
        onDismissDelete = viewModel::dismissDelete,
        modifier = modifier,
    )
}

@Composable
fun QuestsScreen(
    uiState: QuestsUiState,
    onFilterChange: (QuestFilter) -> Unit,
    onSortChange: (QuestSort) -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleCompletion: (QuestOccurrence) -> Unit,
    onOpenCreate: () -> Unit,
    onOpenDetail: (QuestOccurrence) -> Unit,
    onCloseDetail: () -> Unit,
    onOpenEdit: (QuestOccurrence) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCategories: () -> Unit = {},
    onDraftChange: (QuestDraft) -> Unit = {},
    onScopeChange: (QuestScope) -> Unit = {},
    onSaveDraft: () -> Unit = {},
    onCloseEditor: () -> Unit = {},
    onRequestDelete: (QuestOccurrence) -> Unit = {},
    onConfirmDelete: (QuestScope) -> Unit = {},
    onDismissDelete: () -> Unit = {},
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = VrddhihDimens.ContentMaxWidth)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = VrddhihSpacing.lg,
                end = VrddhihSpacing.lg,
                top = VrddhihSpacing.xl,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
        ) {
            item(key = "header") {
                ScreenHeader(
                    eyebrow = stringResource(R.string.quests_eyebrow),
                    title = stringResource(R.string.quests_title),
                    subtitle = stringResource(R.string.quests_description),
                    action = {
                        IconButton(
                            onClick = onNavigateToCategories,
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Menu,
                                contentDescription = stringResource(R.string.categories_title),
                                tint = VrddhihColors.Ice,
                            )
                        }
                    },
                )
            }

            item(key = "search-and-sort") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text(stringResource(R.string.quests_search_hint), color = VrddhihColors.TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = VrddhihColors.TextMuted)
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = VrddhihColors.TextMuted)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = VrddhihColors.TextPrimary,
                            unfocusedTextColor = VrddhihColors.TextPrimary,
                            focusedBorderColor = VrddhihColors.Ice,
                            unfocusedBorderColor = VrddhihColors.Outline,
                            cursorColor = VrddhihColors.Ice,
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )

                    Spacer(Modifier.width(VrddhihSpacing.sm))

                    Box {
                        IconButton(
                            onClick = { sortMenuExpanded = true },
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort",
                                tint = VrddhihColors.Ice,
                            )
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
                        ) {
                            QuestSort.entries.forEach { sort ->
                                val labelRes = when (sort) {
                                    QuestSort.DEADLINE -> R.string.quests_sort_deadline
                                    QuestSort.PRIORITY -> R.string.quests_sort_priority
                                    QuestSort.CREATED -> R.string.quests_sort_created
                                }
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(labelRes),
                                            color = if (uiState.sort == sort) VrddhihColors.Ice else VrddhihColors.TextPrimary,
                                        )
                                    },
                                    onClick = {
                                        onSortChange(sort)
                                        sortMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            item(key = "filter-chips") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .consumeHorizontalNestedScroll()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                ) {
                    QuestFilter.entries.forEach { filter ->
                        val isSelected = uiState.filter == filter
                        val count = uiState.counts[filter] ?: 0
                        val labelRes = when (filter) {
                            QuestFilter.ALL -> R.string.quests_filter_all
                            QuestFilter.TODAY -> R.string.quests_filter_today
                            QuestFilter.UPCOMING -> R.string.quests_filter_upcoming
                            QuestFilter.OVERDUE -> R.string.quests_filter_overdue
                            QuestFilter.COMPLETED -> R.string.quests_filter_completed
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { onFilterChange(filter) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(labelRes))
                                    Spacer(Modifier.width(VrddhihSpacing.xs))
                                    Text(
                                        text = count.toString(),
                                        style = VrddhihTextStyles.NumericSmall,
                                        color = if (isSelected) VrddhihColors.Ice else VrddhihColors.TextMuted,
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = VrddhihColors.Panel,
                                labelColor = VrddhihColors.TextSecondary,
                                selectedContainerColor = VrddhihColors.Electric.copy(alpha = 0.22f),
                                selectedLabelColor = VrddhihColors.Ice,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = VrddhihColors.Outline,
                                selectedBorderColor = VrddhihColors.Ice,
                            ),
                        )
                    }
                }
            }

            if (uiState.occurrences.isEmpty()) {
                item(key = "empty-state") {
                    SystemPanel(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = VrddhihSpacing.xl),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.quests_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = VrddhihColors.TextPrimary,
                            )
                            Spacer(Modifier.height(VrddhihSpacing.xs))
                            Text(
                                text = stringResource(R.string.quests_empty_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = VrddhihColors.TextMuted,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            } else {
                items(
                    items = uiState.occurrences,
                    key = { it.key },
                ) { occurrence ->
                    val category = uiState.categories.find { it.id == (occurrence.quest.categoryId ?: CATEGORY_OTHER_ID) }
                    QuestItemRow(
                        occurrence = occurrence,
                        today = uiState.today,
                        onToggle = { onToggleCompletion(occurrence) },
                        onClick = { onOpenDetail(occurrence) },
                        category = category,
                    )
                }
            }
        }

        // Prominent, restrained New Quest action anchored at bottom above bottom navigation
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = VrddhihDimens.ContentMaxWidth)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            VrddhihColors.Void.copy(alpha = 0.88f),
                            VrddhihColors.Void,
                        ),
                    ),
                )
                .padding(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.md),
        ) {
            Button(
                onClick = onOpenCreate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(VrddhihRadii.md),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VrddhihColors.Electric,
                    contentColor = VrddhihColors.Void,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = VrddhihColors.Void,
                )
                Spacer(Modifier.width(VrddhihSpacing.sm))
                Text(
                    text = stringResource(R.string.quests_new_action).uppercase(),
                    style = VrddhihTextStyles.TagLabel,
                    color = VrddhihColors.Void,
                )
            }
        }
    }

    if (uiState.isDetailOpen && uiState.selectedOccurrence != null) {
        val selected = uiState.selectedOccurrence
        val category = uiState.categories.find { it.id == (selected.quest.categoryId ?: CATEGORY_OTHER_ID) }
        QuestDetailDialog(
            occurrence = selected,
            today = uiState.today,
            onToggleCompletion = { onToggleCompletion(selected) },
            onEdit = { onOpenEdit(selected) },
            onDelete = { onRequestDelete(selected) },
            onDismiss = onCloseDetail,
            category = category,
        )
    }

    if (uiState.deleteTarget != null) {
        QuestDeleteDialog(
            target = uiState.deleteTarget,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF04060C)
@Composable
private fun QuestsScreenPreview() {
    VrddhihTheme {
        QuestsScreen(
            uiState = QuestsUiState(),
            onFilterChange = {},
            onSortChange = {},
            onSearchChange = {},
            onToggleCompletion = {},
            onOpenCreate = {},
            onOpenDetail = {},
            onCloseDetail = {},
            onOpenEdit = {},
            onDraftChange = {},
            onScopeChange = {},
            onSaveDraft = {},
            onCloseEditor = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onDismissDelete = {},
        )
    }
}
