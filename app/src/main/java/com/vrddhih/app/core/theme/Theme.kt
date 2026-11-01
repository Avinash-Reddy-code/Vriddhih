package com.vrddhih.app.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

private fun createVrddhihColorScheme(theme: VrddhihThemeConfig) = darkColorScheme(
    primary = theme.primaryAccent,
    onPrimary = VrddhihColors.Void,
    primaryContainer = VrddhihColors.EmblemCore,
    onPrimaryContainer = VrddhihColors.Frost,
    secondary = theme.secondaryAccent,
    onSecondary = VrddhihColors.Void,
    tertiary = VrddhihColors.Success,
    onTertiary = VrddhihColors.Void,
    background = VrddhihColors.Void,
    onBackground = VrddhihColors.TextPrimary,
    surface = VrddhihColors.Panel,
    onSurface = VrddhihColors.TextPrimary,
    surfaceVariant = VrddhihColors.PanelRaised,
    onSurfaceVariant = VrddhihColors.TextSecondary,
    surfaceContainerLowest = VrddhihColors.Void,
    surfaceContainerLow = VrddhihColors.Abyss,
    surfaceContainer = VrddhihColors.Panel,
    surfaceContainerHigh = VrddhihColors.PanelRaised,
    surfaceContainerHighest = VrddhihColors.PanelHigh,
    outline = VrddhihColors.OutlineBright,
    outlineVariant = VrddhihColors.Outline,
    error = VrddhihColors.Danger,
    onError = VrddhihColors.Void,
)

private val VrddhihShapes = Shapes(
    extraSmall = RoundedCornerShape(VrddhihRadii.xs),
    small = RoundedCornerShape(VrddhihRadii.sm),
    medium = RoundedCornerShape(VrddhihRadii.md),
    large = RoundedCornerShape(VrddhihRadii.lg),
)

/** Vrddhih is dark-only by design: the hunter-system look does not have a light variant. */
@Composable
fun VrddhihTheme(content: @Composable () -> Unit) {
    val theme = VrddhihThemeState.currentTheme
    val colorScheme = remember(theme) { createVrddhihColorScheme(theme) }

    CompositionLocalProvider(
        LocalVrddhihTheme provides theme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = VrddhihTypography,
            shapes = VrddhihShapes,
            content = content,
        )
    }
}
