package com.vrddhih.app.features.quests.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vrddhih.app.R
import com.vrddhih.app.core.data.local.entity.TaskPriority
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.ui.components.SystemTag

@Composable
fun TaskPriorityTag(
    priority: TaskPriority,
    modifier: Modifier = Modifier,
) {
    val (labelRes, color) = when (priority) {
        TaskPriority.CRITICAL -> R.string.priority_critical to VrddhihColors.Danger
        TaskPriority.HIGH -> R.string.priority_high to VrddhihColors.Warning
        TaskPriority.MEDIUM -> R.string.priority_medium to VrddhihColors.Electric
        TaskPriority.LOW -> R.string.priority_low to VrddhihColors.TextSecondary
    }
    SystemTag(text = stringResource(labelRes), color = color, modifier = modifier)
}
