package com.vrddhih.app.features.activities

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.theme.VrddhihTheme
import com.vrddhih.app.core.ui.components.ScreenHeader
import com.vrddhih.app.core.ui.components.neonAmbientGlow
import com.vrddhih.app.features.habits.HabitsScreen
import com.vrddhih.app.features.habits.HabitsViewModel
import com.vrddhih.app.features.quests.QuestsRoute
import com.vrddhih.app.features.quests.QuestsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class ActivityTab {
    TASKS,
    HABITS,
}

@Composable
fun ActivitiesRoute(
    modifier: Modifier = Modifier,
    parentPagerState: PagerState? = null,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToCreateQuest: () -> Unit = {},
    onNavigateToEditQuest: (Long, LocalDate) -> Unit = { _, _ -> },
    questsViewModel: QuestsViewModel = viewModel(factory = QuestsViewModel.Factory),
    habitsViewModel: HabitsViewModel = viewModel(factory = HabitsViewModel.Factory),
    initialTab: ActivityTab = ActivityTab.TASKS,
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }

    ActivitiesScreen(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        parentPagerState = parentPagerState,
        onNavigateToCategories = onNavigateToCategories,
        onNavigateToCreateQuest = onNavigateToCreateQuest,
        onNavigateToEditQuest = onNavigateToEditQuest,
        questsViewModel = questsViewModel,
        habitsViewModel = habitsViewModel,
        modifier = modifier,
    )
}

@Composable
fun ActivitiesScreen(
    selectedTab: ActivityTab,
    onTabSelected: (ActivityTab) -> Unit,
    modifier: Modifier = Modifier,
    parentPagerState: PagerState? = null,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToCreateQuest: () -> Unit = {},
    onNavigateToEditQuest: (Long, LocalDate) -> Unit = { _, _ -> },
    questsViewModel: QuestsViewModel = viewModel(factory = QuestsViewModel.Factory),
    habitsViewModel: HabitsViewModel = viewModel(factory = HabitsViewModel.Factory),
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = selectedTab.ordinal,
        pageCount = { ActivityTab.entries.size },
    )

    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab.ordinal && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(
                page = selectedTab.ordinal,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            )
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val currentTab = ActivityTab.entries[pagerState.currentPage]
        if (currentTab != selectedTab) {
            onTabSelected(currentTab)
        }
    }

    val nestedConnection = remember(parentPagerState, pagerState) {
        parentPagerState?.let { parent ->
            createNestedPagerConnection(parentState = parent, childState = pagerState)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = VrddhihColors.Void,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = VrddhihDimens.ContentMaxWidth)
                    .fillMaxWidth()
                    .padding(
                        start = VrddhihSpacing.lg,
                        end = VrddhihSpacing.lg,
                        top = VrddhihSpacing.xl,
                        bottom = VrddhihSpacing.xs,
                    ),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
            ) {
                ScreenHeader(
                    eyebrow = stringResource(R.string.activities_eyebrow),
                    title = stringResource(R.string.activities_title),
                )

                val displayTab = ActivityTab.entries[pagerState.targetPage]
                ActivitiesSegmentedControl(
                    selectedTab = displayTab,
                    onTabSelected = { tab ->
                        if (tab != displayTab) {
                            onTabSelected(tab)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    page = tab.ordinal,
                                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                                )
                            }
                        }
                    },
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .then(if (nestedConnection != null) Modifier.nestedScroll(nestedConnection) else Modifier),
                key = { page -> ActivityTab.entries[page].name },
                beyondViewportPageCount = 1,
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState,
                    snapAnimationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = 850f,
                    ),
                ),
            ) { page ->
                when (ActivityTab.entries[page]) {
                    ActivityTab.TASKS -> {
                        QuestsRoute(
                            showHeader = false,
                            onNavigateToCategories = onNavigateToCategories,
                            onNavigateToCreateQuest = onNavigateToCreateQuest,
                            onNavigateToEditQuest = onNavigateToEditQuest,
                            viewModel = questsViewModel,
                        )
                    }
                    ActivityTab.HABITS -> {
                        HabitsScreen(
                            showHeader = false,
                            viewModel = habitsViewModel,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivitiesSegmentedControl(
    selectedTab: ActivityTab,
    onTabSelected: (ActivityTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(VrddhihRadii.sm))
            .background(VrddhihColors.PanelRaised)
            .border(1.dp, VrddhihColors.Outline, RoundedCornerShape(VrddhihRadii.sm))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ActivitySegmentedButton(
            label = stringResource(R.string.activities_tab_tasks),
            icon = Icons.Outlined.TaskAlt,
            selected = selectedTab == ActivityTab.TASKS,
            onClick = { onTabSelected(ActivityTab.TASKS) },
            modifier = Modifier.weight(1f),
        )
        ActivitySegmentedButton(
            label = stringResource(R.string.activities_tab_habits),
            icon = Icons.Outlined.Autorenew,
            selected = selectedTab == ActivityTab.HABITS,
            onClick = { onTabSelected(ActivityTab.HABITS) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ActivitySegmentedButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(VrddhihRadii.xs)
    val animatedBg by animateColorAsState(
        targetValue = if (selected) VrddhihColors.Electric.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "tab-bg",
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (selected) VrddhihColors.Electric else Color.Transparent,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "tab-border",
    )
    val animatedContentColor by animateColorAsState(
        targetValue = if (selected) VrddhihColors.Ice else VrddhihColors.TextSecondary,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "tab-text",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (selected) {
                    Modifier.neonAmbientGlow(VrddhihColors.Electric, radius = 6.dp, alpha = 0.35f)
                } else {
                    Modifier
                },
            )
            .background(animatedBg, shape)
            .border(1.dp, animatedBorder, shape)
            .clip(shape)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = animatedContentColor,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(VrddhihSpacing.xs))
            Text(
                text = label.uppercase(),
                style = VrddhihTextStyles.SystemLabel,
                color = animatedContentColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ActivitiesSegmentedControlPreview() {
    VrddhihTheme {
        ActivitiesSegmentedControl(
            selectedTab = ActivityTab.TASKS,
            onTabSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

internal fun createNestedPagerConnection(
    parentState: PagerState,
    childState: PagerState,
): NestedScrollConnection = object : NestedScrollConnection {
    private var isSettling = false

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (source != NestedScrollSource.UserInput) return Offset.Zero
        val delta = available.x
        if (delta == 0f) return Offset.Zero

        // If parent pager is already displaced mid-drag between screens,
        // it consumes all incoming scroll delta until settled back to 0.
        if (abs(parentState.currentPageOffsetFraction) > 1e-4f) {
            val consumed = parentState.dispatchRawDelta(delta)
            return Offset(consumed, 0f)
        }
        return Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (source != NestedScrollSource.UserInput) return Offset.Zero
        val delta = available.x
        if (delta == 0f) return Offset.Zero

        val isAtStartBoundary = childState.currentPage == 0 && childState.currentPageOffsetFraction <= 1e-4f && delta > 0f
        val isAtEndBoundary = childState.currentPage == childState.pageCount - 1 && childState.currentPageOffsetFraction >= -1e-4f && delta < 0f

        val canParentScrollBackward = parentState.currentPage > 0 || parentState.currentPageOffsetFraction > 1e-4f || parentState.canScrollBackward
        val canParentScrollForward = parentState.currentPage < parentState.pageCount - 1 || parentState.currentPageOffsetFraction < -1e-4f || parentState.canScrollForward

        if (isAtStartBoundary && canParentScrollBackward) {
            val consumed = parentState.dispatchRawDelta(delta)
            return Offset(consumed, 0f)
        }

        if (isAtEndBoundary && canParentScrollForward) {
            val consumed = parentState.dispatchRawDelta(delta)
            return Offset(consumed, 0f)
        }

        return Offset.Zero
    }

    private suspend fun settleParent(velocityX: Float): Velocity {
        if (isSettling) return Velocity.Zero
        isSettling = true
        try {
            val fraction = parentState.currentPageOffsetFraction
            val currentPage = parentState.currentPage
            val targetPage = when {
                velocityX > 400f -> (currentPage - 1).coerceAtLeast(0)
                velocityX < -400f -> (currentPage + 1).coerceAtMost(parentState.pageCount - 1)
                fraction < -0.18f -> (currentPage - 1).coerceAtLeast(0)
                fraction > 0.18f -> (currentPage + 1).coerceAtMost(parentState.pageCount - 1)
                else -> currentPage
            }
            parentState.animateScrollToPage(
                page = targetPage,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = 850f,
                ),
            )
            return Velocity(velocityX, 0f)
        } finally {
            isSettling = false
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (abs(parentState.currentPageOffsetFraction) > 1e-4f) {
            return settleParent(available.x)
        }
        return Velocity.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (abs(parentState.currentPageOffsetFraction) > 1e-4f) {
            return settleParent(available.x)
        }
        return Velocity.Zero
    }
}

