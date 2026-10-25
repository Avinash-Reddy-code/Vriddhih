package com.vrddhih.app.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VrddhihColorScheme = darkColorScheme(
    primary = VrddhihColors.Electric,
    onPrimary = VrddhihColors.Void,
    primaryContainer = VrddhihColors.EmblemCore,
    onPrimaryContainer = VrddhihColors.Frost,
    secondary = VrddhihColors.Ice,
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
    surfaceContainerHighest = VrddhihColors.PanelRaised,
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
    MaterialTheme(
        colorScheme = VrddhihColorScheme,
        typography = VrddhihTypography,
        shapes = VrddhihShapes,
        content = content,
    )
}
