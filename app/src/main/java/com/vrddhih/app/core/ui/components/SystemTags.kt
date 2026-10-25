package com.vrddhih.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles

/** Small uppercase pill for statuses such as priority. */
@Composable
fun SystemTag(text: String, color: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(VrddhihRadii.xs)
    Text(
        text = text.uppercase(),
        style = VrddhihTextStyles.TagLabel,
        color = color,
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.12f))
            .border(VrddhihDimens.BorderWidth, color.copy(alpha = 0.45f), shape)
            .padding(horizontal = VrddhihSpacing.sm, vertical = VrddhihSpacing.xxs),
    )
}

/** Labels content backed by mock data. Remove usages as each section moves to real local data. */
@Composable
fun MockDataBadge(modifier: Modifier = Modifier) {
    SystemTag(text = stringResource(R.string.mock_data_badge), color = VrddhihColors.Sample, modifier = modifier)
}

@Composable
fun SystemIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = VrddhihColors.Ice,
) {
    Box(
        modifier = modifier
            .size(VrddhihDimens.IconBadgeSize)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.12f))
            .border(VrddhihDimens.BorderWidth, tint.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
    }
}
