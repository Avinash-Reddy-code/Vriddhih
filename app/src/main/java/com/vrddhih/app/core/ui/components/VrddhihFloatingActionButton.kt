package com.vrddhih.app.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihThemeState

/**
 * Common floating '+' action button.
 *
 * Clean visual design:
 * - One solid orange surface (#FF6A00) transitioning to #E85A00 on press.
 * - One clean rounded-square shape (18.dp corner radius, 56.dp dimensions).
 * - Centered black '+' icon (#000000).
 * - Single subtle, soft orange ambient glow with no concentric rings or nested layers.
 */
@Composable
fun VrddhihFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Add,
    contentDescription: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(stiffness = 800f),
        label = "fab_press_scale",
    )
    val currentTheme = VrddhihThemeState.currentTheme
    val containerColor by animateColorAsState(
        targetValue = if (isPressed) currentTheme.fabPressedColor else currentTheme.fabColor,
        animationSpec = tween(durationMillis = 100),
        label = "fab_container_color",
    )
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .size(56.dp)
            .neonAmbientGlow(
                color = currentTheme.fabGlowColor,
                radius = 10.dp,
                alpha = 0.30f,
            )
            .clip(shape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = VrddhihColors.Void),
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = VrddhihColors.Void,
            modifier = Modifier.size(28.dp),
        )
    }
}

