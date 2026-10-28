package com.vrddhih.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.theme.VrddhihTheme

/**
 * Prominent, anchored bottom creation action bar for primary feature list screens (Quests, Habits, Goals).
 *
 * ## Placement Contract & Integration Rules:
 *
 * 1. **Screen Root Structure**:
 *    The owning screen should be structured with a root `Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter)`.
 *    The scrolling list sits as the first child, and `AnchoredPrimaryAction` sits as the trailing child
 *    anchored at `Alignment.BottomCenter`.
 *
 * 2. **Coordination with Bottom Navigation**:
 *    The app scaffold handles bottom navigation padding via `Scaffold(bottomBar = { VrddhihBottomBar(...) }) { innerPadding -> ... }`.
 *    Therefore, `AnchoredPrimaryAction` naturally floats immediately above the bottom navigation bar without
 *    needing additional window insets or manual offset calculations.
 *
 * 3. **List Content Clearance**:
 *    The owning screen's scrolling list (`LazyColumn`) **must** set `contentPadding = PaddingValues(..., bottom = 96.dp)`.
 *    This ensures that when the user scrolls to the bottom of their content, the final item clears the 50 dp action
 *    button plus its 24 dp vertical padding (total 74 dp height), with 22 dp of comfortable breathing room.
 *
 * 4. **Display Width Bounds**:
 *    The container automatically clamps its width to [VrddhihDimens.ContentMaxWidth] (640 dp) on tablets and
 *    large screens, maintaining visual alignment with the centered content column.
 *
 * @param text The uppercase action label (e.g. `"New Quest"`, `"New Habit"`, `"New Goal"`).
 * @param onClick Invoked when the user clicks the button.
 * @param modifier Modifier applied to the outer scrim container.
 * @param icon Leading icon Vector; defaults to [Icons.Filled.Add]. Set to `null` to omit the icon.
 * @param enabled Whether the action button is active and interactive.
 * @param isLoading Whether an asynchronous creation or save operation is currently in flight.
 */
@Composable
fun AnchoredPrimaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = Icons.Filled.Add,
    accent: Color = VrddhihColors.Electric,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Box(
        modifier = modifier
            .widthIn(max = VrddhihDimens.ContentMaxWidth)
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        VrddhihColors.Void.copy(alpha = 0.88f),
                        VrddhihColors.Void,
                    ),
                ),
            )
            .padding(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.md),
    ) {
        PrimaryCreationButton(
            text = text,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            icon = icon,
            accent = accent,
            enabled = enabled,
            isLoading = isLoading,
        )
    }
}

/**
 * Standalone primary creation button styled with the Solo Leveling Hunter System visual identity.
 *
 * Visual & Accessibility Specification:
 * - **Container**: [VrddhihColors.Electric] (`#2F8CFF`).
 * - **Foreground**: [VrddhihColors.Void] (`#04060C`) providing high-contrast readability (~5.8:1).
 * - **Height**: Visual height of 50 dp, with a strict [defaultMinSize] of 48 dp meeting accessibility guidelines.
 * - **Shape**: [RoundedCornerShape] with [VrddhihRadii.md] (14 dp).
 * - **Typography**: [VrddhihTextStyles.TagLabel] uppercase with bold weight.
 * - **Elevation**: 2 dp default, 4 dp pressed, 0 dp disabled.
 * - **Loading State**: When [isLoading] is true, clicks are disabled to prevent duplicate triggers,
 *   a 18 dp [CircularProgressIndicator] replaces the icon, layout height is preserved at 50 dp,
 *   and TalkBack receives a `"Loading…"` state description.
 *
 * @param text The uppercase action label.
 * @param onClick Invoked when the button is clicked.
 * @param modifier Modifier applied to the button.
 * @param icon Leading icon Vector; defaults to [Icons.Filled.Add]. Set to `null` to omit.
 * @param enabled Whether the button is interactive.
 * @param isLoading Whether an asynchronous creation or save operation is currently in flight.
 */
@Composable
fun PrimaryCreationButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = Icons.Filled.Add,
    accent: Color = VrddhihColors.Electric,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val loadingDescription = stringResource(R.string.action_loading)
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .height(50.dp)
            .then(
                if (enabled && !isLoading) {
                    Modifier.neonPillGlow(accent, spread = 6.dp, cornerRadius = VrddhihRadii.md, alpha = 0.26f)
                } else {
                    Modifier
                },
            )
            .semantics {
                if (isLoading) {
                    stateDescription = loadingDescription
                }
            },
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(VrddhihRadii.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = accent,
            contentColor = VrddhihColors.Void,
            disabledContainerColor = if (isLoading) accent.copy(alpha = 0.85f) else VrddhihColors.PanelRaised,
            disabledContentColor = if (isLoading) VrddhihColors.Void else VrddhihColors.TextMuted,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            disabledElevation = 0.dp,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = VrddhihColors.Void,
            )
            Spacer(Modifier.width(VrddhihSpacing.sm))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (enabled) VrddhihColors.Void else VrddhihColors.TextMuted,
            )
            Spacer(Modifier.width(VrddhihSpacing.sm))
        }
        Text(
            text = text.uppercase(),
            style = VrddhihTextStyles.TagLabel,
            fontWeight = FontWeight.Bold,
            color = if (isLoading || enabled) VrddhihColors.Void else VrddhihColors.TextMuted,
        )
    }
}

/**
 * Reusable full-width or compact primary action button styled with ultra-dark neon aesthetics.
 */
@Composable
fun VrddhihPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color = VrddhihColors.NeonGreen,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    PrimaryCreationButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        accent = accent,
        enabled = enabled,
        isLoading = isLoading,
    )
}

// region Previews

@Preview(name = "Anchored Creation Actions", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun AnchoredPrimaryActionPreview() {
    VrddhihTheme {
        Surface(color = VrddhihColors.Void) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Quests anchored action
                AnchoredPrimaryAction(
                    text = stringResource(R.string.quests_new_action),
                    onClick = {},
                )

                Spacer(Modifier.height(VrddhihSpacing.md))

                // 2. Habits anchored action
                AnchoredPrimaryAction(
                    text = stringResource(R.string.habits_new_action),
                    onClick = {},
                )

                Spacer(Modifier.height(VrddhihSpacing.md))

                // 3. Goals anchored action
                AnchoredPrimaryAction(
                    text = stringResource(R.string.goals_new_action),
                    onClick = {},
                )

                Spacer(Modifier.height(VrddhihSpacing.md))

                // 4. Standalone disabled state
                Box(modifier = Modifier.padding(horizontal = VrddhihSpacing.lg)) {
                    PrimaryCreationButton(
                        text = "Action Unavailable",
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(VrddhihSpacing.md))

                // 5. Standalone loading state
                Box(modifier = Modifier.padding(horizontal = VrddhihSpacing.lg)) {
                    PrimaryCreationButton(
                        text = "Creating Quest",
                        onClick = {},
                        isLoading = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// endregion
