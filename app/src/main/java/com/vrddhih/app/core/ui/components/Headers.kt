package com.vrddhih.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.vrddhih.app.core.theme.VrddhihBrushes
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles

/** Top-of-screen title block: a small system "eyebrow" label above the screen title. */
@Composable
fun ScreenHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = eyebrow.uppercase(), style = VrddhihTextStyles.SystemLabel, color = VrddhihColors.Ice)
        Spacer(Modifier.height(VrddhihSpacing.xs))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = VrddhihColors.TextPrimary,
            modifier = Modifier.semantics { heading() },
        )
        if (subtitle != null) {
            Spacer(Modifier.height(VrddhihSpacing.xxs))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = VrddhihColors.TextSecondary)
        }
    }
}

/** Section title used inside panels, with an energy accent bar and an optional trailing slot. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(VrddhihBrushes.EnergyVertical),
        )
        Spacer(Modifier.width(VrddhihSpacing.sm))
        Text(
            text = title.uppercase(),
            style = VrddhihTextStyles.SystemLabel,
            color = VrddhihColors.Frost,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        trailing?.invoke(this)
    }
}
