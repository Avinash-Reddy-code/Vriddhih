package com.vrddhih.app.core.navigation

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihGlass
import com.vrddhih.app.core.ui.components.neonAmbientGlow

/**
 * Floating Telegram-style navigation bar with ultra-dark frosted glass and glowing neon selection.
 *
 * Features:
 * - Horizontally centered, compact floating pill with generous rounded corners.
 * - Multi-layer ultra-dark translucent glass shading with specular edge highlights and blur support.
 * - Four icon-only destinations with zero text clutter.
 * - Fluid sliding indicator pill with spring physics and real-time 1:1 page drag tracking.
 * - Restrained electric ripple feedback and scale micro-animations.
 */
@Composable
fun VrddhihBottomBar(
    destinations: List<TopLevelDestination>,
    isSelected: (TopLevelDestination) -> Boolean,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    selectedPageFraction: (() -> Float)? = null,
) {
    val pillShape = RoundedCornerShape(percent = 50)
    val glassBg = VrddhihGlass.SurfaceBrush
    val glassHighlight = VrddhihGlass.SpecularBrush
    val glassBorder = VrddhihGlass.BorderBrush

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 12.dp, top = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Floating Telegram-style Navigation Pill
        Box(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .fillMaxWidth()
                .height(58.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = pillShape,
                    ambientColor = VrddhihGlass.ShadowAmbient,
                    spotColor = VrddhihGlass.ShadowSpot,
                )
                .clip(pillShape)
                .border(1.dp, glassBorder, pillShape),
            contentAlignment = Alignment.Center,
        ) {
            // Optional system blur underlay where supported (API 31+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(pillShape)
                        .blur(20.dp)
                        .background(VrddhihGlass.FallbackSurface),
                )
            }

            // Glass shading layers
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(glassBg),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(glassHighlight),
            )

            // Dynamic width-aware content and sliding indicator
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val tabCount = destinations.size.coerceAtLeast(1)
                val tabWidth = maxWidth / tabCount
                val selectedIndex = destinations.indexOfFirst { isSelected(it) }.coerceAtLeast(0)

                val targetOffset = tabWidth * selectedIndex
                val animatedOffset by animateDpAsState(
                    targetValue = targetOffset,
                    animationSpec = spring(
                        dampingRatio = 0.78f,
                        stiffness = 450f,
                    ),
                    label = "nav_indicator_offset",
                )

                // Sliding Glowing Active Indicator Pill
                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .fillMaxHeight()
                        .offset {
                            val xPx = if (selectedPageFraction != null) {
                                val fraction = selectedPageFraction().coerceIn(0f, (tabCount - 1).toFloat())
                                (tabWidth.toPx() * fraction).roundToInt()
                            } else {
                                animatedOffset.roundToPx()
                            }
                            IntOffset(x = xPx, y = 0)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .height(36.dp)
                            .neonAmbientGlow(VrddhihColors.Electric, radius = 8.dp, alpha = 0.35f)
                            .clip(pillShape)
                            .background(VrddhihColors.Electric.copy(alpha = 0.22f))
                            .border(1.dp, VrddhihColors.Electric.copy(alpha = 0.65f), pillShape),
                    )
                }

                // Foreground Interactive Icon Buttons
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    destinations.forEach { destination ->
                        val selected = isSelected(destination)
                        val label = stringResource(destination.labelRes)
                        val animatedIconScale by animateFloatAsState(
                            targetValue = if (selected) 1.08f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
                            label = "icon_scale",
                        )
                        val animatedIconColor by animateColorAsState(
                            targetValue = if (selected) VrddhihColors.Cyan else VrddhihColors.TextMuted,
                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                            label = "icon_color",
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(pillShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(
                                        bounded = false,
                                        radius = 24.dp,
                                        color = VrddhihColors.Electric.copy(alpha = 0.35f),
                                    ),
                                    role = Role.Tab,
                                    onClick = { onNavigate(destination) },
                                )
                                .semantics {
                                    this.selected = selected
                                    contentDescription = label
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = null,
                                tint = animatedIconColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .scale(animatedIconScale),
                            )
                        }
                    }
                }
            }
        }
    }
}
