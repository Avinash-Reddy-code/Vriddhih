package com.vrddhih.app.features.themes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.theme.VrddhihThemeConfig
import com.vrddhih.app.core.theme.VrddhihThemeManager
import com.vrddhih.app.core.theme.VrddhihThemeState
import com.vrddhih.app.core.theme.VrddhihThemes
import com.vrddhih.app.core.ui.components.SystemPanel
import com.vrddhih.app.core.ui.components.SystemTag
import com.vrddhih.app.core.ui.components.neonAmbientGlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeTheme = VrddhihThemeState.currentTheme

    Surface(
        modifier = modifier.fillMaxSize(),
        color = VrddhihColors.Void,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VrddhihColors.Void)
                    .statusBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = VrddhihSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = VrddhihColors.TextPrimary,
                        )
                    }
                    Spacer(Modifier.width(VrddhihSpacing.xs))
                    Text(
                        text = stringResource(R.string.themes_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = VrddhihColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                HorizontalDivider(
                    color = VrddhihColors.Outline,
                    thickness = 1.dp,
                )
            }

            // Scrollable theme selection content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = VrddhihSpacing.lg,
                    end = VrddhihSpacing.lg,
                    top = VrddhihSpacing.md,
                    bottom = 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.lg),
            ) {
                // 1. Live Active Theme Preview Card
                item(key = "theme_preview") {
                    ThemePreviewCard(activeTheme = activeTheme)
                }

                // 2. Default theme row with reset option
                item(key = "default_theme_section") {
                    DefaultThemeSelectorRow(
                        selected = activeTheme.id == VrddhihThemes.DEFAULT_ID,
                        onSelect = { VrddhihThemeManager.selectTheme(VrddhihThemes.Default) },
                    )
                }

                // 3. Core Colors section (4 top swatches)
                item(key = "core_colors_section") {
                    ThemeSectionHeader(title = stringResource(R.string.themes_core_heading))
                    Spacer(Modifier.height(VrddhihSpacing.sm))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        VrddhihThemes.coreThemes.forEach { theme ->
                            ThemeSwatch(
                                theme = theme,
                                isSelected = activeTheme.id == theme.id,
                                onSelect = { VrddhihThemeManager.selectTheme(theme) },
                                size = 52.dp,
                            )
                        }
                    }
                }

                // 4. Premium Accent Colors section (Grid of 18 solid swatches)
                item(key = "premium_accent_section") {
                    ThemeSectionHeader(title = stringResource(R.string.themes_premium_heading))
                    Spacer(Modifier.height(VrddhihSpacing.sm))
                    ThemeGrid(
                        themes = VrddhihThemes.premiumAccentThemes,
                        activeThemeId = activeTheme.id,
                        onSelect = { VrddhihThemeManager.selectTheme(it) },
                    )
                }

                // 5. Curated Two-Tone Presets section (Grid of 12 two-tone swatches)
                item(key = "twotone_presets_section") {
                    ThemeSectionHeader(title = stringResource(R.string.themes_twotone_heading))
                    Spacer(Modifier.height(VrddhihSpacing.sm))
                    ThemeGrid(
                        themes = VrddhihThemes.twoTonePresets,
                        activeThemeId = activeTheme.id,
                        onSelect = { VrddhihThemeManager.selectTheme(it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = VrddhihColors.TextPrimary,
    )
}

/**
 * Responsive 6-column grid for color swatches matching the reference design.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeGrid(
    themes: List<VrddhihThemeConfig>,
    activeThemeId: String,
    onSelect: (VrddhihThemeConfig) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Group into rows of 6 swatches
        themes.chunked(6).forEach { rowThemes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                rowThemes.forEach { theme ->
                    ThemeSwatch(
                        theme = theme,
                        isSelected = activeThemeId == theme.id,
                        onSelect = { onSelect(theme) },
                        size = 44.dp,
                    )
                }
                // Pad empty slots if row has fewer than 6 items
                repeat(6 - rowThemes.size) {
                    Spacer(Modifier.size(44.dp))
                }
            }
        }
    }
}

/**
 * Individual circular swatch with selection double-outline ring matching the reference image.
 */
@Composable
private fun ThemeSwatch(
    theme: VrddhihThemeConfig,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 44.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(stiffness = 900f),
        label = "swatch_scale",
    )

    // Selection ring size: swatch diameter + 8.dp to create concentric gap
    val ringSize = size + 8.dp

    Box(
        modifier = modifier
            .size(ringSize)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.RadioButton,
                onClick = onSelect,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Outer selection ring (visible only when selected)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(ringSize)
                    .clip(CircleShape)
                    .border(2.dp, theme.primaryAccent, CircleShape),
            )
        }

        // Inner circular color swatch
        if (theme.isTwoTone && theme.innerCoreColor != null) {
            // Two-tone: Outer rim in primaryAccent, centered inner core circle in innerCoreColor
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(theme.primaryAccent),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(size * 0.58f)
                        .clip(CircleShape)
                        .background(theme.innerCoreColor),
                )
            }
        } else {
            // Solid circular swatch
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(theme.primaryAccent),
            )
        }
    }
}

/**
 * Dedicated tile for selecting or resetting to the default Ultra Dark theme.
 */
@Composable
private fun DefaultThemeSelectorRow(
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val defaultTheme = VrddhihThemes.Default

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(VrddhihRadii.md),
        color = VrddhihColors.Panel,
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) defaultTheme.primaryAccent else VrddhihColors.Outline,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VrddhihSpacing.md, vertical = VrddhihSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
            ) {
                // Swatch representation of default theme with Electric/Cyan split
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(defaultTheme.primaryAccent, defaultTheme.secondaryAccent),
                            ),
                        )
                        .border(1.dp, VrddhihColors.OutlineBright, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = VrddhihColors.Void,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Column {
                    Text(
                        text = defaultTheme.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = VrddhihColors.TextPrimary,
                    )
                    Text(
                        text = "Electric Blue #5795FF · Orange + Button",
                        style = MaterialTheme.typography.labelSmall,
                        color = VrddhihColors.TextMuted,
                    )
                }
            }

            if (selected) {
                SystemTag(
                    text = stringResource(R.string.themes_active_label).uppercase(),
                    color = defaultTheme.primaryAccent,
                )
            } else {
                OutlinedButton(
                    onClick = onSelect,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(VrddhihRadii.sm),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VrddhihColors.OutlineBright),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = VrddhihColors.TextSecondary,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Default",
                        style = MaterialTheme.typography.labelSmall,
                        color = VrddhihColors.TextSecondary,
                    )
                }
            }
        }
    }
}

/**
 * Live interactive status preview panel reflecting the chosen accent,
 * brand progress indicator, and the contrasting floating + button.
 */
@Composable
private fun ThemePreviewCard(
    activeTheme: VrddhihThemeConfig,
    modifier: Modifier = Modifier,
) {
    SystemPanel(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.themes_preview_title).uppercase(),
                    style = VrddhihTextStyles.TagLabel,
                    color = VrddhihColors.TextMuted,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = activeTheme.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = activeTheme.primaryAccent,
                )
            }

            SystemTag(
                text = stringResource(R.string.themes_active_label).uppercase(),
                color = activeTheme.primaryAccent,
            )
        }

        Spacer(Modifier.height(VrddhihSpacing.md))

        // Sample interactive progress bar using theme accent
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.themes_preview_directive),
                    style = MaterialTheme.typography.bodySmall,
                    color = VrddhihColors.TextSecondary,
                )
                Text(
                    text = "75%",
                    style = MaterialTheme.typography.bodySmall,
                    color = activeTheme.primaryAccent,
                    fontWeight = FontWeight.Bold,
                )
            }
            LinearProgressIndicator(
                progress = { 0.75f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = activeTheme.primaryAccent,
                trackColor = VrddhihColors.PanelRaised,
            )
        }

        Spacer(Modifier.height(VrddhihSpacing.md))

        // Mini preview showing theme primary action button and complementary FAB
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Theme accent button
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = activeTheme.primaryAccent,
                    contentColor = VrddhihColors.Void,
                ),
                shape = RoundedCornerShape(VrddhihRadii.sm),
                modifier = Modifier.height(36.dp),
            ) {
                Text(
                    text = stringResource(R.string.themes_preview_action),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = VrddhihColors.Void,
                )
            }

            // Preview of the independent contrasting floating + button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
            ) {
                Text(
                    text = "+ Button:",
                    style = MaterialTheme.typography.labelSmall,
                    color = VrddhihColors.TextMuted,
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .neonAmbientGlow(
                            color = activeTheme.fabGlowColor,
                            radius = 6.dp,
                            alpha = 0.40f,
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(activeTheme.fabColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = VrddhihColors.Void,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
