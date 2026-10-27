package com.vrddhih.app.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object VrddhihColors {
    // Ultra-dark background and charcoal surfaces (Habit Tick reference)
    val Void = Color(0xFF0B0B0D)          // Near-black app background (~#0B0B0D)
    val Abyss = Color(0xFF101014)         // Deepest surface / navigation bar
    val Panel = Color(0xFF17171B)         // Primary surface (cards, sheets, panels)
    val PanelRaised = Color(0xFF202026)   // Secondary surface (elevated cards, inputs, buttons)
    val PanelHigh = Color(0xFF282832)     // Highlights, active control surfaces
    val EmblemCore = Color(0xFF191D28)

    // Restrained dark gray borders and dividers
    val Outline = Color(0xFF26262E)
    val OutlineBright = Color(0xFF383844)

    // Ultra-dark Neon Accent Palette
    val NeonGreen = Color(0xFF59F581)     // Habit Tick vibrant neon green
    val Electric = Color(0xFF5795FF)      // Electric blue
    val Cyan = Color(0xFF38E5E5)          // Vivid cyan / ice energy
    val Violet = Color(0xFFB388FF)        // Neon violet / royal
    val Pink = Color(0xFFFF72C6)          // Vibrant pink
    val Amber = Color(0xFFFFC45C)         // Warm neon amber
    val Coral = Color(0xFFFF7777)         // Neon coral

    // High-contrast typography tokens
    val TextPrimary = Color(0xFFF5F6F8)   // Near-white primary text
    val TextSecondary = Color(0xFF9E9EA8) // Muted gray with accessible contrast
    val TextMuted = Color(0xFF686875)     // De-emphasized metadata

    // Semantic roles mapped to neon tokens
    val Ice = Cyan                        // System framing & info labels
    val Frost = Color(0xFFE2F4FF)         // Crisp high-energy readouts
    val Success = NeonGreen               // Completed checkmarks, affirmative actions
    val Warning = Amber                   // Caution, threshold states
    val Danger = Coral                    // Overdue, critical, destructive actions
    val Sample = Violet                   // Mock / sample data indicator
}

object VrddhihBrushes {
    val Backdrop = Brush.verticalGradient(listOf(VrddhihColors.Void, VrddhihColors.Void))
    val Panel = Brush.verticalGradient(listOf(VrddhihColors.PanelRaised, VrddhihColors.Panel))
    val Energy = Brush.horizontalGradient(listOf(VrddhihColors.Electric, VrddhihColors.Cyan))
    val EnergyVertical = Brush.verticalGradient(listOf(VrddhihColors.Cyan, VrddhihColors.Electric))
    val NeonSuccess = Brush.horizontalGradient(listOf(VrddhihColors.NeonGreen, Color(0xFF7DFF9E)))
    val Emblem = Brush.linearGradient(listOf(VrddhihColors.PanelRaised, VrddhihColors.Panel))
    val Divider = Brush.horizontalGradient(
        listOf(Color.Transparent, VrddhihColors.OutlineBright.copy(alpha = 0.5f), Color.Transparent),
    )

    fun panelBorder(accent: Color): Brush = Brush.linearGradient(
        listOf(accent.copy(alpha = 0.5f), VrddhihColors.Outline, accent.copy(alpha = 0.2f)),
    )
}
