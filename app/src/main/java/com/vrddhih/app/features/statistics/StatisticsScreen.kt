package com.vrddhih.app.features.statistics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.vrddhih.app.R
import com.vrddhih.app.core.ui.components.FeaturePlaceholder

@Composable
fun StatisticsScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholder(
        eyebrow = stringResource(R.string.stats_eyebrow),
        title = stringResource(R.string.stats_title),
        icon = Icons.Outlined.Insights,
        description = stringResource(R.string.stats_description),
        phaseLabel = stringResource(R.string.stats_phase),
        plannedCapabilities = stringArrayResource(R.array.stats_capabilities).toList(),
        modifier = modifier,
    )
}
