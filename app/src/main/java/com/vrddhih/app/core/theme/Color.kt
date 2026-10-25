package com.vrddhih.app.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object VrddhihColors {
    val Void = Color(0xFF04060C)
    val Abyss = Color(0xFF080E1C)
    val Panel = Color(0xFF0C1426)
    val PanelRaised = Color(0xFF111C34)
    val EmblemCore = Color(0xFF12305F)
    val Outline = Color(0xFF1C2A48)
    val OutlineBright = Color(0xFF2A3D66)

    val Electric = Color(0xFF2F8CFF)
    val Ice = Color(0xFF7CD3FF)
    val Frost = Color(0xFFD4F1FF)

    val TextPrimary = Color(0xFFE8F2FF)
    val TextSecondary = Color(0xFFA3B5D1)
    val TextMuted = Color(0xFF7A8DAB)

    val Success = Color(0xFF45E0B0)
    val Warning = Color(0xFFFFB84D)
    val Danger = Color(0xFFFF5C7C)

    /** Marks sample/mock content so it is never confused with real user records. */
    val Sample = Color(0xFFB79CFF)
}

object VrddhihBrushes {
    val Backdrop = Brush.verticalGradient(listOf(VrddhihColors.Abyss, VrddhihColors.Void))
    val Panel = Brush.verticalGradient(listOf(VrddhihColors.PanelRaised, VrddhihColors.Panel))
    val Energy = Brush.horizontalGradient(listOf(VrddhihColors.Electric, VrddhihColors.Ice))
    val EnergyVertical = Brush.verticalGradient(listOf(VrddhihColors.Ice, VrddhihColors.Electric))
    val Emblem = Brush.linearGradient(listOf(VrddhihColors.EmblemCore, VrddhihColors.PanelRaised))
    val Divider = Brush.horizontalGradient(
        listOf(Color.Transparent, VrddhihColors.Electric.copy(alpha = 0.55f), Color.Transparent),
    )

    fun panelBorder(accent: Color): Brush = Brush.linearGradient(
        listOf(accent.copy(alpha = 0.75f), VrddhihColors.Outline, accent.copy(alpha = 0.35f)),
    )
}
