package com.vrddhih.app.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vrddhih.app.core.theme.VrddhihBrushes
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens

@Composable
fun VrddhihBottomBar(
    destinations: List<TopLevelDestination>,
    isSelected: (TopLevelDestination) -> Boolean,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(VrddhihDimens.BorderWidth)
                .background(VrddhihBrushes.Divider),
        )
        NavigationBar(
            containerColor = VrddhihColors.Abyss,
            contentColor = VrddhihColors.TextSecondary,
            tonalElevation = 0.dp,
        ) {
            destinations.forEach { destination ->
                val selected = isSelected(destination)
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(destination) },
                    icon = {
                        Icon(
                            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = null,
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(destination.labelRes),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VrddhihColors.Frost,
                        selectedTextColor = VrddhihColors.Ice,
                        indicatorColor = VrddhihColors.Electric.copy(alpha = 0.22f),
                        unselectedIconColor = VrddhihColors.TextMuted,
                        unselectedTextColor = VrddhihColors.TextMuted,
                    ),
                )
            }
        }
    }
}
