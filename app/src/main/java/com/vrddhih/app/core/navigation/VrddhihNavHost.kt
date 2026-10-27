package com.vrddhih.app.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrddhih.app.features.dashboard.DashboardRoute
import com.vrddhih.app.features.finance.FinanceScreen
import com.vrddhih.app.features.goals.GoalsRoute
import com.vrddhih.app.features.habits.HabitsScreen
import com.vrddhih.app.features.quests.QuestsRoute
import com.vrddhih.app.features.quests.QuestsViewModel
import com.vrddhih.app.features.statistics.StatisticsScreen
import java.time.LocalDate

@Composable
fun VrddhihNavHost(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToCreateQuest: () -> Unit = {},
    onNavigateToEditQuest: (Long, LocalDate) -> Unit = { _, _ -> },
    questsViewModel: QuestsViewModel = viewModel(factory = QuestsViewModel.Factory),
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        key = { page -> TopLevelDestination.entries[page].route },
    ) { page ->
        when (TopLevelDestination.entries[page]) {
            TopLevelDestination.DASHBOARD -> DashboardRoute()
            TopLevelDestination.QUESTS -> QuestsRoute(
                onNavigateToCategories = onNavigateToCategories,
                onNavigateToCreateQuest = onNavigateToCreateQuest,
                onNavigateToEditQuest = onNavigateToEditQuest,
                viewModel = questsViewModel,
            )
            TopLevelDestination.HABITS -> HabitsScreen()
            TopLevelDestination.GOALS -> GoalsRoute()
            TopLevelDestination.FINANCE -> FinanceScreen()
            TopLevelDestination.STATS -> StatisticsScreen()
        }
    }
}

@Composable
fun VrddhihNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState(initialPage = 0, pageCount = { TopLevelDestination.entries.size }),
) {
    VrddhihNavHost(pagerState = pagerState, modifier = modifier)
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
