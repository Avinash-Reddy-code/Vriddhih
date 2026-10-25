package com.vrddhih.app.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector
import com.vrddhih.app.R

enum class TopLevelDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    DASHBOARD("dashboard", R.string.nav_dashboard, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    QUESTS("quests", R.string.nav_quests, Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt),
    HABITS("habits", R.string.nav_habits, Icons.Filled.Autorenew, Icons.Outlined.Autorenew),
    GOALS("goals", R.string.nav_goals, Icons.Filled.Flag, Icons.Outlined.Flag),
    FINANCE("finance", R.string.nav_finance, Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    STATS("statistics", R.string.nav_stats, Icons.Filled.Insights, Icons.Outlined.Insights),
}
