package com.vrddhih.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.vrddhih.app.R
import com.vrddhih.app.core.navigation.TopLevelDestination
import com.vrddhih.app.core.navigation.VrddhihBottomBar
import com.vrddhih.app.core.navigation.VrddhihNavHost
import com.vrddhih.app.core.navigation.VrddhihNavigationDrawerSheet
import com.vrddhih.app.core.theme.VrddhihBrushes
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.ui.components.CalendarToggleButton
import com.vrddhih.app.core.ui.components.MainScreenHeader
import com.vrddhih.app.core.ui.components.StatsToggleButton
import com.vrddhih.app.core.ui.components.VrddhihCalendarDialog
import com.vrddhih.app.core.ui.components.VrddhihFloatingActionButton
import com.vrddhih.app.core.ui.gesture.consumeHorizontalNestedScroll
import com.vrddhih.app.features.activities.ActivityTab
import com.vrddhih.app.features.categories.CategoriesRoute
import com.vrddhih.app.features.dashboard.DashboardViewModel
import com.vrddhih.app.features.dashboard.components.DashboardCreationSheet
import com.vrddhih.app.features.goals.GoalsViewModel
import com.vrddhih.app.features.habits.HabitsViewModel
import com.vrddhih.app.features.quests.QuestEditorScreen
import com.vrddhih.app.features.quests.QuestsViewModel
import com.vrddhih.app.features.statistics.StatisticsScreen
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun VrddhihApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    pagerState: PagerState = rememberPagerState(initialPage = 0, pageCount = { TopLevelDestination.entries.size }),
    questsViewModel: QuestsViewModel = viewModel(factory = QuestsViewModel.Factory),
    dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
    habitsViewModel: HabitsViewModel = viewModel(factory = HabitsViewModel.Factory),
    goalsViewModel: GoalsViewModel = viewModel(factory = GoalsViewModel.Factory),
) {
    val coroutineScope = rememberCoroutineScope()
    val dateState by dashboardViewModel.dateState.collectAsStateWithLifecycle()
    var showCalendar by rememberSaveable { mutableStateOf(false) }
    var showDashboardCreateSheet by rememberSaveable { mutableStateOf(false) }
    var activeActivityTab by rememberSaveable { mutableStateOf(ActivityTab.TASKS) }

    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = modifier.fillMaxSize(),
    ) {
        composable("main") {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val isDrawerActive = drawerState.isOpen || drawerState.targetValue == DrawerValue.Open

            BackHandler(enabled = isDrawerActive) {
                coroutineScope.launch { drawerState.close() }
            }

            BackHandler(enabled = !isDrawerActive && pagerState.currentPage != 0) {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(
                        page = 0,
                        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                    )
                }
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = isDrawerActive,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isDrawerActive) Modifier.consumeHorizontalNestedScroll() else Modifier),
                drawerContent = {
                    VrddhihNavigationDrawerSheet(
                        selectedDate = dateState.selectedDate,
                        onNavigateToCategories = {
                            coroutineScope.launch {
                                drawerState.close()
                                navController.navigate("categories")
                            }
                        },
                        onNavigateToThemes = {
                            coroutineScope.launch {
                                drawerState.close()
                                navController.navigate("themes")
                            }
                        },
                    )
                },
            ) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VrddhihBrushes.Backdrop),
                    containerColor = Color.Transparent,
                    topBar = {
                        val activePage = if (pagerState.isScrollInProgress) pagerState.targetPage else pagerState.currentPage
                        val currentDestination = TopLevelDestination.entries[activePage]
                        val isTodaySelected = dateState.selectedDate == dateState.today
                        val titleText = when (currentDestination) {
                            TopLevelDestination.DASHBOARD -> {
                                if (isTodaySelected) {
                                    stringResource(R.string.calendar_today)
                                } else {
                                    remember(dateState.selectedDate) {
                                        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
                                        dateState.selectedDate.format(formatter)
                                    }
                                }
                            }
                            TopLevelDestination.ACTIVITIES -> stringResource(R.string.nav_activities)
                            TopLevelDestination.GOALS -> stringResource(R.string.nav_goals)
                            TopLevelDestination.FINANCE -> stringResource(R.string.nav_finance)
                        }

                        MainScreenHeader(
                            pageIndex = activePage,
                            title = titleText,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            isTitleClickable = activePage == 0 && !isTodaySelected,
                            onTitleClick = dashboardViewModel::selectToday,
                            titleClickLabel = stringResource(R.string.date_strip_today_action),
                            actions = {
                                AnimatedVisibility(
                                    visible = activePage == 0,
                                    enter = fadeIn(tween(180)),
                                    exit = fadeOut(tween(150)),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                                    ) {
                                        StatsToggleButton(
                                            onClick = { navController.navigate("statistics") },
                                        )
                                        CalendarToggleButton(
                                            onClick = { showCalendar = !showCalendar },
                                        )
                                    }
                                }
                            },
                        )
                    },
                    bottomBar = {
                        VrddhihBottomBar(
                            destinations = TopLevelDestination.entries,
                            isSelected = { it.ordinal == (if (pagerState.isScrollInProgress) pagerState.targetPage else pagerState.currentPage) },
                            selectedPageFraction = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
                            onNavigate = { destination ->
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        page = destination.ordinal,
                                        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                                    )
                                }
                            },
                        )
                    },
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        VrddhihNavHost(
                            pagerState = pagerState,
                            modifier = Modifier.padding(innerPadding),
                            onNavigateToCategories = { navController.navigate("categories") },
                            onNavigateToStats = { navController.navigate("statistics") },
                            onNavigateToCreateQuest = { navController.navigate("quest_editor") },
                            onNavigateToEditQuest = { questId, date ->
                                navController.navigate("quest_editor?questId=$questId&occurrenceDate=$date")
                            },
                            onNavigateToCreateHabit = { navController.navigate("habit_editor") },
                            onNavigateToEditHabit = { habitId ->
                                navController.navigate("habit_editor?habitId=$habitId")
                            },
                            questsViewModel = questsViewModel,
                            dashboardViewModel = dashboardViewModel,
                            habitsViewModel = habitsViewModel,
                            goalsViewModel = goalsViewModel,
                            selectedActivityTab = activeActivityTab,
                            onActivityTabSelected = { activeActivityTab = it },
                        )

                        val activePage = if (pagerState.isScrollInProgress) pagerState.targetPage else pagerState.currentPage
                        val isFabVisible = activePage != TopLevelDestination.FINANCE.ordinal

                        AnimatedVisibility(
                            visible = isFabVisible,
                            enter = scaleIn(spring(stiffness = 600f, dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(tween(180)),
                            exit = scaleOut(tween(150)) + fadeOut(tween(150)),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(
                                    end = 20.dp,
                                    bottom = innerPadding.calculateBottomPadding() + 16.dp,
                                ),
                        ) {
                            VrddhihFloatingActionButton(
                                onClick = {
                                    when (TopLevelDestination.entries[activePage]) {
                                        TopLevelDestination.DASHBOARD -> {
                                            showDashboardCreateSheet = true
                                        }
                                        TopLevelDestination.ACTIVITIES -> {
                                            when (activeActivityTab) {
                                                ActivityTab.TASKS -> navController.navigate("quest_editor")
                                                ActivityTab.HABITS -> navController.navigate("habit_editor")
                                            }
                                        }
                                        TopLevelDestination.GOALS -> {
                                            goalsViewModel.openCreateGoal()
                                        }
                                        TopLevelDestination.FINANCE -> {
                                            // Preserved without common FAB
                                        }
                                    }
                                },
                                contentDescription = stringResource(R.string.action_create),
                            )
                        }
                    }
                }
            }

            if (showDashboardCreateSheet) {
                DashboardCreationSheet(
                    onDismiss = { showDashboardCreateSheet = false },
                    onCreateQuest = {
                        showDashboardCreateSheet = false
                        navController.navigate("quest_editor")
                    },
                    onCreateHabit = {
                        showDashboardCreateSheet = false
                        navController.navigate("habit_editor")
                    },
                    onCreateGoal = {
                        showDashboardCreateSheet = false
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                page = TopLevelDestination.GOALS.ordinal,
                                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                            )
                            goalsViewModel.openCreateGoal()
                        }
                    },
                )
            }

            if (showCalendar) {
                VrddhihCalendarDialog(
                    selectedDate = dateState.selectedDate,
                    today = dateState.today,
                    onDateSelected = { date ->
                        dashboardViewModel.selectDate(date)
                        showCalendar = false
                    },
                    onTodaySelected = {
                        dashboardViewModel.selectToday()
                        showCalendar = false
                    },
                    onDismiss = { showCalendar = false },
                )
            }
        }

        composable(
            route = "quest_editor?questId={questId}&occurrenceDate={occurrenceDate}",
            arguments = listOf(
                navArgument("questId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("occurrenceDate") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStackEntry ->
            val questIdArg = backStackEntry.arguments?.getString("questId")?.toLongOrNull()
            val dateArg = backStackEntry.arguments?.getString("occurrenceDate")?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            }

            LaunchedEffect(questIdArg, dateArg) {
                questsViewModel.prepareEditor(questIdArg, dateArg)
            }

            val uiState by questsViewModel.uiState.collectAsStateWithLifecycle()

            QuestEditorScreen(
                draft = uiState.editingDraft,
                isLoading = uiState.isEditorLoading,
                isEdit = uiState.editingQuestId != null || questIdArg != null,
                scope = uiState.editingScope,
                errors = uiState.editorErrors,
                categories = uiState.categories,
                onDraftChange = questsViewModel::updateEditorDraft,
                onScopeChange = questsViewModel::updateEditorScope,
                onSave = {
                    questsViewModel.saveEditor(
                        onSaved = {
                            navController.popBackStack()
                        }
                    )
                },
                onCancel = {
                    questsViewModel.closeEditor()
                    navController.popBackStack()
                },
                onCreateCustomCategory = questsViewModel::createCustomCategory,
            )
        }

        composable(
            route = "habit_editor?habitId={habitId}",
            arguments = listOf(
                navArgument("habitId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStackEntry ->
            val habitIdArg = backStackEntry.arguments?.getString("habitId")?.toLongOrNull()

            LaunchedEffect(habitIdArg) {
                habitsViewModel.prepareEditor(habitIdArg)
            }

            val habitsUiState by habitsViewModel.uiState.collectAsStateWithLifecycle()

            com.vrddhih.app.features.habits.HabitEditorScreen(
                draft = habitsUiState.editingDraft,
                isLoading = habitsUiState.isEditorLoading,
                isEdit = habitsUiState.editingDraft?.id != null,
                errors = habitsUiState.editorErrors,
                categories = habitsUiState.categories,
                onDraftChange = habitsViewModel::updateEditorDraft,
                onSave = {
                    habitsViewModel.saveEditor(
                        onSaved = {
                            navController.popBackStack()
                        }
                    )
                },
                onSaveAndNew = {
                    habitsViewModel.saveAndNewHabit()
                },
                onDelete = {
                    habitsViewModel.deleteEditingHabit(
                        onDeleted = {
                            navController.popBackStack()
                        }
                    )
                },
                onCancel = {
                    habitsViewModel.closeEditor()
                    navController.popBackStack()
                },
                onCreateCustomCategory = habitsViewModel::createCustomCategory,
            )
        }

        composable("categories") {
            CategoriesRoute(
                onBack = { navController.popBackStack() },
            )
        }

        composable("statistics") {
            StatisticsScreen(
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                onBack = { navController.popBackStack() },
            )
        }

        composable("themes") {
            com.vrddhih.app.features.themes.ThemesScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
