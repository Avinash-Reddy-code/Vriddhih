package com.vrddhih.app.features.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.features.goals.components.GoalCard
import com.vrddhih.app.features.goals.components.GoalEditorDialog

@Composable
fun GoalsRoute(
    modifier: Modifier = Modifier,
    viewModel: GoalsViewModel = viewModel(factory = GoalsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GoalsScreen(
        uiState = uiState,
        onFilterChange = viewModel::setFilter,
        onOpenCreateGoal = viewModel::openCreateGoal,
        onOpenEditGoal = viewModel::openEditGoal,
        onCloseEditor = viewModel::closeEditor,
        onSaveGoal = viewModel::saveGoal,
        onDeleteGoal = viewModel::deleteGoal,
        onToggleMilestone = viewModel::toggleMilestone,
        onUpdateNumericProgress = viewModel::updateNumericProgress,
        onUpdateCompletionCount = viewModel::updateCompletionCount,
        modifier = modifier,
    )
}

@Composable
fun GoalsScreen(
    uiState: GoalsUiState,
    onFilterChange: (GoalFilter) -> Unit,
    onOpenCreateGoal: () -> Unit,
    onOpenEditGoal: (com.vrddhih.app.core.data.local.dao.GoalWithAllConfigs) -> Unit,
    onCloseEditor: () -> Unit,
    onSaveGoal: (
        goalId: Long?,
        title: String,
        description: String,
        type: com.vrddhih.app.core.data.local.entity.GoalType,
        categoryId: String?,
        startDate: java.time.LocalDate?,
        targetDate: java.time.LocalDate?,
        milestoneTitles: List<String>,
        numericTargetValue: Long,
        numericCurrentValue: Long,
        numericUnit: String,
        completionTargetCount: Int,
        completionCurrentCount: Int,
    ) -> Unit,
    onDeleteGoal: (Long) -> Unit,
    onToggleMilestone: (goalId: Long, milestoneId: Long, completed: Boolean) -> Unit,
    onUpdateNumericProgress: (goalId: Long, newValue: Long) -> Unit,
    onUpdateCompletionCount: (goalId: Long, newCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = VrddhihColors.Void,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Header(onAddClick = onOpenCreateGoal)

            FilterTabs(
                selectedFilter = uiState.filter,
                onFilterSelected = onFilterChange,
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = VrddhihColors.Electric)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
                ) {
                    if (uiState.filteredGoals.isEmpty()) {
                        item {
                            EmptyState(onCreateGoal = onOpenCreateGoal)
                        }
                    } else {
                        items(uiState.filteredGoals, key = { it.goal.id }) { goalWithConfigs ->
                            GoalCard(
                                goalWithConfigs = goalWithConfigs,
                                categories = uiState.categories,
                                onEdit = { onOpenEditGoal(goalWithConfigs) },
                                onToggleMilestone = onToggleMilestone,
                                onUpdateNumericProgress = onUpdateNumericProgress,
                                onUpdateCompletionCount = onUpdateCompletionCount,
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.isEditorOpen) {
        GoalEditorDialog(
            existingGoal = uiState.editingGoal,
            categories = uiState.categories,
            onDismiss = onCloseEditor,
            onSave = onSaveGoal,
            onDelete = onDeleteGoal,
        )
    }
}

@Composable
private fun Header(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.md),
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
                text = stringResource(R.string.goals_title),
                style = MaterialTheme.typography.titleLarge,
                color = VrddhihColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        }

        IconButton(
            onClick = onAddClick,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.goals_new_action),
                tint = VrddhihColors.Electric,
            )
        }
    }
}

@Composable
private fun FilterTabs(
    selectedFilter: GoalFilter,
    onFilterSelected: (GoalFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
    ) {
        GoalFilterTab(
            label = stringResource(R.string.goals_filter_active),
            selected = selectedFilter == GoalFilter.ACTIVE,
            onClick = { onFilterSelected(GoalFilter.ACTIVE) },
            modifier = Modifier.weight(1f),
        )
        GoalFilterTab(
            label = stringResource(R.string.goals_filter_completed),
            selected = selectedFilter == GoalFilter.COMPLETED,
            onClick = { onFilterSelected(GoalFilter.COMPLETED) },
            modifier = Modifier.weight(1f),
        )
        GoalFilterTab(
            label = stringResource(R.string.goals_filter_all),
            selected = selectedFilter == GoalFilter.ALL,
            onClick = { onFilterSelected(GoalFilter.ALL) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun GoalFilterTab(
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
            .padding(vertical = VrddhihSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            style = VrddhihTextStyles.SystemLabel,
            color = if (selected) VrddhihColors.Ice else VrddhihColors.TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun EmptyState(onCreateGoal: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(VrddhihSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Flag,
            contentDescription = null,
            tint = VrddhihColors.TextMuted,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(VrddhihSpacing.md))
        Text(
            text = stringResource(R.string.goal_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = VrddhihColors.TextSecondary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(VrddhihSpacing.xs))
        Text(
            text = stringResource(R.string.goal_empty_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = VrddhihColors.TextMuted,
        )
        Spacer(Modifier.height(VrddhihSpacing.lg))
        Button(
            onClick = onCreateGoal,
            colors = ButtonDefaults.buttonColors(containerColor = VrddhihColors.Electric),
            shape = RoundedCornerShape(VrddhihRadii.sm),
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.size(VrddhihSpacing.xs))
            Text(
                text = stringResource(R.string.goals_new_action).uppercase(),
                style = VrddhihTextStyles.SystemLabel,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
