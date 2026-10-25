package com.vrddhih.app.features.goals

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.vrddhih.app.R
import com.vrddhih.app.core.ui.components.FeaturePlaceholder

@Composable
fun GoalsScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholder(
        eyebrow = stringResource(R.string.goals_eyebrow),
        title = stringResource(R.string.goals_title),
        icon = Icons.Outlined.Flag,
        description = stringResource(R.string.goals_description),
        phaseLabel = stringResource(R.string.goals_phase),
        plannedCapabilities = stringArrayResource(R.array.goals_capabilities).toList(),
        modifier = modifier,
    )
}
