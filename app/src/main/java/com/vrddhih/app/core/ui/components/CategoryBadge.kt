package com.vrddhih.app.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing

/**
 * A cute, royal category icon badge with subtle depth and jewel-tone accents.
 * Designed to look friendly and premium against the dark navy theme.
 */
@Composable
fun CuteCategoryBadge(
    category: CategoryEntity?,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    iconSize: Dp = 20.dp,
    isSelected: Boolean = false,
) {
    val accentColor = category?.let { Color(it.colorArgb) } ?: VrddhihColors.Ice
    val iconVector = CategoryIcons.byKey(category?.iconKey ?: "more_horiz")
    val shape = RoundedCornerShape(12.dp)
    val isGold = (category?.id == "builtin_money" || category?.colorArgb == 0xFFFFD700.toInt())

    val backgroundBrush = if (isGold) {
        Brush.linearGradient(
            listOf(
                Color(0xFFFFD700).copy(alpha = if (isSelected) 0.35f else 0.22f),
                Color(0xFF8A6D05).copy(alpha = if (isSelected) 0.25f else 0.12f),
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                accentColor.copy(alpha = if (isSelected) 0.30f else 0.18f),
                VrddhihColors.PanelRaised,
            )
        )
    }

    val borderBrush = if (isSelected) {
        Brush.linearGradient(listOf(VrddhihColors.Ice, VrddhihColors.Electric))
    } else {
        Brush.linearGradient(
            listOf(
                accentColor.copy(alpha = 0.50f),
                accentColor.copy(alpha = 0.18f),
            )
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(backgroundBrush)
            .border(if (isSelected) 1.5.dp else 1.dp, borderBrush, shape),
        contentAlignment = Alignment.Center,
    ) {
        // Subtle soft inner circular glow for cute depth
        Box(
            modifier = Modifier
                .size(size * 0.65f)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.14f))
        )
        Icon(
            imageVector = iconVector,
            contentDescription = category?.name,
            tint = accentColor,
            modifier = Modifier.size(iconSize),
        )
    }
}

/**
 * Compact category selection tile used in the New/Edit Quest form.
 * Displays the cute royal badge, category name, and an intuitive selected state.
 */
@Composable
fun CategorySelectionTile(
    category: CategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = Color(category.colorArgb)
    val tileShape = RoundedCornerShape(VrddhihRadii.md)
    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isSelected) 1.5.dp else 0.dp,
        label = "tileBorderWidth",
    )
    val tileBgColor by animateColorAsState(
        targetValue = if (isSelected) VrddhihColors.Electric.copy(alpha = 0.14f) else Color.Transparent,
        label = "tileBgColor",
    )

    Column(
        modifier = modifier
            .widthIn(min = 68.dp, max = 80.dp)
            .clip(tileShape)
            .background(tileBgColor)
            .then(
                if (isSelected) Modifier.border(
                    animatedBorderWidth,
                    Brush.linearGradient(listOf(VrddhihColors.Ice, VrddhihColors.Electric)),
                    tileShape,
                ) else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor),
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics { selected = isSelected }
            .padding(horizontal = VrddhihSpacing.xs, vertical = VrddhihSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            CuteCategoryBadge(
                category = category,
                size = 42.dp,
                iconSize = 22.dp,
                isSelected = isSelected,
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(VrddhihColors.Electric)
                        .border(1.dp, VrddhihColors.Void, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = VrddhihColors.Void,
                        modifier = Modifier.size(9.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(VrddhihSpacing.xxs))

        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) VrddhihColors.TextPrimary else VrddhihColors.TextSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Tile to create a new custom category directly from the category selector.
 */
@Composable
fun CreateCustomCategoryTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tileShape = RoundedCornerShape(VrddhihRadii.md)

    Column(
        modifier = modifier
            .widthIn(min = 68.dp, max = 80.dp)
            .clip(tileShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = VrddhihColors.Ice),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = VrddhihSpacing.xs, vertical = VrddhihSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(VrddhihColors.Panel)
                .border(1.dp, VrddhihColors.OutlineBright, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "New Category",
                tint = VrddhihColors.Ice,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.height(VrddhihSpacing.xxs))

        Text(
            text = "+ Custom",
            style = MaterialTheme.typography.labelSmall,
            color = VrddhihColors.Ice,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Compact category pill for quest rows and detail headers.
 */
@Composable
fun CategoryPill(
    category: CategoryEntity?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val accentColor = category?.let { Color(it.colorArgb) } ?: VrddhihColors.Ice
    val shape = RoundedCornerShape(8.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .background(accentColor.copy(alpha = 0.12f))
            .border(0.75.dp, accentColor.copy(alpha = 0.35f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CuteCategoryBadge(
            category = category,
            size = 18.dp,
            iconSize = 11.dp,
        )
        Text(
            text = category?.name ?: "Other",
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}
