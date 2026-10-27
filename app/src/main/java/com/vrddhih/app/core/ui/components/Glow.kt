package com.vrddhih.app.core.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Renders a soft, restrained ambient radial glow behind an element.
 *
 * Inspired by the Habit Tick hero indicator: an intense vibrant center with a smooth,
 * low-opacity halo fading outward into the dark background without causing blur lag or
 * layout jitter.
 *
 * @param color The accent glow color (e.g. [com.vrddhih.app.core.theme.VrddhihColors.NeonGreen]).
 * @param radius Radius of the soft halo around the component.
 * @param alpha Peak opacity at the inner glow boundary (defaults to 0.28f for restrained elegance).
 */
fun Modifier.neonAmbientGlow(
    color: Color,
    radius: Dp = 14.dp,
    alpha: Float = 0.28f,
): Modifier = drawBehind {
    if (size.minDimension <= 0f) return@drawBehind
    val spreadPx = radius.toPx()
    val center = Offset(size.width / 2f, size.height / 2f)
    val maxRadius = (maxOf(size.width, size.height) / 2f) + spreadPx

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha),
                color.copy(alpha = alpha * 0.45f),
                Color.Transparent,
            ),
            center = center,
            radius = maxRadius,
        ),
        radius = maxRadius,
        center = center,
    )
}

/**
 * Renders a soft ambient rounded halo behind buttons and pill indicators.
 *
 * Designed for primary action buttons (like "Let's Go ->" in Habit Tick) and active
 * navigation/filter pills.
 *
 * @param color The accent glow color.
 * @param spread Outset distance for the soft halo.
 * @param cornerRadius The corner radius of the halo shape.
 * @param alpha Peak opacity of the glow.
 */
fun Modifier.neonPillGlow(
    color: Color,
    spread: Dp = 6.dp,
    cornerRadius: Dp = 14.dp,
    alpha: Float = 0.22f,
): Modifier = drawBehind {
    if (size.minDimension <= 0f) return@drawBehind
    val spreadPx = spread.toPx()
    val crPx = cornerRadius.toPx() + spreadPx

    // Layer 1: Broad soft atmospheric aura
    drawRoundRect(
        color = color.copy(alpha = alpha * 0.4f),
        topLeft = Offset(-spreadPx, -spreadPx),
        size = Size(size.width + spreadPx * 2f, size.height + spreadPx * 2f),
        cornerRadius = CornerRadius(crPx, crPx),
    )

    // Layer 2: Inner concentrated glow fringe
    val halfSpread = spreadPx * 0.5f
    drawRoundRect(
        color = color.copy(alpha = alpha),
        topLeft = Offset(-halfSpread, -halfSpread),
        size = Size(size.width + spreadPx, size.height + spreadPx),
        cornerRadius = CornerRadius(crPx - halfSpread, crPx - halfSpread),
    )
}

/**
 * Status window corner glow for hero panels: an atmospheric radial highlight in the top-left corner.
 */
fun Modifier.cornerGlow(accent: Color, alpha: Float = 0.20f): Modifier = drawBehind {
    if (size.minDimension <= 0f) return@drawBehind
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(accent.copy(alpha = alpha), Color.Transparent),
            center = Offset(size.width * 0.12f, 0f),
            radius = size.maxDimension * 0.75f,
        ),
    )
}
