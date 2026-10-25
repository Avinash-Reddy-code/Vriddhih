package com.vrddhih.app.features.quests

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihTheme
import com.vrddhih.app.core.ui.components.FeaturePlaceholder

@Composable
fun QuestsScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholder(
        eyebrow = stringResource(R.string.quests_eyebrow),
        title = stringResource(R.string.quests_title),
        icon = Icons.Outlined.TaskAlt,
        description = stringResource(R.string.quests_description),
        phaseLabel = stringResource(R.string.quests_phase),
        plannedCapabilities = stringArrayResource(R.array.quests_capabilities).toList(),
        modifier = modifier,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF04060C)
@Composable
private fun QuestsScreenPreview() {
    VrddhihTheme { QuestsScreen() }
}
