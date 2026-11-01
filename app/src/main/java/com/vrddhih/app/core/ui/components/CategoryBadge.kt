package com.vrddhih.app.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
 * Unified category icon badge matching the main category section design:
 * - Rounded-square container with subtle neon ambient glow
 * - Solid category color background
 * - Near-black ([VrddhihColors.Void]) centered vector icon for high-contrast clarity
 */
@Composable
fun CuteCategoryBadge(
    category: CategoryEntity?,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    iconSize: Dp = 20.dp,
    isSelected: Boolean = false,
) = CuteCategoryBadge(
    iconKey = category?.iconKey,
    colorArgb = category?.colorArgb,
    modifier = modifier,
    size = size,
    iconSize = iconSize,
    isSelected = isSelected,
    contentDescription = category?.name,
)

/**
 * Overload for components that supply [iconKey] and [colorArgb] directly.
 */
@Composable
fun CuteCategoryBadge(
    iconKey: String?,
    colorArgb: Int?,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    iconSize: Dp = 20.dp,
    isSelected: Boolean = false,
    contentDescription: String? = null,
) {
    val hasColor = colorArgb != null
    val accentColor = if (hasColor) Color(colorArgb!!) else VrddhihColors.Ice
    val iconVector = CategoryIcons.byKey(iconKey ?: "more_horiz")
    val shape = RoundedCornerShape((size * 0.26f).coerceIn(6.dp, 16.dp))

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (hasColor) {
                    Modifier.neonAmbientGlow(
                        color = accentColor,
                        radius = (size * 0.22f).coerceAtLeast(4.dp),
                        alpha = 0.28f,
                    )
                } else Modifier
            )
            .clip(shape)
            .background(if (hasColor) accentColor else VrddhihColors.PanelRaised)
            .then(
                if (!hasColor) {
                    Modifier.border(1.dp, VrddhihColors.Outline, shape)
                } else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = contentDescription,
            tint = if (hasColor) VrddhihColors.Void else VrddhihColors.Ice,
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

    Column(
        modifier = modifier
            .widthIn(min = 64.dp, max = 80.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor, bounded = false, radius = 32.dp),
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
                size = 44.dp,
                iconSize = 22.dp,
                isSelected = isSelected,
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(VrddhihColors.Ice)
                        .border(1.dp, VrddhihColors.Void, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = VrddhihColors.Void,
                        modifier = Modifier.size(10.dp),
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
    Column(
        modifier = modifier
            .widthIn(min = 64.dp, max = 80.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = VrddhihColors.Ice, bounded = false, radius = 32.dp),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = VrddhihSpacing.xs, vertical = VrddhihSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
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

/**
 * Standardized horizontal category selection row for all editor forms (Quests, Habits, Goals).
 *
 * Eliminates per-editor category selection duplication by rendering a unified, horizontally
 * scrolling row of [CategorySelectionTile] items, followed by [CreateCustomCategoryTile] when
 * [onNewCategoryClick] is supplied.
 *
 * @param categories Active categories list.
 * @param selectedCategoryId Currently selected category ID, or null.
 * @param onCategorySelected Invoked with the newly selected category ID (or null if toggled off).
 * @param modifier Modifier applied to the row.
 * @param onNewCategoryClick Optional callback to trigger category creation.
 * @param allowDeselect Whether clicking the already-selected category deselects it.
 */
@Composable
fun CategoryPickerRow(
    categories: List<CategoryEntity>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    onNewCategoryClick: (() -> Unit)? = null,
    allowDeselect: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        categories.forEach { category ->
            val isSelected = category.id == selectedCategoryId
            CategorySelectionTile(
                category = category,
                isSelected = isSelected,
                onClick = {
                    val next = if (allowDeselect && isSelected) null else category.id
                    onCategorySelected(next)
                },
            )
        }

        if (onNewCategoryClick != null) {
            CreateCustomCategoryTile(onClick = onNewCategoryClick)
        }
    }
}

