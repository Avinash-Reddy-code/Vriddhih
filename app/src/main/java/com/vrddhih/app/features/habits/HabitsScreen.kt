package com.vrddhih.app.features.habits

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrddhih.app.R
import com.vrddhih.app.core.data.local.dao.HabitWithConfigs
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.ScreenHeader
import com.vrddhih.app.features.habits.components.HabitItemCard
import com.vrddhih.app.features.habits.components.HabitStatisticsDialog

@Composable
fun HabitsScreen(
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    onNavigateToCreateHabit: () -> Unit = {},
    onNavigateToEditHabit: (Long) -> Unit = {},
    viewModel: HabitsViewModel = viewModel(factory = HabitsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var habitToDelete by remember { mutableStateOf<HabitWithConfigs?>(null) }
    var statisticsHabit by remember { mutableStateOf<HabitItemDisplay?>(null) }

    LifecycleResumeEffect(viewModel) {
        viewModel.refreshToday()
        onPauseOrDispose { }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = VrddhihColors.Void,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = VrddhihDimens.ContentMaxWidth)
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = VrddhihSpacing.lg,
                    end = VrddhihSpacing.lg,
                    top = if (showHeader) VrddhihSpacing.xl else VrddhihSpacing.xs,
                    bottom = 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.lg),
            ) {
                // Header
                if (showHeader) {
                    item(key = "header") {
                        ScreenHeader(
                            eyebrow = stringResource(R.string.habits_eyebrow),
                            title = stringResource(R.string.habits_title),
                            subtitle = stringResource(R.string.habits_description),
                        )
                    }
                }

                // Habit Count Bar
                item(key = "action-bar") {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = "${uiState.habitDisplays.size} HABITS",
                            style = VrddhihTextStyles.SystemLabel,
                            color = VrddhihColors.TextMuted,
                        )
                    }
                }

                // Loading Spinner or Habit Items
                if (uiState.isLoading) {
                    item(key = "loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = VrddhihColors.Electric)
                        }
                    }
                } else if (uiState.habitDisplays.isEmpty()) {
                    item(key = "empty") {
                        EmptyHabitsState(onCreateHabit = onNavigateToCreateHabit)
                    }
                } else {
                    items(
                        items = uiState.habitDisplays,
                        key = { it.habitWithConfigs.habit.id },
                    ) { habitDisplay ->
                        HabitItemCard(
                            display = habitDisplay,
                            onEdit = { habitWithConfigs ->
                                onNavigateToEditHabit(habitWithConfigs.habit.id)
                            },
                            onDelete = { habitWithConfigs ->
                                habitToDelete = habitWithConfigs
                            },
                            onShowStatistics = { display ->
                                statisticsHabit = display
                            },
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    habitToDelete?.let { habitWithConfigs ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            containerColor = VrddhihColors.Panel,
            title = {
                Text(
                    text = "DELETE HABIT",
                    style = VrddhihTextStyles.SystemLabel,
                    color = VrddhihColors.Danger,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${habitWithConfigs.habit.name}\"? This will permanently delete this habit and its historical progress records. Unrelated quests and goals will not be affected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VrddhihColors.TextSecondary,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHabit(habitWithConfigs.habit.id)
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VrddhihColors.Danger,
                        contentColor = VrddhihColors.Void,
                    ),
                    shape = RoundedCornerShape(VrddhihRadii.xs),
                ) {
                    Text("DELETE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("CANCEL", color = VrddhihColors.TextMuted)
                }
            },
        )
    }

    // Habit-Specific Statistics Dialog
    statisticsHabit?.let { display ->
        HabitStatisticsDialog(
            display = display,
            onDismiss = { statisticsHabit = null },
        )
    }
}

@Composable
private fun EmptyHabitsState(
    onCreateHabit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = VrddhihSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
    ) {
        Icon(
            imageVector = Icons.Outlined.TrackChanges,
            contentDescription = null,
            tint = VrddhihColors.TextMuted,
            modifier = Modifier.size(56.dp),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.habits_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = VrddhihColors.TextSecondary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.habits_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = VrddhihColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.xxs),
            )
        }

        Spacer(Modifier.height(VrddhihSpacing.xs))

        Button(
            onClick = onCreateHabit,
            colors = ButtonDefaults.buttonColors(
                containerColor = VrddhihColors.Electric,
                contentColor = VrddhihColors.Void,
            ),
            shape = RoundedCornerShape(VrddhihRadii.sm),
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("CREATE HABIT", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}
