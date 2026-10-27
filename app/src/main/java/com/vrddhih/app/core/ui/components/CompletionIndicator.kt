package com.vrddhih.app.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii

/**
 * Ultra-dark neon completion indicator inspired by the Habit Tick hero visual reference.
 *
 * When checked:
 * - Renders a vibrant, solid neon green rounded-square container ([VrddhihColors.NeonGreen]).
 * - Displays a high-contrast near-black checkmark icon ([VrddhihColors.Void]).
 * - Projects a soft, restrained ambient glow halo radiating into the dark background.
 *
 * When unchecked:
 * - Renders a clean, dark charcoal surface with restrained border.
 *
 * Adheres to accessibility requirements: guarantees a minimum 48 × 48 dp touch envelope
 * while presenting a crisp, compact visual graphic.
 *
 * @param checked Whether the item is completed/active.
 * @param onCheckedChange Callback invoked when tapped.
 * @param modifier Outer modifier applied to the touch envelope.
 * @param enabled Whether the control is interactive.
 * @param accent Accent color when checked; defaults to [VrddhihColors.NeonGreen].
 * @param visualSize Visual dimension of the indicator (e.g. 26 dp).
 * @param contentDescription Optional accessible description for screen readers.
 */
@Composable
fun VrddhihCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = VrddhihColors.NeonGreen,
    visualSize: Dp = 26.dp,
    contentDescription: String? = null,
) {
    val shape = RoundedCornerShape(VrddhihRadii.sm)
    val checkmarkScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "checkboxScale",
    )
    val containerColor by animateColorAsState(
        targetValue = if (checked) accent else VrddhihColors.PanelRaised,
        label = "checkboxBg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) accent else VrddhihColors.OutlineBright,
        label = "checkboxBorder",
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .then(
                if (onCheckedChange != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = false, radius = 24.dp, color = accent),
                        enabled = enabled,
                        role = Role.Checkbox,
                        onClick = { onCheckedChange(!checked) },
                    )
                } else {
                    Modifier
                },
            )
            .semantics {
                role = Role.Checkbox
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(visualSize)
                .then(if (checked) Modifier.neonAmbientGlow(accent, radius = 10.dp, alpha = 0.35f) else Modifier)
                .clip(shape)
                .background(containerColor)
                .border(VrddhihDimens.BorderWidthThick, borderColor, shape),
            contentAlignment = Alignment.Center,
        ) {
            if (checkmarkScale > 0.05f) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = contentDescription,
                    tint = VrddhihColors.Void,
                    modifier = Modifier
                        .size(visualSize * 0.65f)
                        .scale(checkmarkScale),
                )
            }
        }
    }
}
