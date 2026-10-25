package com.vrddhih.app.features.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTheme
import com.vrddhih.app.core.ui.components.ScreenHeader
import com.vrddhih.app.core.ui.components.SystemPanel
import com.vrddhih.app.features.dashboard.mock.DashboardMockData

@Composable
fun DashboardRoute(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(
        uiState = uiState,
        onToggleQuestPreview = viewModel::toggleQuestPreview,
        modifier = modifier,
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onToggleQuestPreview: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = VrddhihDimens.ContentMaxWidth)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.lg),
        ) {
            item(key = "header") {
                ScreenHeader(
                    eyebrow = stringResource(R.string.dashboard_eyebrow),
                    title = stringResource(R.string.dashboard_title),
                    subtitle = stringResource(R.string.dashboard_subtitle),
                )
            }
            item(key = "notice") { PreviewNotice() }
            item(key = "player") { PlayerStatusPanel(player = uiState.player) }
            item(key = "summary") { DailySummaryPanel(uiState = uiState) }
            item(key = "quests") { TodaysQuestsPanel(quests = uiState.quests, onToggleQuest = onToggleQuestPreview) }
            item(key = "habits") { HabitProgressPanel(uiState = uiState) }
            item(key = "goals") { GoalsPanel(goals = uiState.goals) }
            item(key = "deadlines") { DeadlinesPanel(deadlines = uiState.deadlines) }
            item(key = "footer") {
                Text(
                    text = stringResource(R.string.dashboard_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = VrddhihColors.TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = VrddhihSpacing.sm),
                )
            }
        }
    }
}

@Composable
private fun PreviewNotice(modifier: Modifier = Modifier) {
    SystemPanel(
        modifier = modifier,
        accent = VrddhihColors.Sample,
        contentPadding = PaddingValues(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = VrddhihColors.Sample,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(VrddhihSpacing.md))
            Text(
                text = stringResource(R.string.dashboard_preview_notice),
                style = MaterialTheme.typography.bodySmall,
                color = VrddhihColors.TextSecondary,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF04060C, widthDp = 390, heightDp = 1800)
@Composable
private fun DashboardScreenPreview() {
    VrddhihTheme {
        DashboardScreen(uiState = DashboardMockData.initialState(), onToggleQuestPreview = {})
    }
}
