package com.vrddhih.app.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object VrddhihColors {
    // Ultra-ultra-dark pure black background and near-black surfaces
    val Void = Color(0xFF000000)          // Pure black app background (#000000)
    val Abyss = Color(0xFF060606)         // Deepest surface / navigation bar (~#060606)
    val Panel = Color(0xFF0A0A0A)         // Primary surface (cards, sheets, panels, dialogs) (~#0A0A0A)
    val PanelRaised = Color(0xFF101010)   // Secondary surface (elevated cards, inputs, buttons) (~#101010)
    val PanelHigh = Color(0xFF161616)     // Highlights, active control surfaces (~#161616)
    val EmblemCore = Color(0xFF0B0D12)

    // Restrained dark gray borders and dividers
    val Outline = Color(0xFF1A1A1E)
    val OutlineBright = Color(0xFF26262C)

    // Ultra-dark Neon Accent Palette
    val NeonGreen = Color(0xFF59F581)     // Habit Tick vibrant neon green
    val NeonGreenBright = Color(0xFF7DFF9E) // Success gradient luminous highlight
    // Dynamic brand accents backed by active theme
    val Electric: Color
        get() = VrddhihThemeState.currentTheme.primaryAccent

    val Cyan: Color
        get() = VrddhihThemeState.currentTheme.secondaryAccent

    val Violet = Color(0xFFB388FF)        // Neon violet / royal
    val Pink = Color(0xFFFF72C6)          // Vibrant pink
    val Amber = Color(0xFFFFC45C)         // Warm neon amber
    val Coral = Color(0xFFFF7777)         // Neon coral
    val Gold = Color(0xFFFFD700)          // Category & reward gold accent
    val GoldDark = Color(0xFF8A6D05)      // Deeper gold for gradient shading

    // Floating action button accents backed by active theme's independent mapping
    val Orange: Color
        get() = VrddhihThemeState.currentTheme.fabColor

    val OrangePressed: Color
        get() = VrddhihThemeState.currentTheme.fabPressedColor

    val OrangeDarkIcon = Color(0xFF1A0A00)// Deep near-black icon (#1A0A00) for strong contrast

    // High-contrast typography tokens
    val TextPrimary = Color(0xFFF5F6F8)   // Near-white primary text
    val TextSecondary = Color(0xFF9E9EA8) // Muted gray with accessible contrast
    val TextMuted = Color(0xFF686875)     // De-emphasized metadata

    // Semantic roles mapped to neon tokens
    val Ice: Color
        get() = VrddhihThemeState.currentTheme.secondaryAccent
    val Frost = Color(0xFFE2F4FF)         // Crisp high-energy readouts
    val Success = NeonGreen               // Completed checkmarks, affirmative actions
    val Warning = Amber                   // Caution, threshold states
    val Danger = Coral                    // Overdue, critical, destructive actions
    val Sample = Violet                   // Mock / sample data indicator
}

/** Glassmorphism design tokens for translucent surfaces, specular rims, and blur underlays. */
object VrddhihGlass {
    val SurfaceTop = Color(0x750C0E18)
    val SurfaceBottom = Color(0x8F060810)
    val SpecularTop = Color(0x26FFFFFF)
    val SpecularMid = Color(0x08FFFFFF)
    val BorderTop = Color(0x52FFFFFF)
    val BorderMid = Color(0x29FFFFFF)
    val BorderBottom = Color(0x1AFFFFFF)
    val ShadowAmbient = Color(0x33000000)
    val ShadowSpot = Color(0x4D000000)
    val FallbackSurface = Color(0x4006080E)

    val SurfaceBrush = Brush.verticalGradient(listOf(SurfaceTop, SurfaceBottom))
    val SpecularBrush = Brush.verticalGradient(listOf(SpecularTop, SpecularMid, Color.Transparent))
    val BorderBrush = Brush.verticalGradient(listOf(BorderTop, BorderMid, BorderBottom))
}

object VrddhihBrushes {
    val Backdrop = Brush.verticalGradient(listOf(VrddhihColors.Void, VrddhihColors.Void))
    val Panel = Brush.verticalGradient(listOf(VrddhihColors.PanelRaised, VrddhihColors.Panel))
    val Energy: Brush
        get() = Brush.horizontalGradient(listOf(VrddhihColors.Electric, VrddhihColors.Cyan))
    val EnergyVertical: Brush
        get() = Brush.verticalGradient(listOf(VrddhihColors.Cyan, VrddhihColors.Electric))
    val NeonSuccess = Brush.horizontalGradient(listOf(VrddhihColors.NeonGreen, VrddhihColors.NeonGreenBright))
    val Emblem = Brush.linearGradient(listOf(VrddhihColors.PanelRaised, VrddhihColors.Panel))
    val Divider = Brush.horizontalGradient(
        listOf(Color.Transparent, VrddhihColors.OutlineBright.copy(alpha = 0.5f), Color.Transparent),
    )

    fun panelBorder(accent: Color): Brush = Brush.linearGradient(
        listOf(accent.copy(alpha = 0.5f), VrddhihColors.Outline, accent.copy(alpha = 0.2f)),
    )
}
