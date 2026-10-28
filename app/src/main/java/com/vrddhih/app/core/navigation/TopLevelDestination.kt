package com.vrddhih.app.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.ui.graphics.vector.ImageVector
import com.vrddhih.app.R

enum class TopLevelDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    DASHBOARD("dashboard", R.string.nav_dashboard, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    ACTIVITIES("activities", R.string.nav_activities, Icons.Filled.Checklist, Icons.Outlined.Checklist),
    GOALS("goals", R.string.nav_goals, Icons.Filled.Flag, Icons.Outlined.Flag),
    FINANCE("finance", R.string.nav_finance, Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
}
