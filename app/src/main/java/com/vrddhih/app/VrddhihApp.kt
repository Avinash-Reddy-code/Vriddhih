package com.vrddhih.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.vrddhih.app.core.navigation.TopLevelDestination
import com.vrddhih.app.core.navigation.VrddhihBottomBar
import com.vrddhih.app.core.navigation.VrddhihNavHost
import com.vrddhih.app.core.theme.VrddhihBrushes
import com.vrddhih.app.features.categories.CategoriesRoute
import com.vrddhih.app.features.quests.QuestEditorScreen
import com.vrddhih.app.features.quests.QuestsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun VrddhihApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    pagerState: PagerState = rememberPagerState(initialPage = 0, pageCount = { TopLevelDestination.entries.size }),
    questsViewModel: QuestsViewModel = viewModel(factory = QuestsViewModel.Factory),
) {
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = modifier.fillMaxSize(),
    ) {
        composable("main") {
            BackHandler(enabled = pagerState.currentPage != 0) {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(0)
                }
            }

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VrddhihBrushes.Backdrop),
                containerColor = Color.Transparent,
                bottomBar = {
                    VrddhihBottomBar(
                        destinations = TopLevelDestination.entries,
                        isSelected = { it.ordinal == pagerState.currentPage },
                        onNavigate = { destination ->
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(destination.ordinal)
                            }
                        },
                    )
                },
            ) { innerPadding ->
                VrddhihNavHost(
                    pagerState = pagerState,
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToCategories = { navController.navigate("categories") },
                    onNavigateToCreateQuest = { navController.navigate("quest_editor") },
                    onNavigateToEditQuest = { questId, date ->
                        navController.navigate("quest_editor?questId=$questId&occurrenceDate=$date")
                    },
                    questsViewModel = questsViewModel,
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

        composable("categories") {
            CategoriesRoute(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
