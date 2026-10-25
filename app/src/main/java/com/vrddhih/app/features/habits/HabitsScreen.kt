package com.vrddhih.app.features.habits

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.vrddhih.app.R
import com.vrddhih.app.core.ui.components.FeaturePlaceholder

@Composable
fun HabitsScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholder(
        eyebrow = stringResource(R.string.habits_eyebrow),
        title = stringResource(R.string.habits_title),
        icon = Icons.Outlined.Autorenew,
        description = stringResource(R.string.habits_description),
        phaseLabel = stringResource(R.string.habits_phase),
        plannedCapabilities = stringArrayResource(R.array.habits_capabilities).toList(),
        modifier = modifier,
    )
}
