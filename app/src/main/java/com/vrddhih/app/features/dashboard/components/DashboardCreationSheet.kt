package com.vrddhih.app.features.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles

/**
 * Compact modal bottom sheet offering creation options on the Dashboard (Quest, Habit, Goal).
 * Matches Vrddhih's ultra-dark neon theme and delegates to the existing creation flows without duplicating forms.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCreationSheet(
    onDismiss: () -> Unit,
    onCreateQuest: () -> Unit,
    onCreateHabit: () -> Unit,
    onCreateGoal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VrddhihColors.Panel,
        contentColor = VrddhihColors.TextPrimary,
        scrimColor = VrddhihColors.Void.copy(alpha = 0.76f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(VrddhihColors.OutlineBright),
            )
        },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VrddhihSpacing.lg)
                .navigationBarsPadding()
                .padding(bottom = VrddhihSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = VrddhihSpacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.create_sheet_eyebrow),
                    style = VrddhihTextStyles.SystemLabel,
                    color = VrddhihColors.Ice,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.create_sheet_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VrddhihColors.TextPrimary,
                )
            }

            // Option 1: Quest
            CreationOptionRow(
                title = stringResource(R.string.quests_new_action),
                description = stringResource(R.string.create_sheet_quest_desc),
                icon = Icons.Outlined.TaskAlt,
                accentColor = VrddhihColors.Electric,
                onClick = onCreateQuest,
            )

            // Option 2: Habit
            CreationOptionRow(
                title = stringResource(R.string.habits_new_action),
                description = stringResource(R.string.create_sheet_habit_desc),
                icon = Icons.Outlined.Autorenew,
                accentColor = VrddhihColors.NeonGreen,
                onClick = onCreateHabit,
            )

            // Option 3: Goal
            CreationOptionRow(
                title = stringResource(R.string.goals_new_action),
                description = stringResource(R.string.create_sheet_goal_desc),
                icon = Icons.Outlined.Flag,
                accentColor = VrddhihColors.Amber,
                onClick = onCreateGoal,
            )
        }
    }
}

@Composable
private fun CreationOptionRow(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(VrddhihRadii.md)
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(VrddhihColors.PanelRaised)
            .border(width = 1.dp, color = VrddhihColors.Outline, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = accentColor),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = VrddhihSpacing.md, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Accent Icon Container
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.14f))
                .border(width = 1.dp, color = accentColor.copy(alpha = 0.35f), shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(Modifier.width(VrddhihSpacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = VrddhihColors.TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = VrddhihColors.TextSecondary,
            )
        }

        Spacer(Modifier.width(VrddhihSpacing.sm))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = VrddhihColors.TextMuted,
            modifier = Modifier.size(14.dp),
        )
    }
}
