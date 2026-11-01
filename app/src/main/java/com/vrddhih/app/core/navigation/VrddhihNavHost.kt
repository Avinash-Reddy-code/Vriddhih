package com.vrddhih.app.core.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrddhih.app.features.activities.ActivitiesRoute
import com.vrddhih.app.features.dashboard.DashboardRoute
import com.vrddhih.app.features.dashboard.DashboardViewModel
import com.vrddhih.app.features.activities.ActivityTab
import com.vrddhih.app.features.finance.FinanceScreen
import com.vrddhih.app.features.goals.GoalsRoute
import com.vrddhih.app.features.goals.GoalsViewModel
import com.vrddhih.app.features.habits.HabitsViewModel
import com.vrddhih.app.features.quests.QuestsViewModel
import java.time.LocalDate

@Composable
fun VrddhihNavHost(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToCreateQuest: () -> Unit = {},
    onNavigateToEditQuest: (Long, LocalDate) -> Unit = { _, _ -> },
    onNavigateToCreateHabit: () -> Unit = {},
    onNavigateToEditHabit: (Long) -> Unit = {},
    questsViewModel: QuestsViewModel = viewModel(factory = QuestsViewModel.Factory),
    dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
    habitsViewModel: HabitsViewModel = viewModel(factory = HabitsViewModel.Factory),
    goalsViewModel: GoalsViewModel = viewModel(factory = GoalsViewModel.Factory),
    selectedActivityTab: ActivityTab = ActivityTab.TASKS,
    onActivityTabSelected: (ActivityTab) -> Unit = {},
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        key = { page -> TopLevelDestination.entries[page].route },
        beyondViewportPageCount = 1,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = 850f,
            ),
        ),
    ) { page ->
        when (TopLevelDestination.entries[page]) {
            TopLevelDestination.DASHBOARD -> DashboardRoute(
                onNavigateToCategories = onNavigateToCategories,
                onNavigateToStats = onNavigateToStats,
                viewModel = dashboardViewModel,
            )
            TopLevelDestination.ACTIVITIES -> ActivitiesRoute(
                parentPagerState = pagerState,
                onNavigateToCategories = onNavigateToCategories,
                onNavigateToCreateQuest = onNavigateToCreateQuest,
                onNavigateToEditQuest = onNavigateToEditQuest,
                onNavigateToCreateHabit = onNavigateToCreateHabit,
                onNavigateToEditHabit = onNavigateToEditHabit,
                questsViewModel = questsViewModel,
                habitsViewModel = habitsViewModel,
                selectedTab = selectedActivityTab,
                onTabSelected = onActivityTabSelected,
            )
            TopLevelDestination.GOALS -> GoalsRoute(
                viewModel = goalsViewModel,
            )
            TopLevelDestination.FINANCE -> FinanceScreen()
        }
    }
}

@Composable
fun VrddhihNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState(initialPage = 0, pageCount = { TopLevelDestination.entries.size }),
) {
    VrddhihNavHost(
        pagerState = pagerState,
        modifier = modifier,
        onNavigateToCategories = { navController.navigate("categories") },
        onNavigateToStats = { navController.navigate("statistics") },
    )
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
