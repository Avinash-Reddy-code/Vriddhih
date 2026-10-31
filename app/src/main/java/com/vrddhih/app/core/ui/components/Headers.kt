package com.vrddhih.app.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihBrushes
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.theme.VrddhihTheme

/**
 * Top-of-screen title block: a small system "eyebrow" label above the screen title, with an optional
 * [action] aligned to the top right.
 *
 * Designed according to the Phase 01.2 specification:
 * - [eyebrow]: optional uppercase system directive tag (e.g. "DAILY DIRECTIVES").
 * - [title]: mandatory primary screen headline with heading accessibility semantics.
 * - [subtitle]: optional supporting context or description.
 * - [action]: optional trailing utility action slot (e.g. calendar or category picker),
 *   enforced with a 48 dp minimum touch target envelope and safety clearance.
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = if (action != null) VrddhihSpacing.md else 0.dp),
        ) {
            if (!eyebrow.isNullOrBlank()) {
                Text(
                    text = eyebrow.uppercase(),
                    style = VrddhihTextStyles.SystemLabel,
                    color = VrddhihColors.Ice,
                )
                Spacer(Modifier.height(VrddhihSpacing.xs))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = VrddhihColors.TextPrimary,
                modifier = Modifier.semantics { heading() },
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(VrddhihSpacing.xs))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VrddhihColors.TextSecondary,
                )
            }
        }
        if (action != null) {
            Box(
                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                contentAlignment = Alignment.TopEnd,
            ) {
                action()
            }
        }
    }
}

/** Section title used inside panels, with an energy accent bar and an optional trailing slot. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(VrddhihBrushes.EnergyVertical),
        )
        Spacer(Modifier.width(VrddhihSpacing.sm))
        Text(
            text = title.uppercase(),
            style = VrddhihTextStyles.SystemLabel,
            color = VrddhihColors.Frost,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        trailing?.invoke(this)
    }
}

/**
 * Shared main screen header across all primary tabs (Dashboard, Activities, Goals, Finance).
 *
 * Guarantees:
 * - Hamburger menu button fixed at the exact same position on every main tab.
 * - Dynamic heading beginning at the exact same horizontal position regardless of length.
 * - Smooth horizontal sliding text animation when switching between tabs (inspired by HabitNow).
 * - Trailing actions slot for contextual actions (e.g. Stats & Calendar on Dashboard).
 */
@Composable
fun MainScreenHeader(
    pageIndex: Int,
    title: String,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    isTitleClickable: Boolean = false,
    onTitleClick: () -> Unit = {},
    titleClickLabel: String? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = VrddhihSpacing.lg,
                end = VrddhihSpacing.lg,
                top = VrddhihSpacing.xs,
                bottom = VrddhihSpacing.xs,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
            modifier = Modifier.weight(1f, fill = false),
        ) {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.size(48.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(VrddhihColors.PanelRaised)
                        .border(1.dp, VrddhihColors.OutlineBright, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = stringResource(R.string.nav_drawer_open),
                        tint = VrddhihColors.Cyan,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            AnimatedContent(
                targetState = title,
                transitionSpec = {
                    val slideSpec = tween<IntOffset>(durationMillis = 180, easing = FastOutSlowInEasing)
                    val fadeSpec = tween<Float>(durationMillis = 160, easing = FastOutSlowInEasing)
                    (slideInVertically(
                        animationSpec = slideSpec,
                        initialOffsetY = { -it / 2 },
                    ) + fadeIn(animationSpec = fadeSpec)).togetherWith(
                        slideOutVertically(
                            animationSpec = slideSpec,
                            targetOffsetY = { it / 2 },
                        ) + fadeOut(animationSpec = fadeSpec)
                    )
                },
                label = "main_screen_heading",
            ) { displayTitle ->
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = VrddhihColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (isTitleClickable) {
                        Modifier
                            .clip(RoundedCornerShape(VrddhihRadii.sm))
                            .clickable(
                                role = Role.Button,
                                onClickLabel = titleClickLabel,
                                onClick = onTitleClick,
                            )
                            .padding(vertical = VrddhihSpacing.xxs, horizontal = VrddhihSpacing.xs)
                    } else {
                        Modifier.padding(vertical = VrddhihSpacing.xxs, horizontal = VrddhihSpacing.xs)
                    },
                )
            }
        }

        if (actions != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
            ) {
                actions()
            }
        }
    }
}

@Preview(name = "ScreenHeader Variants", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ScreenHeaderPreview() {
    VrddhihTheme {
        Surface(color = VrddhihColors.Void) {
            Column(
                modifier = Modifier.padding(VrddhihSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xl),
            ) {
                // 1. Full header with eyebrow, title, subtitle, and utility action
                ScreenHeader(
                    eyebrow = "Daily Directives",
                    title = "Quests",
                    subtitle = "Actionable hunter directives with priority and scheduling",
                    action = {
                        IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Calendar",
                                tint = VrddhihColors.Ice,
                            )
                        }
                    },
                )

                // 2. Minimal header (title only)
                ScreenHeader(title = "Objectives")

                // 3. Header without eyebrow, but with subtitle and action
                ScreenHeader(
                    title = "Habit Protocols",
                    subtitle = "Consistent daily behavioral routines",
                    action = {
                        IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Calendar",
                                tint = VrddhihColors.Ice,
                            )
                        }
                    },
                )

                // 4. Header with long title to verify safety clearance against action
                ScreenHeader(
                    eyebrow = "Tactical Evaluation",
                    title = "Extremely Long Directive Headline For Testing Safe Separation",
                    subtitle = "This subtitle must wrap safely without clipping into the trailing action button envelope.",
                    action = {
                        IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Calendar",
                                tint = VrddhihColors.Ice,
                            )
                        }
                    },
                )
            }
        }
    }
}
