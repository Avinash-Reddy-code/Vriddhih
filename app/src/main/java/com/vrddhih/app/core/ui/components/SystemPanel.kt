package com.vrddhih.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.vrddhih.app.core.theme.VrddhihBrushes
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing

/**
 * The base "status window" surface: a dark gradient card with an accent-tinted border.
 * [glow] adds a soft radial highlight in the top-left corner; use it sparingly for hero panels.
 */
@Composable
fun SystemPanel(
    modifier: Modifier = Modifier,
    accent: Color = VrddhihColors.Electric,
    glow: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(VrddhihSpacing.lg),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(VrddhihRadii.lg)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(VrddhihBrushes.Panel)
            .then(if (glow) Modifier.cornerGlow(accent) else Modifier)
            .border(VrddhihDimens.BorderWidth, VrddhihBrushes.panelBorder(accent), shape)
            .padding(contentPadding),
        content = content,
    )
}

private fun Modifier.cornerGlow(accent: Color): Modifier = drawBehind {
    if (size.minDimension <= 0f) return@drawBehind
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(accent.copy(alpha = 0.22f), Color.Transparent),
            center = Offset(size.width * 0.12f, 0f),
            radius = size.maxDimension * 0.75f,
        ),
    )
}
