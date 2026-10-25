package com.vrddhih.app.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vrddhih.app.features.dashboard.DashboardRoute
import com.vrddhih.app.features.finance.FinanceScreen
import com.vrddhih.app.features.goals.GoalsScreen
import com.vrddhih.app.features.habits.HabitsScreen
import com.vrddhih.app.features.quests.QuestsScreen
import com.vrddhih.app.features.statistics.StatisticsScreen

private const val FADE_IN_MILLIS = 220
private const val FADE_OUT_MILLIS = 160

@Composable
fun VrddhihNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.DASHBOARD.route,
        modifier = modifier,
        enterTransition = { fadeIn(tween(FADE_IN_MILLIS)) },
        exitTransition = { fadeOut(tween(FADE_OUT_MILLIS)) },
        popEnterTransition = { fadeIn(tween(FADE_IN_MILLIS)) },
        popExitTransition = { fadeOut(tween(FADE_OUT_MILLIS)) },
    ) {
        composable(TopLevelDestination.DASHBOARD.route) { DashboardRoute() }
        composable(TopLevelDestination.QUESTS.route) { QuestsScreen() }
        composable(TopLevelDestination.HABITS.route) { HabitsScreen() }
        composable(TopLevelDestination.GOALS.route) { GoalsScreen() }
        composable(TopLevelDestination.FINANCE.route) { FinanceScreen() }
        composable(TopLevelDestination.STATS.route) { StatisticsScreen() }
    }
}

/**
 * Switches tabs without stacking duplicates: pops back to the start destination,
 * saving the state of the tab being left and restoring the state of the tab being opened.
 */
fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavDestination?.isInHierarchy(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.route == destination.route } == true
