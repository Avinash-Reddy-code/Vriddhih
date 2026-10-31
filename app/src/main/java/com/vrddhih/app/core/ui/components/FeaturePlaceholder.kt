package com.vrddhih.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihSpacing

/**
 * Empty-state screen for features that are planned but not implemented yet.
 * It describes the feature honestly instead of showing fake functionality.
 */
@Composable
fun FeaturePlaceholder(
    icon: ImageVector,
    description: String,
    phaseLabel: String,
    plannedCapabilities: List<String>,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    title: String? = null,
    onBack: (() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = VrddhihDimens.ContentMaxWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = VrddhihSpacing.lg,
                    end = VrddhihSpacing.lg,
                    top = if (eyebrow == null && title == null) VrddhihSpacing.md else VrddhihSpacing.xl,
                    bottom = 96.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.lg),
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(VrddhihColors.PanelRaised)
                            .border(1.dp, VrddhihColors.OutlineBright, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = VrddhihColors.TextPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            if (!eyebrow.isNullOrBlank() || !title.isNullOrBlank()) {
                ScreenHeader(eyebrow = eyebrow, title = title.orEmpty())
            }

            SystemPanel(glow = true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SystemIconBadge(icon = icon)
                    Spacer(Modifier.width(VrddhihSpacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.placeholder_status_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = VrddhihColors.TextPrimary,
                        )
                        Spacer(Modifier.height(VrddhihSpacing.xxs))
                        Text(text = phaseLabel, style = MaterialTheme.typography.bodySmall, color = VrddhihColors.Ice)
                    }
                }
                Spacer(Modifier.height(VrddhihSpacing.lg))
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = VrddhihColors.TextSecondary)
            }

            SystemPanel(accent = VrddhihColors.OutlineBright) {
                SectionHeader(title = stringResource(R.string.placeholder_planned_heading))
                Spacer(Modifier.height(VrddhihSpacing.md))
                plannedCapabilities.forEach { capability ->
                    Row(
                        modifier = Modifier.padding(vertical = VrddhihSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .rotate(45f)
                                .background(VrddhihColors.Ice),
                        )
                        Spacer(Modifier.width(VrddhihSpacing.md))
                        Text(text = capability, style = MaterialTheme.typography.bodyMedium, color = VrddhihColors.TextPrimary)
                    }
                }
            }

            Text(
                text = stringResource(R.string.placeholder_footer),
                style = MaterialTheme.typography.bodySmall,
                color = VrddhihColors.TextMuted,
            )
        }
    }
}
