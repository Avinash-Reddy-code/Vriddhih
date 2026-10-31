package com.vrddhih.app.core.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.ui.gesture.consumeHorizontalNestedScroll
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

/**
 * Shared sidebar drawer sheet with ultra-dark neon styling, date readouts,
 * and navigation links (Categories, etc.).
 */
@Composable
fun VrddhihNavigationDrawerSheet(
    selectedDate: LocalDate,
    onNavigateToCategories: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val weekday = remember(selectedDate) {
        selectedDate.dayOfWeek.getDisplayName(JavaTextStyle.FULL, Locale.getDefault())
    }
    val formattedDate = remember(selectedDate) {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())
        selectedDate.format(formatter)
    }

    ModalDrawerSheet(
        drawerContainerColor = VrddhihColors.Panel,
        drawerShape = RoundedCornerShape(
            topEnd = VrddhihRadii.md,
            bottomEnd = VrddhihRadii.md,
        ),
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .consumeHorizontalNestedScroll()
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        VrddhihColors.Electric.copy(alpha = 0.5f),
                        VrddhihColors.Electric.copy(alpha = 0.1f),
                    ),
                ),
                shape = RoundedCornerShape(
                    topEnd = VrddhihRadii.md,
                    bottomEnd = VrddhihRadii.md,
                ),
            ),
    ) {
        // Drawer Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = VrddhihSpacing.lg,
                    end = VrddhihSpacing.lg,
                    top = VrddhihSpacing.md,
                    bottom = VrddhihSpacing.xs,
                ),
        ) {
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = VrddhihColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(VrddhihSpacing.xxs))
            Text(
                text = weekday,
                style = MaterialTheme.typography.bodyMedium,
                color = VrddhihColors.Ice,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = VrddhihColors.TextMuted,
            )
        }

        Spacer(Modifier.height(VrddhihSpacing.sm))
        HorizontalDivider(
            color = VrddhihColors.Outline,
            modifier = Modifier.padding(horizontal = VrddhihSpacing.lg),
        )
        Spacer(Modifier.height(VrddhihSpacing.md))

        // Navigation Items
        NavigationDrawerItem(
            label = {
                Text(
                    text = stringResource(R.string.categories_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = VrddhihColors.TextPrimary,
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Category,
                    contentDescription = null,
                    tint = VrddhihColors.Ice,
                    modifier = Modifier.size(20.dp),
                )
            },
            selected = false,
            onClick = onNavigateToCategories,
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                selectedContainerColor = VrddhihColors.Electric.copy(alpha = 0.15f),
            ),
            shape = RoundedCornerShape(VrddhihRadii.sm),
            modifier = Modifier.padding(horizontal = VrddhihSpacing.md),
        )
    }
}
